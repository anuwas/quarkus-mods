package com.example.batch.repository.main;

import com.example.batch.entity.main.BatchExecutionLog;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for BatchExecutionLog — audit trail of every batch run.
 */
@ApplicationScoped
public class BatchExecutionLogRepository implements PanacheRepositoryBase<BatchExecutionLog, Long> {

    @Inject
    EntityManager em;

    /**
     * Persist a new log entry at the start of a batch run.
     * Uses REQUIRES_NEW so it commits immediately even if the outer
     * batch transaction rolls back later.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public BatchExecutionLog createLog(String batchId, String nodeId, int chunkSize) {
        BatchExecutionLog log = new BatchExecutionLog();
        log.batchId    = batchId;
        log.nodeId     = nodeId;
        log.chunkSize  = chunkSize;
        log.status     = "STARTED";
        log.startedAt  = LocalDateTime.now();
        persist(log);
        return log;
    }

    /**
     * Update the log entry after the batch run completes (success or failure).
     * Uses REQUIRES_NEW so it commits independently of the batch transaction.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void completeLog(String batchId, String status,
                             long read, long ok, long failed,
                             long durationMs, String errorSummary) {
        findByBatchId(batchId).ifPresent(log -> {
            log.status       = status;
            log.recordsRead  = read;
            log.recordsOk    = ok;
            log.recordsFailed= failed;
            log.completedAt  = LocalDateTime.now();
            log.durationMs   = durationMs;
            log.errorSummary = errorSummary;
            if (durationMs > 0) {
                log.throughputRps = (ok * 1000.0) / durationMs;
            }
        });
    }

    // -----------------------------------------------------------------------
    // Queries
    // -----------------------------------------------------------------------

    public Optional<BatchExecutionLog> findByBatchId(String batchId) {
        return find("batchId", batchId).firstResultOptional();
    }

    public List<BatchExecutionLog> findRecent(int limit) {
        return find("ORDER BY startedAt DESC").page(0, limit).list();
    }
}
