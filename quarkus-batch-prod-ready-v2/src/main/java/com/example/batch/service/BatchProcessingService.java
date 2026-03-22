package com.example.batch.service;

import com.example.batch.config.BatchProperties;
import com.example.batch.entity.stg.StagingRecord;
import com.example.batch.repository.main.AggregatedResultRepository;
import com.example.batch.repository.main.BatchExecutionLogRepository;
import com.example.batch.repository.stg.StagingRecordRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Core batch processing service.
 *
 * Processes exactly one chunk per call — the scheduler invokes this once per
 * scheduled tick, ensuring each chunk is its own atomic unit.
 *
 * Per-chunk lifecycle:
 * ─────────────────────────────────────────────────────────────────────
 *  Phase 1 — CLAIM  (REQUIRES_NEW transaction, committed immediately)
 *    SELECT … FOR UPDATE SKIP LOCKED → UPDATE status=PROCESSING
 *    → other nodes skip these rows from this point forward
 *
 *  Phase 2 — PROCESS  (in-memory, no DB)
 *    ChunkAggregator.process() validates each record, aggregates valid ones,
 *    collects failed IDs with their error messages.
 *
 *  Phase 3 — COMMIT  (separate REQUIRES_NEW transaction per datasource)
 *    3a. AggregatedResultRepository.upsertAll() → writes to mainDB (REQUIRES_NEW)
 *    3b. StagingRecordRepository.markCompleted() per success       (REQUIRES_NEW)
 *        StagingRecordRepository.markFailed()    per validation failure
 *    → Each datasource commits independently; avoids cross-datasource XA issues
 *
 *  Phase 4 — AUDIT LOG  (REQUIRES_NEW, always commits)
 *    BatchExecutionLogRepository.completeLog() records final metrics.
 *
 * On any unrecoverable exception in Phase 3 the transaction rolls back and
 * Phase 4 bulk-marks all claimed IDs as FAILED in a new transaction.
 * ─────────────────────────────────────────────────────────────────────
 *
 * Multi-node safety: enforced by SELECT FOR UPDATE SKIP LOCKED in Phase 1.
 * Concurrent execution guard: AtomicBoolean prevents the same node from
 * starting a new chunk while the previous one is still running.
 */
@ApplicationScoped
public class BatchProcessingService {

    @Inject BatchProperties              props;
    @Inject StagingRecordRepository      stagingRepo;
    @Inject AggregatedResultRepository   aggregatedRepo;
    @Inject BatchExecutionLogRepository  logRepo;
    @Inject ChunkAggregator              aggregator;
    @Inject MeterRegistry                meterRegistry;

    /** Node identifier — hostname + PID, set once on first use. */
    private volatile String nodeId;

    /** Prevents concurrent chunk execution on the same JVM instance. */
    private final AtomicBoolean executing = new AtomicBoolean(false);

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
            Log.warnf("[%s] Skipping — previous chunk is still executing.", getNodeId());
            return BatchResult.skipped(getNodeId());
        }

        long startMs = System.currentTimeMillis();
        String batchId = "BATCH-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

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
        String node = getNodeId();
        int chunkSize = props.chunkSize();

        // Persist audit log entry immediately (REQUIRES_NEW — survives rollback)
        logRepo.createLog(batchId, node, chunkSize);

        // ── Phase 1: CLAIM ────────────────────────────────────────────────
        List<StagingRecord> claimed = stagingRepo.claimChunk(chunkSize, node);

        if (claimed.isEmpty()) {
            Log.infof("[%s][%s] No PENDING records available — skipping.", node, batchId);
            logRepo.completeLog(batchId, "EMPTY", 0, 0, 0,
                    System.currentTimeMillis() - startMs, null);
            recordMetric("batch.run.empty");
            return BatchResult.empty(batchId, node);
        }

        Log.infof("[%s][%s] Processing chunk of %d records.", node, batchId, claimed.size());

        List<Long> allClaimedIds = claimed.stream().map(r -> r.id).toList();

        try {
            // ── Phase 2: PROCESS (in-memory) ──────────────────────────────
            ChunkAggregator.ChunkResult result =  aggregator.process(claimed, batchId, node);

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

            stagingRepo.bulkMarkFailed(allClaimedIds, "Chunk transaction failed: " + e.getMessage());

            long duration = System.currentTimeMillis() - startMs;
            logRepo.completeLog(batchId, "FAILED",
                    claimed.size(), 0, allClaimedIds.size(), duration, e.getMessage());

            recordMetric("batch.run.failed");
            return BatchResult.failed(batchId, node, allClaimedIds.size(), e.getMessage());
        }
    }

    /**
     * Write aggregated results to mainDB and then update staging statuses.
     *
     * Each datasource is written in its own REQUIRES_NEW transaction to avoid
     * enlisting two datasources in the same JTA transaction (which caused
     * "Enlisted connection used without active transaction" via Agroal's
     * LocalXAResource).
     *
     * Ordering: mainDB first, then stgDB.  If the stgDB update fails after
     * mainDB has committed, the staging records remain in PROCESSING and can
     * be recovered via the admin reset endpoint.  The mainDB upsert is
     * idempotent, so a retry is safe.
     */
    protected void commitChunk(ChunkAggregator.ChunkResult result) {
        // 3a — write aggregated results to mainDB (own transaction)
        commitToMainDb(result);

        // 3b — update staging statuses in stgDB (own transaction)
        commitToStgDb(result);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    protected void commitToMainDb(ChunkAggregator.ChunkResult result) {
        if (!result.aggregated().isEmpty()) {
            aggregatedRepo.upsertAll(result.aggregated());
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    protected void commitToStgDb(ChunkAggregator.ChunkResult result) {
        stagingRepo.markCompletedBulk(result.successIds());
        stagingRepo.markFailedBulk(result.failedIds());
    }

    // -----------------------------------------------------------------------
    // Metrics
    // -----------------------------------------------------------------------

    private void recordChunkMetrics(long ok, long failed, long durationMs) {
        Counter.builder("batch.records.ok")
                .register(meterRegistry).increment(ok);
        Counter.builder("batch.records.failed")
                .register(meterRegistry).increment(failed);
        Timer.builder("batch.chunk.duration")
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

    // -----------------------------------------------------------------------
    // Result record
    // -----------------------------------------------------------------------

    /**
     * Immutable result of a single chunk execution.
     */
    public record BatchResult(
        String  batchId,
        String  nodeId,
        String  outcome,       // COMPLETED | COMPLETED_WITH_ERRORS | EMPTY | FAILED | SKIPPED
        int     totalRead,
        int     totalOk,
        int     totalFailed,
        long    durationMs,
        String  errorMessage
    ) {
        static BatchResult completed(String b, String n, int read, int ok, int failed, long ms) {
            String status = failed > 0 ? "COMPLETED_WITH_ERRORS" : "COMPLETED";
            return new BatchResult(b, n, status, read, ok, failed, ms, null);
        }
        static BatchResult empty(String b, String n) {
            return new BatchResult(b, n, "EMPTY", 0, 0, 0, 0, null);
        }
        static BatchResult failed(String b, String n, int read, String err) {
            return new BatchResult(b, n, "FAILED", read, 0, read, 0, err);
        }
        static BatchResult skipped(String n) {
            return new BatchResult(null, n, "SKIPPED", 0, 0, 0, 0, "Already executing");
        }
    }
}
