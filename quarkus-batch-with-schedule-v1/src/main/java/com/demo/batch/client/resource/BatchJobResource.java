package com.demo.batch.client.resource;

import com.demo.batch.entity.mn.MnBatchJobLog;
import com.demo.batch.entity.mn.MnDailySalesSummary;
import com.demo.batch.model.BatchContext;
import com.demo.batch.service.SalesBatchJobService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for batch job management and result inspection.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /batch/run       — trigger a manual batch execution</li>
 *   <li>GET  /batch/jobs      — list recent job logs</li>
 *   <li>GET  /batch/jobs/{id} — get a specific job log</li>
 *   <li>GET  /batch/summaries — query aggregated results from mndb</li>
 *   <li>GET  /batch/status    — application status</li>
 * </ul>
 */
@Path("/batch")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BatchJobResource {

    @Inject
    SalesBatchJobService batchJobService;

    // -------------------------------------------------------------------------
    // Trigger
    // -------------------------------------------------------------------------

    /**
     * Manually trigger a batch run (synchronous — waits for completion).
     */
    @POST
    @Path("/run")
    public Response triggerBatch() {
        Log.info("Manual batch trigger received via REST API");
        try {
            BatchContext context = batchJobService.execute();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("runId",            context.runId);
            result.put("jobName",          context.jobName);
            result.put("status",           context.resolveStatus());
            result.put("recordsRead",      context.getRecordsRead());
            result.put("recordsProcessed", context.getRecordsProcessed());
            result.put("recordsWritten",   context.getRecordsWritten());
            result.put("recordsSkipped",   context.getRecordsSkipped());
            result.put("recordsFailed",    context.getRecordsFailed());
            result.put("durationMs",       context.getDurationMs());

            return Response.ok(result).build();

        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            Log.errorf(e, "Batch run failed: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Batch job failed: " + e.getMessage()))
                    .build();
        }
    }

    // -------------------------------------------------------------------------
    // Job Logs
    // -------------------------------------------------------------------------

    @GET
    @Path("/jobs")
    public Response getRecentJobs(@QueryParam("limit") @DefaultValue("20") int limit) {
        List<MnBatchJobLog> jobs = MnBatchJobLog.findRecentJobs(limit);
        return Response.ok(jobs).build();
    }

    @GET
    @Path("/jobs/{runId}")
    public Response getJobByRunId(@PathParam("runId") String runId) {
        return MnBatchJobLog.findByRunId(runId)
                .map(job -> Response.ok(job).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Job not found: " + runId)).build());
    }

    // -------------------------------------------------------------------------
    // Summaries
    // -------------------------------------------------------------------------

    @GET
    @Path("/summaries")
    public Response getSummaries(
            @QueryParam("from") String from,
            @QueryParam("to")   String to) {

        LocalDate fromDate = from != null ? LocalDate.parse(from) : LocalDate.now().minusDays(30);
        LocalDate toDate   = to   != null ? LocalDate.parse(to)   : LocalDate.now();

        List<MnDailySalesSummary> summaries = MnDailySalesSummary.findByDateRange(fromDate, toDate);
        return Response.ok(summaries).build();
    }

    // -------------------------------------------------------------------------
    // Status
    // -------------------------------------------------------------------------

    @GET
    @Path("/status")
    public Response status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("app",     "quarkus-batch-demo");
        status.put("version", "1.0.0");
        status.put("time",    java.time.LocalDateTime.now().toString());

        long pendingInStg  = com.demo.batch.entity.stg.StgSalesTransaction.countPending();
        long summariesInMn = MnDailySalesSummary.count();
        long jobsRun       = MnBatchJobLog.count();

        status.put("stg.pendingTransactions", pendingInStg);
        status.put("mn.summaryRecords",       summariesInMn);
        status.put("mn.jobsRun",              jobsRun);

        return Response.ok(status).build();
    }
}
