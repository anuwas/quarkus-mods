package com.demo.batch.model;

import com.demo.batch.entity.mn.MnBatchJobLog;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Immutable batch execution context passed through reader → processor → writer pipeline.
 * Uses atomic counters for thread-safe metric tracking.
 */
public final class BatchContext {

    public final String runId;
    public final String jobName;
    public final int chunkSize;
    public final LocalDateTime startedAt;

    private final AtomicLong recordsRead = new AtomicLong(0);
    private final AtomicLong recordsProcessed = new AtomicLong(0);
    private final AtomicLong recordsWritten = new AtomicLong(0);
    private final AtomicLong recordsSkipped = new AtomicLong(0);
    private final AtomicLong recordsFailed = new AtomicLong(0);

    public BatchContext(String runId, String jobName, int chunkSize) {
        this.runId = runId;
        this.jobName = jobName;
        this.chunkSize = chunkSize;
        this.startedAt = LocalDateTime.now();
    }

    public void incrementRead(long count)      { recordsRead.addAndGet(count); }
    public void incrementProcessed(long count) { recordsProcessed.addAndGet(count); }
    public void incrementWritten(long count)   { recordsWritten.addAndGet(count); }
    public void incrementSkipped(long count)   { recordsSkipped.addAndGet(count); }
    public void incrementFailed(long count)    { recordsFailed.addAndGet(count); }

    public long getRecordsRead()      { return recordsRead.get(); }
    public long getRecordsProcessed() { return recordsProcessed.get(); }
    public long getRecordsWritten()   { return recordsWritten.get(); }
    public long getRecordsSkipped()   { return recordsSkipped.get(); }
    public long getRecordsFailed()    { return recordsFailed.get(); }

    public long getDurationMs() {
        return java.time.Duration.between(startedAt, LocalDateTime.now()).toMillis();
    }

    public MnBatchJobLog.JobStatus resolveStatus() {
        if (recordsFailed.get() > 0 || recordsSkipped.get() > 0) {
            return MnBatchJobLog.JobStatus.COMPLETED_WITH_ERRORS;
        }
        return MnBatchJobLog.JobStatus.COMPLETED;
    }

    @Override
    public String toString() {
        return ("BatchContext{runId='%s', read=%d, processed=%d, written=%d, skipped=%d, failed=%d, durationMs=%d}")
                .formatted(runId, recordsRead.get(), recordsProcessed.get(),
                        recordsWritten.get(), recordsSkipped.get(), recordsFailed.get(), getDurationMs());
    }
}
