package com.demo.batch.service;

import com.demo.batch.config.BatchConfig;
import com.demo.batch.entity.mn.MnBatchJobLog;
import com.demo.batch.entity.stg.StgSalesTransaction;
import com.demo.batch.exception.BatchReadException;
import com.demo.batch.model.AggregatedSalesData;
import com.demo.batch.model.BatchContext;
import com.demo.batch.processor.SalesDataAggregator;
import com.demo.batch.reader.SalesTransactionReader;
import com.demo.batch.util.BatchUtil;
import com.demo.batch.writer.SalesSummaryWriter;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.context.ManagedExecutor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Batch Job Orchestrator V2 — high-volume Read → Process → Write pipeline.
 *
 * V2 improvements over V1:
 *
 *  1. KEYSET PAGINATION  — loop advances via cursor (lastSeenId) rather than page offset.
 *     Every read is O(1) regardless of dataset size.
 *
 *  2. NO STARTUP COUNT   — removed countPending(). The loop terminates when the reader
 *     returns an empty page. The O(1) approximation is used only for advisory logging.
 *
 *  3. PARALLEL WORKERS   — read-ahead fills a work queue; N ManagedExecutor threads
 *     each process + write their own chunk concurrently. Thread count is controlled
 *     by batch.thread-pool-size (default 8).
 *
 *  4. DYNAMIC SKIP LIMIT — expressed as a percentage of chunk size (batch.skip-limit-pct)
 *     so it scales automatically as chunk-size grows.
 *
 *  5. LARGER DEFAULTS    — chunk-size=1000, pool sizes raised to 30.
 *
 * Pipeline overview:
 * <pre>
 *   Main thread (coordinator):
 *     reads chunks sequentially via keyset cursor
 *          │
 *          ▼  submits to executor
 *   Worker threads (N parallel):
 *     SalesDataAggregator.aggregate()
 *          │
 *     SalesSummaryWriter.write()   ← each write is its own @Transactional boundary
 * </pre>
 */
@ApplicationScoped
public class SalesBatchJobService {

    private static final String JOB_NAME = "SalesDailyAggregationJob";

    @Inject BatchConfig          batchConfig;
    @Inject SalesTransactionReader reader;
    @Inject SalesDataAggregator    processor;
    @Inject SalesSummaryWriter     writer;
    @Inject BatchMetricsService    metricsService;
    @Inject ManagedExecutor        managedExecutor;

    /** Prevents concurrent execution. */
    private final AtomicBoolean running = new AtomicBoolean(false);

    // -------------------------------------------------------------------------
    // Public entry point
    // -------------------------------------------------------------------------

    public BatchContext execute() {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("A batch job is already running.");
        }

