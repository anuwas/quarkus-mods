package com.example.batch.service;

import com.example.batch.config.BatchProperties;
import com.example.batch.dto.BatchResult;
import com.example.batch.dto.ChunkResult;
import com.example.batch.entity.stg.StagingSynchLog;
import com.example.batch.repository.main.BatchExecutionLogRepository;
import com.example.batch.repository.stg.StagingSynchLogRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract base class for entity-specific batch processing services.
 *
 * <p>Encapsulates the common four-phase chunk lifecycle:
 * <ol>
 *   <li><b>CLAIM</b> — lock PENDING rows via {@code SELECT … FOR UPDATE SKIP LOCKED}</li>
 *   <li><b>PROCESS</b> — validate and map staging records (delegated to subclass)</li>
 *   <li><b>COMMIT</b> — write entities to mainDB and update staging statuses</li>
 *   <li><b>AUDIT LOG</b> — record execution metrics</li>
 * </ol>
 *
 * <p>Subclasses must implement:
 * <ul>
 *   <li>{@link #entityName()} — human-readable name for logging and metrics</li>
 *   <li>{@link #tableName()} — the staging table name to filter on (e.g. "stg_centres")</li>
 *   <li>{@link #processRecords(List, String, String)} — validation and mapping logic</li>
 *   <li>{@link #commitToMainDb(ChunkResult)} — upsert entities to mainDB</li>
 * </ul>
 *
 * <p>Multi-node safety is enforced by {@code SELECT FOR UPDATE SKIP LOCKED} in Phase 1.
 * A per-instance {@link AtomicBoolean} guard prevents concurrent chunk execution
 * on the same JVM for the same entity type.
 */
public abstract class BatchProcessingService {

    @Inject BatchProperties              props;
    @Inject @io.quarkus.hibernate.orm.PersistenceUnit("stgdb") StagingSynchLogRepository stagingRepo;
    @Inject BatchExecutionLogRepository  logRepo;
    @Inject MeterRegistry                meterRegistry;

    /** Node identifier — hostname + PID, set once on first use. */
    private volatile String nodeId;

    /** Prevents concurrent chunk execution on the same JVM instance for this entity. */
    private final AtomicBoolean executing = new AtomicBoolean(false);

    // -----------------------------------------------------------------------
    // Template methods — subclasses must implement
    // -----------------------------------------------------------------------

    /**
     * Human-readable entity name used in batch IDs, log messages, and metric names.
     * Examples: "CENTRE", "STUDENT".
     */
    protected abstract String entityName();

    /**
     * The staging table name used to filter {@code staging_synch_log.table_name}
     * when claiming a chunk (e.g. "stg_centres", "stg_student").
     */
    protected abstract String tableName();

    /**
     * Validate and transform the claimed staging records into a {@link ChunkResult}.
     *
     * @param records  the claimed PROCESSING records from staging_synch_log
     * @param batchId  current batch run identifier (for logging)
     * @param nodeId   current node identifier
     * @return a ChunkResult containing entities, success IDs, and failure IDs
     */
    protected abstract ChunkResult processRecords(List<StagingSynchLog> records,
                                                  String batchId, String nodeId);

    /**
     * Write the processed entities to mainDB.
     * Must be annotated {@code @Transactional(REQUIRES_NEW)} in the concrete subclass.
     *
     * @param result the chunk result containing entities to persist
     */
    protected abstract void commitToMainDb(ChunkResult result);

    // -----------------------------------------------------------------------
    // Public entry point
    // -----------------------------------------------------------------------

    /**
     * Process one chunk.  Called by the scheduler and the manual-trigger REST endpoint.
     *
     * @return BatchResult describing what happened
     */
    public BatchResult processNextChunk() {
        if (!executing.compareAndSet(false, true)) {
            Log.warnf("[%s][%s] Skipping — previous chunk is still executing.",
                    getNodeId(), entityName());
            return BatchResult.skipped(getNodeId());
        }

        long startMs = System.currentTimeMillis();
        String batchId = entityName() + "-" +
                UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        try {
            return doProcessChunk(batchId, startMs);
        } finally {
            executing.set(false);
        }
    }

    // -----------------------------------------------------------------------
    // Internal pipeline
    // -----------------------------------------------------------------------

    private BatchResult doProcessChunk(String batchId, long startMs) {
        String node      = getNodeId();
        int    chunkSize = props.chunkSize();

        // Persist audit log entry immediately (REQUIRES_NEW — survives rollback)
        logRepo.createLog(batchId, node, chunkSize);

        // ── Phase 1: CLAIM ────────────────────────────────────────────────
        List<StagingSynchLog> claimed = stagingRepo.claimChunkByTable(chunkSize, node, tableName());

        if (claimed.isEmpty()) {
            Log.infof("[%s][%s] No PENDING %s records available — skipping.",
                    node, batchId, entityName());
            logRepo.completeLog(batchId, "EMPTY", 0, 0, 0,
                    System.currentTimeMillis() - startMs, null);
            recordMetric("batch." + entityName().toLowerCase() + ".run.empty");
            return BatchResult.empty(batchId, node);
        }

        Log.infof("[%s][%s] Processing chunk of %d %s records.",
                node, batchId, claimed.size(), entityName());

        List<Long> allClaimedIds = claimed.stream().map(r -> r.id).toList();

        try {
            // ── Phase 2: PROCESS (in-memory) ──────────────────────────────
            ChunkResult result = processRecords(claimed, batchId, node);

            // ── Phase 3: COMMIT ───────────────────────────────────────────
            commitChunk(result);

            // ── Phase 4: AUDIT LOG ────────────────────────────────────────
            long duration = System.currentTimeMillis() - startMs;
            String finalStatus = result.failedIds().isEmpty()
                    ? "COMPLETED" : "COMPLETED_WITH_ERRORS";

            logRepo.completeLog(batchId, finalStatus,
                    claimed.size(), result.successIds().size(),
                    result.failedIds().size(), duration, null);

            recordChunkMetrics(result.successIds().size(), result.failedIds().size(), duration);

            Log.infof("[%s][%s] Done. ok=%d failed=%d duration=%d ms (%.0f rec/s)",
                    node, batchId, result.successIds().size(),
                    result.failedIds().size(), duration,
                    duration > 0 ? result.successIds().size() * 1000.0 / duration : 0);

            return BatchResult.completed(batchId, node, claimed.size(),
                    result.successIds().size(), result.failedIds().size(), duration);

        } catch (Exception e) {
            // Phase 3 rolled back — mark everything FAILED in a new transaction
            Log.errorf(e, "[%s][%s] Chunk transaction failed — marking %d records as FAILED.",
                    node, batchId, allClaimedIds.size());

            stagingRepo.bulkMarkFailed(allClaimedIds,
                    "Chunk transaction failed: " + e.getMessage());

            long duration = System.currentTimeMillis() - startMs;
            logRepo.completeLog(batchId, "FAILED",
                    claimed.size(), 0, allClaimedIds.size(), duration, e.getMessage());

            recordMetric("batch." + entityName().toLowerCase() + ".run.failed");
            return BatchResult.failed(batchId, node, allClaimedIds.size(), e.getMessage());
        }
    }

    /**
     * Commit the chunk: write entities to mainDB then update staging statuses.
     *
     * <p>Each datasource is written in its own REQUIRES_NEW transaction to avoid
     * enlisting two datasources in the same JTA transaction.
     *
     * <p>Ordering: mainDB first, then stgDB.  If the stgDB update fails after
     * mainDB has committed, the staging records remain in PROCESSING and can
     * be recovered via the admin reset endpoint.  The mainDB upsert is
     * idempotent, so a retry is safe.
     */
    protected void commitChunk(ChunkResult result) {
        // 3a — write entities to mainDB (own transaction — delegated to subclass)
        commitToMainDb(result);

        // 3b — update staging statuses in stgDB (own transaction)
        commitToStgDb(result);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    protected void commitToStgDb(ChunkResult result) {
        stagingRepo.markCompletedBulk(result.successIds());
        stagingRepo.markFailedBulk(result.failedIds());
    }

    // -----------------------------------------------------------------------
    // Metrics
    // -----------------------------------------------------------------------

    private void recordChunkMetrics(long ok, long failed, long durationMs) {
        String prefix = "batch." + entityName().toLowerCase();
        Counter.builder(prefix + ".records.ok")
                .register(meterRegistry).increment(ok);
        Counter.builder(prefix + ".records.failed")
                .register(meterRegistry).increment(failed);
        Timer.builder(prefix + ".chunk.duration")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    private void recordMetric(String name) {
        Counter.builder(name).register(meterRegistry).increment();
    }

    // -----------------------------------------------------------------------
    // Node identity
    // -----------------------------------------------------------------------

    public String getNodeId() {
        if (nodeId == null) {
            try {
                nodeId = InetAddress.getLocalHost().getHostName() + "-" +
                         ProcessHandle.current().pid();
            } catch (Exception e) {
                nodeId = "node-" + UUID.randomUUID().toString().substring(0, 8);
            }
        }
        return nodeId;
    }
}
