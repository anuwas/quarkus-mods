package com.demo.batch.processor;

import com.demo.batch.entity.stg.StgSalesTransaction;
import com.demo.batch.exception.BatchProcessException;
import com.demo.batch.model.AggregatedSalesData;
import com.demo.batch.model.BatchContext;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

/**
 * Batch Processor V2 — data aggregator (unchanged logic, thread-safe for parallel workers).
 *
 * This bean is {@code @ApplicationScoped} (singleton). The aggregate() method is stateless
 * — all state lives in the returned list — so multiple threads calling it concurrently
 * is safe without any synchronization.
 *
 * Aggregation key: (transactionDate, productCode, region)
 * Per-group metrics: SUM qty, SUM revenue, AVG/MIN/MAX unit price, COUNT transactions.
 */
@ApplicationScoped
public class SalesDataAggregator {

    public List<AggregatedSalesData> aggregate(List<StgSalesTransaction> transactions,
                                                BatchContext context) {
        if (transactions == null || transactions.isEmpty()) return Collections.emptyList();

        Map<AggregationKey, AggregatedSalesData> map = new LinkedHashMap<>();
        int skipped = 0;

        for (StgSalesTransaction txn : transactions) {
            try {
                validate(txn);

                AggregationKey key = new AggregationKey(
                        txn.transactionDate, txn.productCode, txn.region);

                map.computeIfAbsent(key, k -> new AggregatedSalesData(
                        k.date(), k.productCode(), txn.productName, txn.category, k.region()))
                   .accumulate(txn.quantity, txn.unitPrice, txn.id);

                context.incrementProcessed(1);

            } catch (IllegalArgumentException e) {
                Log.warnf("[%s] Skipping invalid txn id=%d: %s", context.runId, txn.id, e.getMessage());
                context.incrementSkipped(1);
                skipped++;

                if (context.getRecordsSkipped() > context.effectiveSkipLimit()) {
                    throw new BatchProcessException(
                            "Skip limit (%d) exceeded in chunk. Aborting chunk."
                                    .formatted(context.effectiveSkipLimit()));
                }
            }
        }

        List<AggregatedSalesData> results = new ArrayList<>(map.values());
        results.forEach(AggregatedSalesData::finalizeAggregation);

        Log.debugf("[%s] Aggregated %d txns → %d summaries (skipped=%d)",
                context.runId, transactions.size() - skipped, results.size(), skipped);

        return results;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void validate(StgSalesTransaction txn) {
        if (txn.transactionDate == null)            throw new IllegalArgumentException("transactionDate is null");
        if (txn.productCode == null || txn.productCode.isBlank()) throw new IllegalArgumentException("productCode is blank");
        if (txn.region == null || txn.region.isBlank())           throw new IllegalArgumentException("region is blank");
        if (txn.quantity == null || txn.quantity <= 0)            throw new IllegalArgumentException("quantity must be > 0");
        if (txn.unitPrice == null || txn.unitPrice.signum() <= 0) throw new IllegalArgumentException("unitPrice must be > 0");
    }

    private record AggregationKey(java.time.LocalDate date, String productCode, String region) {}
}
