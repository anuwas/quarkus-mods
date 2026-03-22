package com.example.batch.service;

import com.example.batch.entity.main.AggregatedResult;
import com.example.batch.entity.stg.StagingRecord;
import com.example.batch.exception.RecordValidationException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

/**
 * Stateless service that validates and aggregates a chunk of StagingRecords
 * into a list of AggregatedResult objects.
 *
 * Validation rules applied per record:
 *   - productName must be non-blank
 *   - quantity must be > 0
 *
 * Aggregation key: productName
 * Per-group metrics: totalQuantity, transactionCount
 *
 * This class is intentionally free of @Transactional — it performs pure in-memory
 * computation and does not touch the database.
 */
@ApplicationScoped
public class ChunkAggregator {

    /**
     * Result of processing one chunk.
     *
     * @param aggregated   AggregatedResult objects ready for upsert into mainDB
     * @param successIds   IDs of StagingRecords that passed validation
     * @param failedIds    IDs that failed validation, with their error messages
     */
    public record ChunkResult(
        List<AggregatedResult> aggregated,
        List<Long>             successIds,
        Map<Long, String>      failedIds
    ) {}

    // -----------------------------------------------------------------------
    // Core method
    // -----------------------------------------------------------------------

    /**
     * Validate and aggregate a chunk of staging records.
     *
     * @param records   the claimed PROCESSING records
     * @param batchId   current batch run identifier (written into AggregatedResult)
     * @param nodeId    current node identifier
     * @return          ChunkResult with aggregated data + success/failure IDs
     */
    public ChunkResult process(List<StagingRecord> records, String batchId, String nodeId) {
        Map<String, AggregatedResult> aggregationMap = new LinkedHashMap<>();
        List<Long>         successIds = new ArrayList<>(records.size());
        Map<Long, String>  failedIds  = new LinkedHashMap<>();

        for (StagingRecord record : records) {
            try {
                validate(record);
                String key = record.productName;

                AggregatedResult agg = aggregationMap.computeIfAbsent(key, k -> newResult(k, batchId, nodeId));

                accumulate(agg, record);
                successIds.add(record.id);

            } catch (RecordValidationException e) {
                Log.warnf("Validation failed for record id=%d: %s", record.id, e.getMessage());
                failedIds.put(record.id, e.getMessage());
            } catch (Exception e) {
                Log.errorf(e, "Unexpected error processing record id=%d", record.id);
                failedIds.put(record.id, "Internal error: " + e.getMessage());
            }
        }


        Log.debugf("Chunk aggregation complete: %d ok, %d failed, %d groups",
            successIds.size(), failedIds.size(), aggregationMap.size());

        return new ChunkResult(
            new ArrayList<>(aggregationMap.values()),
            successIds,
            failedIds
        );
    }

    // -----------------------------------------------------------------------
    // Validation
    // -----------------------------------------------------------------------

    private void validate(StagingRecord r) {
        List<String> errors = new ArrayList<>();

        if (isBlank(r.productName))   errors.add("productName is blank");
        if (r.quantity == null || r.quantity <= 0)
            errors.add("quantity must be > 0 (got: " + r.quantity + ")");

        if (!errors.isEmpty()) {
            throw new RecordValidationException( "Record id=" + r.id + " failed validation: " + String.join("; ", errors));
        }
    }

    // -----------------------------------------------------------------------
    // Aggregation helpers
    // -----------------------------------------------------------------------

    private AggregatedResult newResult(String productName, String batchId, String nodeId) {
        AggregatedResult a = new AggregatedResult();
        a.productName         = productName;
        a.totalQuantity       = 0L;
        a.transactionCount    = 0L;
        a.batchId             = batchId;
        a.nodeId              = nodeId;
        return a;
    }

    private void accumulate(AggregatedResult agg, StagingRecord r) {
        agg.totalQuantity      += r.quantity;
        agg.transactionCount   += 1;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
