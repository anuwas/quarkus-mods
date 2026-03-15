package com.demo.batch.scheduler;

import com.demo.batch.config.BatchConfig;
import com.demo.batch.model.BatchContext;
import com.demo.batch.service.SalesBatchJobService;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Quarkus Scheduler — triggers the batch job on a configurable cron expression.
 *
 * <p>The cron can be overridden at runtime via the {@code BATCH_CRON} environment variable.
 * Set {@code BATCH_SCHEDULE_ENABLED=false} to disable the scheduled trigger entirely
 * (useful in dev/test environments — the job can still be triggered via REST).
 */
@ApplicationScoped
public class BatchScheduler {

    @Inject
    SalesBatchJobService batchJobService;

    @Inject
    BatchConfig batchConfig;

    /**
     * Scheduled trigger. The cron expression is read from application.properties
     * and supports environment variable override.
     */
    @Scheduled(cron = "{batch.schedule.cron}", identity = "sales-batch-job")
    public void triggerBatchJob(ScheduledExecution execution) {
        if (!batchConfig.schedule().enabled()) {
            Log.debugf("Scheduled batch trigger fired at %s but schedule is DISABLED — skipping.",
                    execution.getFireTime());
            return;
        }

        Log.infof("Scheduled batch trigger fired at %s", execution.getFireTime());

        try {
            BatchContext context = batchJobService.execute();
            Log.infof("Scheduled batch completed successfully. %s", context);
        } catch (IllegalStateException e) {
            Log.warnf("Scheduled batch skipped: %s", e.getMessage());
        } catch (Exception e) {
            Log.errorf(e, "Scheduled batch failed: %s", e.getMessage());
            // Do not rethrow — prevent scheduler from disabling on failure
        }
    }
}
