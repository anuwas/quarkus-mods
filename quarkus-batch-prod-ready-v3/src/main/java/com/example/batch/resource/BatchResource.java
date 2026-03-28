package com.example.batch.resource;

import com.example.batch.config.BatchProperties;
import com.example.batch.config.RecordStatus;
import com.example.batch.dto.BatchResult;
import com.example.batch.repository.main.BatchExecutionLogRepository;
import com.example.batch.repository.main.CentreRepository;
import com.example.batch.repository.main.StudentRepository;
import com.example.batch.repository.stg.StagingSynchLogRepository;
import com.example.batch.service.CentreBatchProcessingService;
import com.example.batch.service.StudentBatchProcessingService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Production REST API for the batch processor.
 *
 * Endpoints:
 *   POST /batch/trigger/centres      — trigger one chunk of centre records only
 *   POST /batch/trigger/students     — trigger one chunk of student records only
 *   GET  /batch/status               — live counts per status in stgDB
 *   GET  /batch/executions           — recent batch execution logs
 *   GET  /batch/executions/{id}      — single execution detail
 *   GET  /batch/results/centres      — centre records from mainDB
 *   GET  /batch/results/students     — student records from mainDB
 *   POST /batch/admin/reset          — reset orphaned PROCESSING rows to PENDING
 */
@Path("/batch")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Batch Processor", description = "Trigger, monitor, and manage the batch pipeline")
public class BatchResource {

    @Inject CentreBatchProcessingService  centreService;
    @Inject StudentBatchProcessingService studentService;
    @Inject @io.quarkus.hibernate.orm.PersistenceUnit("stgdb") StagingSynchLogRepository stagingRepo;
    @Inject CentreRepository              centreRepo;
    @Inject StudentRepository             studentRepo;
    @Inject BatchExecutionLogRepository   logRepo;
    @Inject BatchProperties               props;


    // -----------------------------------------------------------------------
    // Trigger — centres only
    // -----------------------------------------------------------------------

    @POST
    @Path("/trigger/centres")
    @Operation(
        summary     = "Trigger one chunk of centre records",
        description = "Processes the next available chunk of PENDING centre records only. " +
                      "Idempotent — safe to call even if no records are pending."
    )
    public Response triggerCentres() {
        Log.infof("Manual batch trigger received (centres).");
        try {
            BatchResult result = centreService.processNextChunk();
            return Response.ok(resultToMap(result)).build();
        } catch (Exception e) {
            Log.errorf(e, "Manual centre trigger failed: %s", e.getMessage());
            return Response.serverError()
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    // -----------------------------------------------------------------------
    // Trigger — students only
    // -----------------------------------------------------------------------

    @POST
    @Path("/trigger/students")
    @Operation(
        summary     = "Trigger one chunk of student records",
        description = "Processes the next available chunk of PENDING student records only. " +
                      "Idempotent — safe to call even if no records are pending."
    )
    public Response triggerStudents() {
        Log.infof("Manual batch trigger received (students).");
        try {
            BatchResult result = studentService.processNextChunk();
            return Response.ok(resultToMap(result)).build();
        } catch (Exception e) {
            Log.errorf(e, "Manual student trigger failed: %s", e.getMessage());
            return Response.serverError()
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    // -----------------------------------------------------------------------
    // Status
    // -----------------------------------------------------------------------

    @GET
    @Path("/status")
    @Operation(summary = "Live stgDB status counts")
    public Response status() {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("node",         centreService.getNodeId());
        s.put("app",          "quarkus-batch-prod");
        s.put("time",         java.time.LocalDateTime.now().toString());
        s.put("config.chunkSize",              props.chunkSize());
        s.put("config.centre.cron",             props.schedule().centre().cron());
        s.put("config.centre.enabled",          props.schedule().centre().enabled());
        s.put("config.student.cron",            props.schedule().student().cron());
        s.put("config.student.enabled",         props.schedule().student().enabled());
        s.put("stg.pending",    stagingRepo.countByStatus(RecordStatus.PENDING));
        s.put("stg.processing", stagingRepo.countByStatus(RecordStatus.PROCESSING));
        s.put("stg.completed",  stagingRepo.countByStatus(RecordStatus.COMPLETED));
        s.put("stg.failed",     stagingRepo.countByStatus(RecordStatus.FAILED));
        s.put("main.centres",   centreRepo.count());
        s.put("main.students",  studentRepo.count());
        s.put("main.execLogs",  logRepo.count());
        return Response.ok(s).build();
    }

    // -----------------------------------------------------------------------
    // Execution logs
    // -----------------------------------------------------------------------

    @GET
    @Path("/executions")
    @Operation(summary = "Recent batch execution logs")
    public Response executions(@QueryParam("limit") @DefaultValue("20")
                               @Min(1) @Max(200) int limit) {
        return Response.ok(logRepo.findRecent(limit)).build();
    }

    @GET
    @Path("/executions/{batchId}")
    @Operation(summary = "Single batch execution detail")
    public Response execution(@PathParam("batchId") String batchId) {
        return logRepo.findByBatchId(batchId)
                .map(log -> Response.ok(log).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Not found: " + batchId)).build());
    }

    // -----------------------------------------------------------------------
    // Results
    // -----------------------------------------------------------------------

    @GET
    @Path("/results/centres")
    @Operation(summary = "Centre records from mainDB")
    public Response centreResults(
            @QueryParam("limit") @DefaultValue("100") @Min(1) @Max(1000) int limit) {
        return Response.ok(centreRepo.listAll().stream().limit(limit).toList()).build();
    }

    @GET
    @Path("/results/students")
    @Operation(summary = "Student records from mainDB")
    public Response studentResults(
            @QueryParam("limit") @DefaultValue("100") @Min(1) @Max(1000) int limit) {
        return Response.ok(studentRepo.listAll().stream().limit(limit).toList()).build();
    }

    // -----------------------------------------------------------------------
    // Admin
    // -----------------------------------------------------------------------

    @POST
    @Path("/admin/reset-processing")
    @Operation(
        summary     = "Reset orphaned PROCESSING records",
        description = "Resets PROCESSING records (from crashed nodes) back to PENDING. " +
                      "Pass nodeId to reset only that node's records, or omit to reset all."
    )
    public Response resetProcessing(@QueryParam("nodeId") String nodeId) {
        int count = stagingRepo.resetOrphanedProcessingRecords(nodeId);
        return Response.ok(Map.of(
                "reset", count,
                "nodeId", nodeId != null ? nodeId : "ALL"
        )).build();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Map<String, Object> resultToMap(BatchResult r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("batchId",      r.batchId());
        m.put("nodeId",       r.nodeId());
        m.put("outcome",      r.outcome());
        m.put("totalRead",    r.totalRead());
        m.put("totalOk",      r.totalOk());
        m.put("totalFailed",  r.totalFailed());
        m.put("durationMs",   r.durationMs());
        if (r.errorMessage() != null) m.put("error", r.errorMessage());
        if (r.durationMs() > 0) {
            m.put("throughputRps", String.format("%.0f", r.totalOk() * 1000.0 / r.durationMs()));
        }
        return m;
    }
}
