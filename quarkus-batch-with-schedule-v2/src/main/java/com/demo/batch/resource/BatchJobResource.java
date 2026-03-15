package com.demo.batch.resource;

import com.demo.batch.entity.mn.MnBatchJobLog;
import com.demo.batch.entity.mn.MnDailySalesSummary;
import com.demo.batch.entity.stg.StgSalesTransaction;
import com.demo.batch.model.BatchContext;
import com.demo.batch.service.SalesBatchJobService;
import com.demo.batch.util.BatchUtil;
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
 * REST API V2 — batch management and result inspection.
 * Added throughput and chunk stats to the run response.
 */
@Path("/batch")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BatchJobResource {

    @Inject SalesBatchJobService batchJobService;

    @POST
    @Path("/run")
    public Response triggerBatch() {
        Log.info("Manual batch trigger via REST");
        try {
            BatchContext ctx = batchJobService.execute();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("runId",            ctx.runId);
            result.put("jobName",          ctx.jobName);
            result.put("status",           ctx.resolveStatus());
            result.put("recordsRead",      ctx.getRecordsRead());
            result.put("recordsProcessed", ctx.getRecordsProcessed());
            result.put("recordsWritten",   ctx.getRecordsWritten());
            result.put("recordsSkipped",   ctx.getRecordsSkipped());
            result.put("recordsFailed",    ctx.getRecordsFailed());
            result.put("chunksProcessed",  ctx.getChunksProcessed());
            result.put("durationMs",       ctx.getDurationMs());
            result.put("durationHuman",    BatchUtil.formatDuration(ctx.getDurationMs()));
            result.put("throughputPerSec", String.format("%.0f", ctx.throughputPerSecond()));

            return Response.ok(result).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            Log.errorf(e, "Batch run failed: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Batch failed: " + e.getMessage())).build();
        }
    }

    @GET
    @Path("/jobs")
    public Response getRecentJobs(@QueryParam("limit") @DefaultValue("20") int limit) {
        return Response.ok(MnBatchJobLog.findRecentJobs(limit)).build();
    }

    @GET
    @Path("/jobs/{runId}")
    public Response getJobByRunId(@PathParam("runId") String runId) {
        return MnBatchJobLog.findByRunId(runId)
                .map(j -> Response.ok(j).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Job not found: " + runId)).build());
    }

    @GET
    @Path("/summaries")
    public Response getSummaries(
            @QueryParam("from") String from,
            @QueryParam("to")   String to) {
        LocalDate fromDate = from != null ? LocalDate.parse(from) : LocalDate.now().minusDays(30);
        LocalDate toDate   = to   != null ? LocalDate.parse(to)   : LocalDate.now();
        return Response.ok(MnDailySalesSummary.findByDateRange(fromDate, toDate)).build();
    }

    @GET
    @Path("/status")
    public Response status() {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("app",     "quarkus-batch-demo");
        s.put("version", "2.0.0");
        s.put("time",    java.time.LocalDateTime.now().toString());
        s.put("stg.pendingTransactions", StgSalesTransaction.count("status", StgSalesTransaction.TransactionStatus.PENDING));
        s.put("stg.approxTotalRows",     StgSalesTransaction.approximatePendingCount());
        s.put("mn.summaryRecords",       MnDailySalesSummary.count());
        s.put("mn.jobsRun",              MnBatchJobLog.count());
        return Response.ok(s).build();
    }
}
