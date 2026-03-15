package com.demo.batch.writer;

import com.demo.batch.entity.mn.MnDailySalesSummary;
import com.demo.batch.entity.stg.StgSalesTransaction;
import com.demo.batch.model.AggregatedSalesData;
import com.demo.batch.model.BatchContext;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Batch Writer — persists aggregated {@link AggregatedSalesData} to the MN database
 * and marks source transactions as PROCESSED in the STG database.
 *
 * <p>Uses upsert semantics: existing summaries for the same
 * (date, productCode, region) key are updated rather than duplicated.
 *
 * <p><b>Important:</b> Due to using multiple non-XA datasources, write operations
 * to mndb and stgdb are performed in separate transactions. This ensures proper
 * transaction isolation per datasource.
 */
@ApplicationScoped
public class SalesSummaryWriter {

    @Inject
    @io.quarkus.hibernate.orm.PersistenceUnit("stgdb")
    EntityManager stgEntityManager;


    /**
     * Self-injection via Instance to enable @Transactional interception on internal method calls.
     * Using Instance<> pattern instead of direct self-injection for CDI compliance.
     */
    @Inject
    Instance<SalesSummaryWriter> selfInstance;

    private SalesSummaryWriter self() {
        return selfInstance.get();
    }

    /**
     * Write a list of aggregated summaries to mndb and mark the corresponding
     * source transactions as PROCESSED in stgdb.
     *
     * <p>This method orchestrates two separate transactions:
     * <ol>
     *   <li>Write summaries to mndb (REQUIRES_NEW transaction)</li>
     *   <li>Mark source records as PROCESSED in stgdb (separate REQUIRES_NEW transaction)</li>
     * </ol>
     *
     * <p>Note: Since these are separate transactions, there's a small window where
     * mndb writes succeed but stgdb update fails. The stgdb update failure is logged
     * but does not fail the batch (best-effort).
     *
     * @param summaries aggregated data from the processor
     * @param context   batch run context
     */
    public void write(List<AggregatedSalesData> summaries, BatchContext context) {
        if (summaries == null || summaries.isEmpty()) {
            Log.debugf("[%s] Writer called with empty list — nothing to write.", context.runId);
            return;
        }

        Log.debugf("[%s] Writing %d summaries to mndb", context.runId, summaries.size());

        // Step 1: Write to mndb in its own transaction (via self-injection for @Transactional)
        WriteResult result = self().writeToMndb(summaries, context);

        // Step 2: Mark source records as PROCESSED in stgdb (separate transaction via self-injection)
        List<Long> allSourceIds = summaries.stream()
                .flatMap(s -> s.sourceTransactionIds.stream())
                .collect(Collectors.toList());

        if (!allSourceIds.isEmpty()) {
            self().markSourceRecordsProcessed(allSourceIds);
        }

        context.incrementWritten(result.written);
        Log.infof("[%s] Writer complete — written=%d, failed=%d, source txns marked=%d",
                context.runId, result.written, result.failed, allSourceIds.size());
    }

    /**
     * Write summaries to mndb in a separate REQUIRES_NEW transaction.
     */
    @Transactional(value = TxType.REQUIRES_NEW)
    public WriteResult writeToMndb(List<AggregatedSalesData> summaries, BatchContext context) {
        int written = 0;
        int failed = 0;

        for (AggregatedSalesData data : summaries) {
            try {
                upsertSummary(data, context.runId);
                written++;
            } catch (Exception e) {
                Log.errorf(e, "[%s] Failed to write summary for product=%s date=%s region=%s",
                        context.runId, data.productCode, data.summaryDate, data.region);
                context.incrementFailed(1);
                failed++;
                // Continue — skip this record, don't fail the whole chunk
            }
        }

        return new WriteResult(written, failed);
    }

    /**
     * Marks source records in stgdb as PROCESSED.
     * Uses a separate REQUIRES_NEW transaction to avoid cross-datasource transaction issues.
     */
    @Transactional(value = TxType.REQUIRES_NEW)
    public void markSourceRecordsProcessed(List<Long> ids) {
        try {
            // Use the injected stgEntityManager directly to ensure correct persistence unit context
            int updated = stgEntityManager.createQuery(
                    "UPDATE StgSalesTransaction SET status = :status, processedAt = :processedAt WHERE id IN :ids")
                    .setParameter("status", StgSalesTransaction.TransactionStatus.PROCESSED)
                    .setParameter("processedAt", java.time.LocalDateTime.now())
                    .setParameter("ids", ids)
                    .executeUpdate();
            Log.debugf("Marked %d STG transactions as PROCESSED", updated);
        } catch (Exception e) {
            // Log but don't fail — STG status update is best-effort
            Log.warnf("Could not mark source transactions as processed: %s", e.getMessage());
        }
    }

    /**
     * Simple holder for write results.
     */
    public static class WriteResult {
        public final int written;
        public final int failed;

        public WriteResult(int written, int failed) {
            this.written = written;
            this.failed = failed;
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void upsertSummary(AggregatedSalesData data, String runId) {
        Optional<MnDailySalesSummary> existing =
                MnDailySalesSummary.findByDateProductRegion(data.summaryDate, data.productCode, data.region);

        MnDailySalesSummary summary = existing.orElseGet(MnDailySalesSummary::new);

        summary.summaryDate       = data.summaryDate;
        summary.productCode       = data.productCode;
        summary.productName       = data.productName;
        summary.category          = data.category;
        summary.region            = data.region;
        summary.totalQuantity     = data.totalQuantity;
        summary.totalRevenue      = data.totalRevenue;
        summary.averageUnitPrice  = data.averageUnitPrice;
        summary.minUnitPrice      = data.minUnitPrice;
        summary.maxUnitPrice      = data.maxUnitPrice;
        summary.transactionCount  = data.transactionCount;
        summary.batchRunId        = runId;
        summary.updatedAt         = LocalDateTime.now();

        if (existing.isEmpty()) {
            summary.persist();
        }
        // If existing, Hibernate dirty-checking will flush the update automatically
    }
}
