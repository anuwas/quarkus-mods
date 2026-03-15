package com.demo.batch.model;

import com.demo.batch.entity.mn.MnBatchJobLog;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe batch execution context passed through the reader → processor → writer pipeline.
 *
 * V2: all counters are AtomicLong so parallel chunk workers can update them concurrently
 * without external synchronization.  A volatile cursor tracks the highest id seen so far
 * for keyset pagination hand-off between the coordinator and workers.
 */
public final class BatchContext {

    public final String runId;
    public final String jobName;
    public final int chunkSize;
    public final int skipLimitPct;
    public final LocalDateTime startedAt;

    // Thread-safe counters
    private final AtomicLong recordsRead      = new AtomicLong(0);
    private final AtomicLong recordsProcessed = new AtomicLong(0);
    private final AtomicLong recordsWritten   = new AtomicLong(0);
    private final AtomicLong recordsSkipped   = new AtomicLong(0);
    private final AtomicLong recordsFailed    = new AtomicLong(0);
    private final AtomicLong chunksProcessed  = new AtomicLong(0);

    public BatchContext(String runId, String jobName, int chunkSize, int skipLimitPct) {
        this.runId        = runId;
        this.jobName      = jobName;
        this.chunkSize    = chunkSize;
        this.skipLimitPct = skipLimitPct;
        this.startedAt    = LocalDateTime.now();
    }

    public void incrementRead(long n)      { recordsRead.addAndGet(n); }
    public void incrementProcessed(long n) { recordsProcessed.addAndGet(n); }
    public void incrementWritten(long n)   { recordsWritten.addAndGet(n); }
    public void incrementSkipped(long n)   { recordsSkipped.addAndGet(n); }
    public void incrementFailed(long n)    { recordsFailed.addAndGet(n); }
    public void incrementChunks()          { chunksProcessed.incrementAndGet(); }

    public long getRecordsRead()      { return recordsRead.get(); }
    public long getRecordsProcessed() { return recordsProcessed.get(); }
    public long getRecordsWritten()   { return recordsWritten.get(); }
    public long getRecordsSkipped()   { return recordsSkipped.get(); }
    public long getRecordsFailed()    { return recordsFailed.get(); }
    public long getChunksProcessed()  { return chunksProcessed.get(); }

    public long getDurationMs() {
        return java.time.Duration.between(startedAt, LocalDateTime.now()).toMillis();
    }

    /**
     * Dynamic skip limit: a percentage of the chunk size.
     * E.g. skipLimitPct=5, chunkSize=1000 → skip limit = 50 per chunk.
     */
    public int effectiveSkipLimit() {
        return Math.max(1, (chunkSize * skipLimitPct) / 100);
    }

    public MnBatchJobLog.JobStatus resolveStatus() {
        if (recordsFailed.get() > 0 || recordsSkipped.get() > 0)
            return MnBatchJobLog.JobStatus.COMPLETED_WITH_ERRORS;
        return MnBatchJobLog.JobStatus.COMPLETED;
    }

    /** Throughput in records per second. */
    public double throughputPerSecond() {
        long ms = getDurationMs();
        return ms > 0 ? (recordsWritten.get() * 1000.0) / ms : 0.0;
    }

    @Override
    public String toString() {
        return ("BatchContext{runId='%s', chunks=%d, read=%d, processed=%d, " +
                "written=%d, skipped=%d, failed=%d, durationMs=%d, tps=%.0f}")
                .formatted(runId, chunksProcessed.get(), recordsRead.get(),
                        recordsProcessed.get(), recordsWritten.get(),
                        recordsSkipped.get(), recordsFailed.get(),
                        getDurationMs(), throughputPerSecond());
    }
}
