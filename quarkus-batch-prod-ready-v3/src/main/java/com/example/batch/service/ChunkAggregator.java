package com.example.batch.service;

import com.example.batch.dto.ChunkResult;
import com.example.batch.entity.main.Product;
import com.example.batch.entity.stg.StagingSynchLog;
import com.example.batch.exception.RecordValidationException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

/**
 * Stateless service that validates and aggregates a chunk of StagingSynchLogs
 * into a list of Product objects.
 *
 * Validation rules applied per record:
 *   - tableName must be non-blank
 *   - tableReference must be non-blank
 *
 * Aggregation key: tableName
 * Per-group metrics: transactionCount
 *
 * This class is intentionally free of @Transactional — it performs pure in-memory
 * computation and does not touch the database.
 */
@ApplicationScoped
public class ChunkAggregator {


    // -----------------------------------------------------------------------
    // Core method
    // -----------------------------------------------------------------------

    /**
     * Validate and aggregate a chunk of staging records.
     *
     * @param records   the claimed PROCESSING records
     * @param batchId   current batch run identifier (written into Product)
     * @param nodeId    current node identifier
     * @return          ChunkResult with aggregated data + success/failure IDs
     */
    public ChunkResult process(List<StagingSynchLog> records, String batchId, String nodeId) {
        Map<String, Product> aggregationMap = new LinkedHashMap<>();
        List<Long>         successIds = new ArrayList<>(records.size());
        Map<Long, String>  failedIds  = new LinkedHashMap<>();

        for (StagingSynchLog record : records) {
            try {
                validate(record);
                String key = record.tableName;

                Product agg = aggregationMap.computeIfAbsent(key, k -> newResult(k, batchId, nodeId));

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

    private void validate(StagingSynchLog r) {
        List<String> errors = new ArrayList<>();

        if (isBlank(r.tableName))   errors.add("tableName is blank");
        if (isBlank(r.tableReference))
            errors.add("tableReference is blank");

        if (!errors.isEmpty()) {
            throw new RecordValidationException( "Record id=" + r.id + " failed validation: " + String.join("; ", errors));
        }
    }

    // -----------------------------------------------------------------------
    // Aggregation helpers
    // -----------------------------------------------------------------------

    private Product newResult(String tableName, String batchId, String nodeId) {
        Product a = new Product();
        a.tableName           = tableName;
        a.totalQuantity       = 0L;
        a.transactionCount    = 0L;
        a.batchId             = batchId;
        a.nodeId              = nodeId;
        return a;
    }

    private void accumulate(Product agg, StagingSynchLog r) {
        agg.transactionCount   += 1;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
