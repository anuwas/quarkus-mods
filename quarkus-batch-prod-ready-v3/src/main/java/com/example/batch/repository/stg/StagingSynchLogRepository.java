package com.example.batch.repository.stg;

import com.example.batch.config.RecordStatus;
import com.example.batch.entity.stg.StagingSynchLog;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Repository for StagingSynchLog operations on stgDB.
 *
 * Critical design: claimChunk() uses PostgreSQL's
 *   SELECT … FOR UPDATE SKIP LOCKED
 * which is the standard mechanism for multi-node batch coordination:
 *
 *   - FOR UPDATE   : places a row-level exclusive lock on selected rows.
 *   - SKIP LOCKED  : any rows already locked by another session are silently
 *                    skipped rather than causing the query to wait or fail.
 *
 * This guarantees that two nodes running simultaneously will always claim
 * different, non-overlapping sets of records with zero contention.
 * The claim (status → PROCESSING + nodeId) is committed in its own
 * REQUIRES_NEW transaction so it is immediately visible to other nodes.
 */
@ApplicationScoped
@io.quarkus.hibernate.orm.PersistenceUnit("stgdb")
public class StagingSynchLogRepository implements PanacheRepositoryBase<StagingSynchLog, Long> {

    @Inject
    @io.quarkus.hibernate.orm.PersistenceUnit("stgdb")
    EntityManager em;

    // -----------------------------------------------------------------------
    // Claim — the core multi-node safety mechanism
    // -----------------------------------------------------------------------

