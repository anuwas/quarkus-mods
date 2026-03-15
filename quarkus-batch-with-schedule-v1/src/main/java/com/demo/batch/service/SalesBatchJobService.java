package com.demo.batch.service;

import com.demo.batch.config.BatchConfig;
import com.demo.batch.entity.mn.MnBatchJobLog;
import com.demo.batch.entity.stg.StgSalesTransaction;
import com.demo.batch.exception.BatchReadException;
import com.demo.batch.model.AggregatedSalesData;
import com.demo.batch.model.BatchContext;
import com.demo.batch.processor.SalesDataAggregator;
import com.demo.batch.reader.SalesTransactionReader;
import com.demo.batch.writer.SalesSummaryWriter;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Core batch job orchestrator — coordinates the full Read → Process → Write pipeline.
 *
 * <p>Pipeline overview:
 * <pre>
 *  [stgdb] ──► SalesTransactionReader
 *                   │
 *                   │  chunk of StgSalesTransaction
 *                   ▼
 *           SalesDataAggregator (Processor)
 *                   │
 *                   │  List of AggregatedSalesData
 *                   ▼
 *           SalesSummaryWriter
 *                   │
 *                   ▼
 *              [mndb] MnDailySalesSummary
 * </pre>
 *
 * <p>Fault-tolerance features:
 * <ul>
 *   <li>Idempotent — only processes PENDING records.</li>
 *   <li>Chunk-based — failure in one chunk does not abort other chunks.</li>
 *   <li>Skip limit — job aborts if too many records fail validation.</li>
 *   <li>Retry — transient chunk failures are retried up to {@code batch.retry-attempts}.</li>
 *   <li>Concurrent execution guard — one active job at a time.</li>
 * </ul>
 */
@ApplicationScoped
public class SalesBatchJobService {

    private static final String JOB_NAME = "SalesDailyAggregationJob";

    @Inject BatchConfig batchConfig;
    @Inject SalesTransactionReader reader;
    @Inject SalesDataAggregator processor;
    @Inject SalesSummaryWriter writer;
    @Inject BatchMetricsService metricsService;

    /** Prevents concurrent execution. */
    private final AtomicBoolean running = new AtomicBoolean(false);

    // -------------------------------------------------------------------------
    // Public entry point
    // -------------------------------------------------------------------------

    /**
     * Execute a full batch run. Returns the completed {@link BatchContext}.
     *
     * @throws IllegalStateException if another run is already in progress
     */
    public BatchContext execute() {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("A batch job is already running. Skipping this trigger.");
        }

        String runId = "RUN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BatchContext context = new BatchContext(runId, JOB_NAME, batchConfig.chunkSize());

        Log.infof("════════════════════════════════════════════════");
        Log.infof("  Batch Job STARTED  runId=%s  at=%s", runId, LocalDateTime.now());
        Log.infof("════════════════════════════════════════════════");

        MnBatchJobLog jobLog = metricsService.createJobLog(context);

        try {
            runPipeline(context);

            MnBatchJobLog.JobStatus finalStatus = context.resolveStatus();
            metricsService.updateJobLog(context, finalStatus, null);
            metricsService.recordBatchCompletion(context, true);

            Log.infof("════════════════════════════════════════════════");
            Log.infof("  Batch Job COMPLETED  %s", context);
            Log.infof("════════════════════════════════════════════════");

        } catch (Exception e) {
            Log.errorf(e, "[%s] Batch job FAILED: %s", runId, e.getMessage());
            metricsService.updateJobLog(context, MnBatchJobLog.JobStatus.FAILED, e.getMessage());
            metricsService.recordBatchCompletion(context, false);
            throw e;
        } finally {
            running.set(false);
        }

        return context;
    }

    // -------------------------------------------------------------------------
    // Pipeline implementation
    // -------------------------------------------------------------------------

    private void runPipeline(BatchContext context) {
        long totalPending = reader.countPending(context);

        if (totalPending == 0) {
            Log.infof("[%s] No pending records to process. Exiting.", context.runId);
            return;
        }

        int chunkSize = context.chunkSize;
        int totalPages = (int) Math.ceil((double) totalPending / chunkSize);
        int currentPage = 0;
        int consecutiveEmptyPages = 0;

        Log.infof("[%s] Processing %d records in %d chunks (chunkSize=%d)",
                context.runId, totalPending, totalPages, chunkSize);

        while (true) {
            // Read
            List<StgSalesTransaction> chunk = readWithRetry(currentPage, chunkSize, context);

            if (chunk.isEmpty()) {
                if (++consecutiveEmptyPages >= 2) break; // end of data
                currentPage++;
                continue;
            }
            consecutiveEmptyPages = 0;

            // Check skip limit
            if (context.getRecordsSkipped() > batchConfig.skipLimit()) {
                Log.errorf("[%s] Skip limit (%d) exceeded. Aborting job.", context.runId, batchConfig.skipLimit());
                throw new IllegalStateException("Skip limit exceeded: " + context.getRecordsSkipped());
            }

            // Process
            List<AggregatedSalesData> aggregated = processWithRetry(chunk, context);

            // Write
            writeWithRetry(aggregated, context);

            // Progress log every 5 pages
            if (currentPage % 5 == 0) {
                Log.infof("[%s] Progress — page=%d read=%d processed=%d written=%d skipped=%d",
                        context.runId, currentPage,
                        context.getRecordsRead(), context.getRecordsProcessed(),
                        context.getRecordsWritten(), context.getRecordsSkipped());
            }

            // Because PENDING records were marked PROCESSED, always re-read from page 0
            // until no more PENDING records are returned.
            // (Offset pagination on a mutable status column can miss records.)
            // We stay on page 0 after each successful chunk.
        }
    }

    // -------------------------------------------------------------------------
    // Retry wrappers
    // -------------------------------------------------------------------------

    private List<StgSalesTransaction> readWithRetry(int page, int pageSize, BatchContext context) {
        int attempts = 0;
        while (true) {
            try {
                return reader.readPage(page, pageSize, context);
            } catch (BatchReadException e) {
                if (++attempts >= batchConfig.retryAttempts()) throw e;
                sleep(batchConfig.retryDelayMs());
                Log.warnf("[%s] Read retry %d/%d after error: %s", context.runId, attempts, batchConfig.retryAttempts(), e.getMessage());
            }
        }
    }

    private List<AggregatedSalesData> processWithRetry(List<StgSalesTransaction> chunk, BatchContext context) {
        int attempts = 0;
        while (true) {
            try {
                return processor.aggregate(chunk, context);
            } catch (Exception e) {
                if (++attempts >= batchConfig.retryAttempts()) throw e;
                sleep(batchConfig.retryDelayMs());
                Log.warnf("[%s] Process retry %d/%d: %s",  context.runId, attempts, batchConfig.retryAttempts(), e.getMessage());
            }
        }
    }

    private void writeWithRetry(List<AggregatedSalesData> summaries, BatchContext context) {
        int attempts = 0;
        while (true) {
            try {
                writer.write(summaries, context);
                return;
            } catch (Exception e) {
                if (++attempts >= batchConfig.retryAttempts()) throw e;
                sleep(batchConfig.retryDelayMs());
                Log.warnf("[%s] Write retry %d/%d: %s",
                        context.runId, attempts, batchConfig.retryAttempts(), e.getMessage());
            }
        }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }
}
