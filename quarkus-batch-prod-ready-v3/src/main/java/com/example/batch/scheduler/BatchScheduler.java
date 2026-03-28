package com.example.batch.scheduler;

import com.example.batch.config.BatchProperties;
import com.example.batch.dto.BatchResult;
import com.example.batch.service.CentreBatchProcessingService;
import com.example.batch.service.StudentBatchProcessingService;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Quarkus Scheduler — triggers one chunk per entity type per its own cron tick.
 *
 * Design principles:
 *   - Separate cron schedules: centres and students each have their own
 *     independent cron expression and enabled flag, allowing different
 *     frequencies or disabling one without affecting the other.
 *
 *   - One chunk per entity per trigger: each execution processes at most one
 *     chunk.  If there is more data, the next trigger picks up the next chunk.
 *     This keeps each unit of work small, bounded, and independently atomic.
 *
 *   - Skips gracefully: if a previous chunk is still running (same node),
 *     the trigger logs a warning and exits without blocking.
 *
 *   - Never throws: exceptions are caught so the Quarkus scheduler does not
 *     disable the trigger after a failure.
 *
 * To process records continuously without waiting for the next cron tick,
 * call POST /batch/trigger (or /batch/trigger/centres, /batch/trigger/students)
 * from an external orchestrator.
 */
@ApplicationScoped
public class BatchScheduler {

    @Inject CentreBatchProcessingService  centreService;
    @Inject StudentBatchProcessingService studentService;
    @Inject BatchProperties               props;

    @Scheduled(
        cron     = "{batch.schedule.centre.cron}",
        identity = "batch-centre-chunk-processor"
    )
    public void onCentreSchedule(ScheduledExecution execution) {
        if (!props.schedule().centre().enabled()) {
            Log.debugf("Centre batch scheduler is disabled — skipping trigger at %s.",
                    execution.getFireTime());
            return;
        }

        Log.debugf("Centre batch scheduler fired at %s.", execution.getFireTime());
        processEntity("Centre", centreService);
    }

    @Scheduled(
        cron     = "{batch.schedule.student.cron}",
        identity = "batch-student-chunk-processor"
    )
    public void onStudentSchedule(ScheduledExecution execution) {
        if (!props.schedule().student().enabled()) {
            Log.debugf("Student batch scheduler is disabled — skipping trigger at %s.",
                    execution.getFireTime());
            return;
        }

        Log.debugf("Student batch scheduler fired at %s.", execution.getFireTime());
        processEntity("Student", studentService);
    }

    /**
     * Process one chunk for the given entity service and log the outcome.
     */
    private void processEntity(String label,
                               com.example.batch.service.BatchProcessingService service) {
        try {
            BatchResult result = service.processNextChunk();

            switch (result.outcome()) {
                case "COMPLETED", "COMPLETED_WITH_ERRORS" ->
                    Log.infof("[%s] Scheduled chunk completed: ok=%d failed=%d duration=%d ms",
                            label, result.totalOk(), result.totalFailed(), result.durationMs());
                case "EMPTY" ->
                    Log.debugf("[%s] Scheduled chunk: no PENDING records.", label);
                case "SKIPPED" ->
                    Log.warnf("[%s] Scheduled chunk skipped — previous chunk still running.", label);
                case "FAILED" ->
                    Log.errorf("[%s] Scheduled chunk FAILED: %s", label, result.errorMessage());
                default ->
                    Log.warnf("[%s] Unexpected outcome: %s", label, result.outcome());
            }

        } catch (Exception e) {
            // Never let an exception propagate — Quarkus would disable the schedule
            Log.errorf(e, "[%s] Unexpected error in batch scheduler: %s", label, e.getMessage());
        }
    }
}
