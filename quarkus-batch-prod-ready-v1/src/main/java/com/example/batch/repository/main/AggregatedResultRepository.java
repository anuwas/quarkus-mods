package com.example.batch.repository.main;

import com.example.batch.entity.main.AggregatedResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Repository for AggregatedResult operations on mainDB.
 *
 * Uses upsert semantics: if a row already exists for the aggregation key
 * (transaction_date, product_code, region) the metrics are merged rather
 * than duplicated.  This makes the batch idempotent — safe to retry.
 */
@ApplicationScoped
public class AggregatedResultRepository {

    @Inject
    @io.quarkus.hibernate.orm.PersistenceUnit("maindb")
    EntityManager em;

    /**
     * Upsert a list of aggregated results.
     * Must be called inside an active transaction (MANDATORY).
     */
    @Transactional(Transactional.TxType.MANDATORY)
    public void upsertAll(List<AggregatedResult> results) {
        for (AggregatedResult result : results) {
            AggregatedResult.findByKey(result.transactionDate, result.productCode, result.region)
                .ifPresentOrElse(
                    existing -> merge(existing, result),
                    result::persist
                );
        }
        em.flush();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void merge(AggregatedResult existing, AggregatedResult incoming) {
        existing.totalQuantity        = existing.totalQuantity + incoming.totalQuantity;
        existing.totalRevenue         = existing.totalRevenue.add(incoming.totalRevenue);
        existing.transactionCount     = existing.transactionCount + incoming.transactionCount;
        existing.uniqueCustomerCount  = existing.uniqueCustomerCount + incoming.uniqueCustomerCount;

        // Re-compute average, min, max across the merged totals
        if (existing.totalQuantity > 0) {
            existing.avgUnitPrice = existing.totalRevenue
                .divide(java.math.BigDecimal.valueOf(existing.totalQuantity),
                        4, java.math.RoundingMode.HALF_UP);
        }
        if (incoming.minUnitPrice != null &&
            (existing.minUnitPrice == null ||
             incoming.minUnitPrice.compareTo(existing.minUnitPrice) < 0)) {
            existing.minUnitPrice = incoming.minUnitPrice;
        }
        if (incoming.maxUnitPrice != null &&
            (existing.maxUnitPrice == null ||
             incoming.maxUnitPrice.compareTo(existing.maxUnitPrice) > 0)) {
            existing.maxUnitPrice = incoming.maxUnitPrice;
        }
        existing.batchId = incoming.batchId;  // update to latest batch
        existing.nodeId  = incoming.nodeId;
    }
}
