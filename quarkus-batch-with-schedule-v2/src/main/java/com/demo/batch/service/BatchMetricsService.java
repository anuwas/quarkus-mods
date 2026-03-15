package com.demo.batch.service;

import com.demo.batch.entity.mn.MnBatchJobLog;
import com.demo.batch.model.BatchContext;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

/**
 * Centralised service for persisting batch job logs to mndb
 * and publishing Micrometer metrics to Prometheus.
 */
@ApplicationScoped
public class BatchMetricsService {

    @Inject
    MeterRegistry meterRegistry;

    // -------------------------------------------------------------------------
    // Job log persistence (mndb)
    // -------------------------------------------------------------------------

    @Transactional
    public MnBatchJobLog createJobLog(BatchContext context) {
        MnBatchJobLog log = new MnBatchJobLog();
        log.runId     = context.runId;
        log.jobName   = context.jobName;
        log.status    = MnBatchJobLog.JobStatus.STARTED;
        log.startedAt = context.startedAt;
        log.chunkSize = context.chunkSize;
        log.persist();
        return log;
    }

    @Transactional
    public void updateJobLog(BatchContext context, MnBatchJobLog.JobStatus status, String errorMessage) {
        MnBatchJobLog.findByRunId(context.runId).ifPresentOrElse(log -> {
            log.status           = status;
            log.completedAt      = LocalDateTime.now();
            log.recordsRead      = context.getRecordsRead();
            log.recordsProcessed = context.getRecordsProcessed();
            log.recordsWritten   = context.getRecordsWritten();
            log.recordsSkipped   = context.getRecordsSkipped();
            log.recordsFailed    = context.getRecordsFailed();
            log.durationMs       = context.getDurationMs();
            log.errorMessage     = errorMessage;
        }, () -> Log.warnf("No job log found for runId=%s", context.runId));
    }

    // -------------------------------------------------------------------------
    // Micrometer metrics
    // -------------------------------------------------------------------------

    public void recordBatchCompletion(BatchContext context, boolean success) {
        Counter.builder("batch.jobs.total")
                .tag("job", context.jobName)
                .tag("status", success ? "success" : "failure")
                .register(meterRegistry)
                .increment();

        meterRegistry.gauge("batch.records.read",
                io.micrometer.core.instrument.Tags.of("job", context.jobName),
                context.getRecordsRead());

        meterRegistry.gauge("batch.records.written",
                io.micrometer.core.instrument.Tags.of("job", context.jobName),
                context.getRecordsWritten());

        Timer.builder("batch.duration.ms")
                .tag("job", context.jobName)
                .register(meterRegistry)
                .record(context.getDurationMs(), java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
