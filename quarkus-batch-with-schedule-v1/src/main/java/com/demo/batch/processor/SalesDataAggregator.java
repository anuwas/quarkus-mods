package com.demo.batch.processor;

import com.demo.batch.entity.stg.StgSalesTransaction;
import com.demo.batch.exception.BatchProcessException;
import com.demo.batch.model.AggregatedSalesData;
import com.demo.batch.model.BatchContext;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

/**
 * Batch Processor — Data Aggregator.
 *
 * <p>Takes a chunk of raw {@link StgSalesTransaction} records and produces a
 * list of {@link AggregatedSalesData} objects grouped by:
 * {@code (transactionDate, productCode, region)}.
 *
 * <p>Aggregation operations performed per group:
 * <ul>
 *   <li>SUM of quantity</li>
 *   <li>SUM of revenue (quantity × unit_price)</li>
 *   <li>AVG unit price (revenue / total quantity)</li>
 *   <li>MIN and MAX unit price</li>
 *   <li>COUNT of transactions</li>
 * </ul>
 */
@ApplicationScoped
public class SalesDataAggregator {

    /**
     * Aggregate a chunk of raw transactions into summaries.
     *
     * @param transactions source records from the reader
     * @param context       current batch context for metrics
     * @return aggregated summaries ready for the writer
     */
    public List<AggregatedSalesData> aggregate(List<StgSalesTransaction> transactions,
                                                BatchContext context) {
        if (transactions == null || transactions.isEmpty()) {
            return Collections.emptyList();
        }

        Log.debugf("[%s] Aggregating chunk of %d transactions", context.runId, transactions.size());

        // Ordered map to keep output deterministic
        Map<AggregationKey, AggregatedSalesData> aggregationMap = new LinkedHashMap<>();
        int skipped = 0;

        for (StgSalesTransaction txn : transactions) {
            try {
                validate(txn);

                AggregationKey key = new AggregationKey(
                        txn.transactionDate, txn.productCode, txn.region);

                AggregatedSalesData summary = aggregationMap.computeIfAbsent(key,
                        k -> new AggregatedSalesData(
                                k.date(), k.productCode(), txn.productName,
                                txn.category, k.region()));

                summary.accumulate(txn.quantity, txn.unitPrice, txn.id);
                context.incrementProcessed(1);

            } catch (IllegalArgumentException e) {
                Log.warnf("[%s] Skipping invalid transaction id=%d: %s",
                        context.runId, txn.id, e.getMessage());
                context.incrementSkipped(1);
                skipped++;
            } catch (Exception e) {
                throw new BatchProcessException(
                        "Unexpected error processing transaction id=%d: %s"
                                .formatted(txn.id, e.getMessage()), e);
            }
        }

        // Finalize averages on all aggregates
        List<AggregatedSalesData> results = new ArrayList<>(aggregationMap.values());
        results.forEach(AggregatedSalesData::finalizeAggregation);

        Log.infof("[%s] Aggregated %d transactions → %d summaries (skipped=%d)",
                context.runId, transactions.size() - skipped, results.size(), skipped);

        return results;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void validate(StgSalesTransaction txn) {
        if (txn.transactionDate == null) throw new IllegalArgumentException("transactionDate is null");
        if (txn.productCode == null || txn.productCode.isBlank()) throw new IllegalArgumentException("productCode is blank");
        if (txn.region == null || txn.region.isBlank()) throw new IllegalArgumentException("region is blank");
        if (txn.quantity == null || txn.quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        if (txn.unitPrice == null || txn.unitPrice.signum() <= 0) throw new IllegalArgumentException("unitPrice must be > 0");
    }

    /**
     * Composite key for grouping: (date, productCode, region).
     */
    private record AggregationKey(
            java.time.LocalDate date,
            String productCode,
            String region) {}
}
