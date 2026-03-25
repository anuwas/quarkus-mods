package com.example.batch.scheduler;

import com.example.batch.config.BatchProperties;
import com.example.batch.dto.BatchResult;
import com.example.batch.service.BatchProcessingService;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Quarkus Scheduler — triggers one chunk per cron tick.
 *
 * Design principles:
 *   - One chunk per trigger: each execution processes at most one chunk.
 *     If there is more data, the next trigger picks up the next chunk.
 *     This keeps each unit of work small, bounded, and independently atomic.
 *
 *   - Skips gracefully: if a previous chunk is still running (same node),
 *     the trigger logs a warning and exits without blocking.
 *
 *   - Never throws: exceptions are caught so the Quarkus scheduler does not
 *     disable the trigger after a failure.
 *
 * To process records continuously without waiting for the next cron tick,
 * call POST /batch/trigger from an external orchestrator.
 */
@ApplicationScoped
public class BatchScheduler {

    @Inject BatchProcessingService batchService;
    @Inject BatchProperties        props;

    @Scheduled(
        cron     = "{batch.schedule.cron}",
        identity = "batch-chunk-processor"
    )
    public void onSchedule(ScheduledExecution execution) {
        if (!props.schedule().enabled()) {
            Log.debugf("Batch scheduler is disabled — skipping trigger at %s.",
                    execution.getFireTime());
            return;
        }

        Log.debugf("Batch scheduler fired at %s.", execution.getFireTime());

        try {
            BatchResult result = batchService.processNextChunk();

            switch (result.outcome()) {
                case "COMPLETED", "COMPLETED_WITH_ERRORS" ->
                    Log.infof("Scheduled chunk completed: ok=%d failed=%d duration=%d ms",
                            result.totalOk(), result.totalFailed(), result.durationMs());
                case "EMPTY" ->
                    Log.debugf("Scheduled chunk: no PENDING records.");
                case "SKIPPED" ->
                    Log.warnf("Scheduled chunk skipped — previous chunk still running.");
                case "FAILED" ->
                    Log.errorf("Scheduled chunk FAILED: %s", result.errorMessage());
                default ->
                    Log.warnf("Unexpected outcome: %s", result.outcome());
            }

        } catch (Exception e) {
            // Never let an exception propagate — Quarkus would disable the schedule
            Log.errorf(e, "Unexpected error in batch scheduler: %s", e.getMessage());
        }
    }
}