        String runId = "RUN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BatchContext context = new BatchContext(
                runId, JOB_NAME, batchConfig.chunkSize(), batchConfig.skipLimitPct());

        Log.infof("══════════════════════════════════════════════════════");
        Log.infof("  Batch Job V2 STARTED  runId=%s  at=%s", runId, LocalDateTime.now());
        Log.infof("  chunkSize=%d  threads=%d  skipLimitPct=%d%%",
                batchConfig.chunkSize(), batchConfig.threadPoolSize(), batchConfig.skipLimitPct());
        Log.infof("══════════════════════════════════════════════════════");

        metricsService.createJobLog(context);

        try {
            runPipeline(context);

            MnBatchJobLog.JobStatus status = context.resolveStatus();
            metricsService.updateJobLog(context, status, null);
            metricsService.recordBatchCompletion(context, true);

            Log.infof("══════════════════════════════════════════════════════");
            Log.infof("  Batch Job COMPLETED  %s", context);
            Log.infof("  Throughput: %.0f records/sec", context.throughputPerSecond());
            Log.infof("  Duration:   %s", BatchUtil.formatDuration(context.getDurationMs()));
            Log.infof("══════════════════════════════════════════════════════");

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
    // Pipeline — coordinator reads sequentially, workers run in parallel
    // -------------------------------------------------------------------------

    private void runPipeline(BatchContext context) {
        // Advisory estimate for logging only — never used as loop terminator
        long approxTotal = reader.approximatePendingCount(context);

        AtomicLong cursor = new AtomicLong(0L); // keyset cursor: highest id seen so far
        int windowSize    = batchConfig.threadPoolSize(); // number of in-flight futures
        int logInterval   = batchConfig.progressLogInterval();

        List<CompletableFuture<Void>> inFlight = new ArrayList<>(windowSize);

        while (true) {
            // Drain the window when full before reading more
            if (inFlight.size() >= windowSize) {
                awaitAll(inFlight, context);
                inFlight.clear();
            }

            // Read next chunk (coordinator thread — sequential, keyset-based)
            List<StgSalesTransaction> chunk =
                    readWithRetry(cursor.get(), context.chunkSize, context);

            if (chunk.isEmpty()) break; // no more PENDING rows — done

            // Advance the keyset cursor to the last id in this chunk
            long newCursor = chunk.get(chunk.size() - 1).id;
            cursor.set(newCursor);

            // Submit process+write as an async task
            List<StgSalesTransaction> chunkRef = List.copyOf(chunk);
            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    () -> processAndWrite(chunkRef, context), managedExecutor);
            inFlight.add(future);

            // Progress log
            if (context.getChunksProcessed() % logInterval == 0 && context.getChunksProcessed() > 0) {
                Log.infof("[%s] Progress — chunks=%d read=%d written=%d tps=%.0f cursorId=%d",
                        context.runId, context.getChunksProcessed(),
                        context.getRecordsRead(), context.getRecordsWritten(),
                        context.throughputPerSecond(), cursor.get());
            }
        }

        // Await any remaining in-flight futures
        if (!inFlight.isEmpty()) {
            awaitAll(inFlight, context);
        }
    }

    // -------------------------------------------------------------------------
    // Worker: process + write one chunk
    // -------------------------------------------------------------------------

    private void processAndWrite(List<StgSalesTransaction> chunk, BatchContext context) {
        List<AggregatedSalesData> aggregated = processWithRetry(chunk, context);
        writeWithRetry(aggregated, context);
    }

    // -------------------------------------------------------------------------
    // Await helpers
    // -------------------------------------------------------------------------

    private void awaitAll(List<CompletableFuture<Void>> futures, BatchContext context) {
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            Log.errorf(e, "[%s] One or more parallel workers failed: %s", context.runId, e.getMessage());
            throw new RuntimeException("Parallel chunk worker failure", e);
        }
    }

    // -------------------------------------------------------------------------
    // Retry wrappers
    // -------------------------------------------------------------------------

    private List<StgSalesTransaction> readWithRetry(long lastId, int pageSize, BatchContext ctx) {
        int attempts = 0;
        while (true) {
            try {
                return reader.readPage(lastId, pageSize, ctx);
            } catch (BatchReadException e) {
                if (++attempts >= batchConfig.retryAttempts()) throw e;
                sleep(batchConfig.retryDelayMs());
                Log.warnf("[%s] Read retry %d/%d: %s", ctx.runId, attempts, batchConfig.retryAttempts(), e.getMessage());
            }
        }
    }

    private List<AggregatedSalesData> processWithRetry(List<StgSalesTransaction> chunk, BatchContext ctx) {
        int attempts = 0;
        while (true) {
            try {
                return processor.aggregate(chunk, ctx);
            } catch (Exception e) {
                if (++attempts >= batchConfig.retryAttempts()) throw e;
                sleep(batchConfig.retryDelayMs());
                Log.warnf("[%s] Process retry %d/%d: %s", ctx.runId, attempts, batchConfig.retryAttempts(), e.getMessage());
            }
        }
    }

    private void writeWithRetry(List<AggregatedSalesData> summaries, BatchContext ctx) {
        int attempts = 0;
        while (true) {
            try {
                writer.write(summaries, ctx);
                return;
            } catch (Exception e) {
                if (++attempts >= batchConfig.retryAttempts()) throw e;
                sleep(batchConfig.retryDelayMs());
                Log.warnf("[%s] Write retry %d/%d: %s", ctx.runId, attempts, batchConfig.retryAttempts(), e.getMessage());
            }
        }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }
}