    /**
     * Atomically claim up to {@code chunkSize} PENDING records for {@code nodeId}.
     *
     * <p>Steps:
     * <ol>
     *   <li>SELECT the next N PENDING ids using FOR UPDATE SKIP LOCKED
     *       (other nodes will skip these rows immediately)</li>
     *   <li>UPDATE status → PROCESSING, node_id → nodeId in the same transaction</li>
     *   <li>COMMIT — the PROCESSING status is visible to all other nodes</li>
     *   <li>Return the claimed records so the caller can process them</li>
     * </ol>
     *
     * @param chunkSize  maximum records to claim
     * @param nodeId     identifier of the calling node (hostname + PID)
     * @return list of claimed StagingSynchLog entities (may be empty if nothing pending)
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public List<StagingSynchLog> claimChunk(int chunkSize, String nodeId) {
        return claimChunkByTable(chunkSize, nodeId, null);
    }

    /**
     * Atomically claim up to {@code chunkSize} PENDING records for a specific
     * {@code tableName} (e.g. "stg_centres" or "stg_student").
     *
     * <p>If {@code tableName} is {@code null}, all PENDING records are eligible
     * (backwards-compatible with the original claimChunk behaviour).
     *
     * @param chunkSize  maximum records to claim
     * @param nodeId     identifier of the calling node (hostname + PID)
     * @param tableName  the staging table to filter on, or null for all tables
     * @return list of claimed StagingSynchLog entities (may be empty if nothing pending)
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public List<StagingSynchLog> claimChunkByTable(int chunkSize, String nodeId, String tableName) {
        // Step 1 — find candidate IDs using keyset + FOR UPDATE SKIP LOCKED
        String sql = "SELECT id FROM staging_synch_log WHERE status = 'PENDING' " +
                     (tableName != null ? "AND table_name = :tableName " : "") +
                     "ORDER BY id ASC LIMIT :size FOR UPDATE SKIP LOCKED";

        var query = em.createNativeQuery(sql)
            .setParameter("size", chunkSize);

        if (tableName != null) {
            query.setParameter("tableName", tableName);
        }

        @SuppressWarnings("unchecked")
        List<Long> ids = query.getResultList();

        if (ids.isEmpty()) {
            Log.debugf("[%s] claimChunkByTable(%s): no PENDING records available.", nodeId, tableName);
            return List.of();
        }

        // Step 2 — atomically flip to PROCESSING with node ownership
        int updated = em.createQuery(
                "UPDATE StagingSynchLog r " +
                "SET r.status = :processing, r.nodeId = :nodeId, r.updatedAt = :now " +
                "WHERE r.id IN :ids AND r.status = :pending")
            .setParameter("processing", RecordStatus.PROCESSING)
            .setParameter("nodeId",     nodeId)
            .setParameter("now",        LocalDateTime.now())
            .setParameter("ids",        ids)
            .setParameter("pending",    RecordStatus.PENDING)
            .executeUpdate();

        Log.infof("[%s] Claimed %d/%d records for table '%s' (PENDING→PROCESSING).",
                nodeId, updated, ids.size(), tableName != null ? tableName : "ALL");

        // Step 3 — load full entities for the claimed IDs
        return list("id IN ?1 AND status = ?2 ORDER BY id ASC",
                ids, RecordStatus.PROCESSING);
    }

    // -----------------------------------------------------------------------
    // Status updates (called inside the main processing transaction)
    // -----------------------------------------------------------------------

    /**
     * Bulk-mark a list of records as COMPLETED in a single UPDATE.
     * Called once per chunk on success IDs — avoids per-record connection
     * re-enlistment which causes "Enlisted connection used without active transaction".
     */
    @Transactional(Transactional.TxType.MANDATORY)
    public void markCompletedBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        em.createQuery(
                "UPDATE StagingSynchLog r " +
                "SET r.status = :s, r.processedAt = :now, r.updatedAt = :now " +
                "WHERE r.id IN :ids")
            .setParameter("s",   RecordStatus.COMPLETED)
            .setParameter("now", LocalDateTime.now())
            .setParameter("ids", ids)
            .executeUpdate();
    }

    /**
     * Bulk-mark a map of records as FAILED with individual error messages.
     * Each record gets its own error message via individual updates,
     * but all within the caller's existing transaction.
     */
    @Transactional(Transactional.TxType.MANDATORY)
    public void markFailedBulk(Map<Long, String> failedMap) {
        if (failedMap == null || failedMap.isEmpty()) return;
        LocalDateTime now = LocalDateTime.now();
        for (var entry : failedMap.entrySet()) {
            em.createQuery(
                    "UPDATE StagingSynchLog r " +
                    "SET r.status = :s, r.errorMessage = :err, " +
                    "    r.processedAt = :now, r.updatedAt = :now " +
                    "WHERE r.id = :id")
                .setParameter("s",   RecordStatus.FAILED)
                .setParameter("err", truncate(entry.getValue(), 1000))
                .setParameter("now", now)
                .setParameter("id",  entry.getKey())
                .executeUpdate();
        }
    }

    /**
     * Bulk-mark all records in the chunk as FAILED.
     * Used when the entire chunk write to mainDB fails (non-recoverable).
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void bulkMarkFailed(List<Long> ids, String errorMessage) {
        if (ids == null || ids.isEmpty()) return;
        em.createQuery(
                "UPDATE StagingSynchLog r " +
                "SET r.status = :s, r.errorMessage = :err, " +
                "    r.processedAt = :now, r.updatedAt = :now " +
                "WHERE r.id IN :ids")
            .setParameter("s",   RecordStatus.FAILED)
            .setParameter("err", truncate(errorMessage, 1000))
            .setParameter("now", LocalDateTime.now())
            .setParameter("ids", ids)
            .executeUpdate();
        Log.warnf("Bulk-marked %d records as FAILED: %s", ids.size(), errorMessage);
    }

    /**
     * Reset PROCESSING records back to PENDING.
     * Used by the admin API to recover from node crashes that left orphaned rows.
     */
    @Transactional
    public int resetOrphanedProcessingRecords(String nodeId) {
        int count = em.createQuery(
                "UPDATE StagingSynchLog r " +
                "SET r.status = :pending, r.nodeId = null, r.updatedAt = :now " +
                "WHERE r.status = :processing " +
                (nodeId != null ? "AND r.nodeId = :nodeId" : ""))
            .setParameter("pending",    RecordStatus.PENDING)
            .setParameter("processing", RecordStatus.PROCESSING)
            .setParameter("now",        LocalDateTime.now())
            .setParameter("nodeId",     nodeId)
            .executeUpdate();
        Log.infof("Reset %d orphaned PROCESSING records to PENDING (nodeId=%s).", count, nodeId);
        return count;
    }

    // -----------------------------------------------------------------------
    // Read queries (non-mutating)
    // -----------------------------------------------------------------------

    public long countByStatus(RecordStatus status) {
        return count("status", status);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
