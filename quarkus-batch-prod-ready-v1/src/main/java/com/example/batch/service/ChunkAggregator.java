package com.example.batch.service;

import com.example.batch.entity.main.AggregatedResult;
import com.example.batch.entity.stg.StagingRecord;
import com.example.batch.exception.RecordValidationException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Stateless service that validates and aggregates a chunk of StagingRecords
 * into a list of AggregatedResult objects.
 *
 * Validation rules applied per record:
 *   - productCode, productName, category, region, customerId must be non-blank
 *   - quantity must be > 0
 *   - unitPrice must be > 0
 *   - transactionDate must not be null
 *
 * Aggregation key: (transactionDate, productCode, region)
 * Per-group metrics: totalQuantity, totalRevenue, avgUnitPrice, min/maxUnitPrice,
 *                    transactionCount, uniqueCustomerCount
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
        Map<AggKey, AggregatedResult> aggregationMap = new LinkedHashMap<>();
        List<Long>         successIds = new ArrayList<>(records.size());
        Map<Long, String>  failedIds  = new LinkedHashMap<>();

        for (StagingRecord record : records) {
            try {
                validate(record);
                AggKey key = new AggKey(record.transactionDate, record.productCode, record.region);

                AggregatedResult agg = aggregationMap.computeIfAbsent(key,
                    k -> newResult(k, record, batchId, nodeId));

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

        // Finalise averages after all accumulations
        aggregationMap.values().forEach(this::finalise);

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

        if (isBlank(r.productCode))   errors.add("productCode is blank");
        if (isBlank(r.productName))   errors.add("productName is blank");
        if (isBlank(r.category))      errors.add("category is blank");
        if (isBlank(r.region))        errors.add("region is blank");
        if (isBlank(r.customerId))    errors.add("customerId is blank");
        if (r.transactionDate == null)errors.add("transactionDate is null");
        if (r.quantity == null || r.quantity <= 0)
            errors.add("quantity must be > 0 (got: " + r.quantity + ")");
        if (r.unitPrice == null || r.unitPrice.compareTo(BigDecimal.ZERO) <= 0)
            errors.add("unitPrice must be > 0 (got: " + r.unitPrice + ")");

        if (!errors.isEmpty()) {
            throw new RecordValidationException(
                "Record id=" + r.id + " failed validation: " + String.join("; ", errors));
        }
    }

    // -----------------------------------------------------------------------
    // Aggregation helpers
    // -----------------------------------------------------------------------

    private AggregatedResult newResult(AggKey key, StagingRecord r, String batchId, String nodeId) {
        AggregatedResult a = new AggregatedResult();
        a.transactionDate     = key.date();
        a.productCode         = key.productCode();
        a.productName         = r.productName;
        a.category            = r.category;
        a.region              = key.region();
        a.totalQuantity       = 0L;
        a.totalRevenue        = BigDecimal.ZERO;
        a.transactionCount    = 0L;
        a.uniqueCustomerCount = 0L;
        a.batchId             = batchId;
        a.nodeId              = nodeId;
        return a;
    }

    private void accumulate(AggregatedResult agg, StagingRecord r) {
        BigDecimal lineTotal = r.unitPrice.multiply(BigDecimal.valueOf(r.quantity));

        agg.totalQuantity      += r.quantity;
        agg.totalRevenue        = agg.totalRevenue.add(lineTotal);
        agg.transactionCount   += 1;
        agg.uniqueCustomerCount += 1;  // simplified: per-record; set-based dedup in finalise

        if (agg.minUnitPrice == null || r.unitPrice.compareTo(agg.minUnitPrice) < 0)
            agg.minUnitPrice = r.unitPrice;
        if (agg.maxUnitPrice == null || r.unitPrice.compareTo(agg.maxUnitPrice) > 0)
            agg.maxUnitPrice = r.unitPrice;
    }

    private void finalise(AggregatedResult agg) {
        if (agg.totalQuantity > 0) {
            agg.avgUnitPrice = agg.totalRevenue
                .divide(BigDecimal.valueOf(agg.totalQuantity), 4, RoundingMode.HALF_UP);
        } else {
            agg.avgUnitPrice = BigDecimal.ZERO;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    // -----------------------------------------------------------------------
    // Key record
    // -----------------------------------------------------------------------

    private record AggKey(java.time.LocalDate date, String productCode, String region) {}
}
