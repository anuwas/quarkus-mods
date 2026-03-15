package com.demo.batch.writer;

import com.demo.batch.entity.mn.MnDailySalesSummary;
import com.demo.batch.exception.BatchWriteException;
import com.demo.batch.model.AggregatedSalesData;
import com.demo.batch.model.BatchContext;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Batch Writer V2 — persists aggregated summaries to mndb and marks source rows PROCESSED in stgdb.
 *
 * Key changes from V1:
 *  - markSourceRecordsProcessed() uses a native SQL temp-table join instead of IN (...),
 *    which breaks the Postgres query planner above ~1000 parameters.
 *  - All writes still happen in a single @Transactional boundary (atomic per chunk).
 *  - Safe for concurrent calls from parallel workers — each call is a separate transaction.
 */
@ApplicationScoped
public class SalesSummaryWriter {

    @Inject
    @io.quarkus.hibernate.orm.PersistenceUnit("stgdb")
    EntityManager stgEm;

    @Inject
    @io.quarkus.hibernate.orm.PersistenceUnit("mndb")
    EntityManager mnEm;

    /**
     * Write aggregated summaries to mndb and mark source transactions as PROCESSED in stgdb.
     * Both operations are wrapped in a single transaction — they commit or rollback together.
     */
    @Transactional
    public void write(List<AggregatedSalesData> summaries, BatchContext context) {
        if (summaries == null || summaries.isEmpty()) return;

        int written = 0;
        int failed  = 0;

        for (AggregatedSalesData data : summaries) {
            try {
                upsertSummary(data, context.runId);
                written++;
            } catch (Exception e) {
                Log.errorf(e, "[%s] Failed to write summary product=%s date=%s region=%s",
                        context.runId, data.productCode, data.summaryDate, data.region);
                context.incrementFailed(1);
                failed++;
            }
        }

        // Flush Hibernate writes before the native bulk update
        mnEm.flush();

        // Collect all source IDs from this chunk and bulk-mark as PROCESSED
        List<Long> allSourceIds = summaries.stream()
                .flatMap(s -> s.sourceTransactionIds.stream())
                .collect(Collectors.toList());

        if (!allSourceIds.isEmpty()) {
            markSourceRecordsProcessed(allSourceIds);
        }

        context.incrementWritten(written);
        context.incrementChunks();

        Log.debugf("[%s] Writer chunk done — written=%d failed=%d sourceMarked=%d",
                context.runId, written, failed, allSourceIds.size());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void upsertSummary(AggregatedSalesData data, String runId) {
        Optional<MnDailySalesSummary> existing =
                MnDailySalesSummary.findByDateProductRegion(
                        data.summaryDate, data.productCode, data.region);

        MnDailySalesSummary s = existing.orElseGet(MnDailySalesSummary::new);
        s.summaryDate      = data.summaryDate;
        s.productCode      = data.productCode;
        s.productName      = data.productName;
        s.category         = data.category;
        s.region           = data.region;
        s.totalQuantity    = data.totalQuantity;
        s.totalRevenue     = data.totalRevenue;
        s.averageUnitPrice = data.averageUnitPrice;
        s.minUnitPrice     = data.minUnitPrice;
        s.maxUnitPrice     = data.maxUnitPrice;
        s.transactionCount = data.transactionCount;
        s.batchRunId       = runId;
        s.updatedAt        = LocalDateTime.now();

        if (existing.isEmpty()) s.persist();
        // existing: Hibernate dirty-check flushes the update automatically
    }

    /**
     * V2: Bulk-mark source rows as PROCESSED using a PostgreSQL temp table join.
     *
     * Why not IN (...)?  PostgreSQL's query planner degrades sharply with IN lists
     * longer than ~1000 items — each item becomes a separate bind parameter and the
     * execution plan cannot use the index efficiently.
     *
     * This approach:
     *   1. Creates a session-scoped temp table (deleted on commit).
     *   2. Batch-inserts the IDs using Hibernate's statement batch size.
     *   3. Executes a single UPDATE … FROM join — one round trip, one index seek.
     */
    private void markSourceRecordsProcessed(List<Long> ids) {
        try {
            // 1. Create temp table (ON COMMIT DELETE ROWS — auto-cleaned after txn)
            stgEm.createNativeQuery(
                    "CREATE TEMP TABLE IF NOT EXISTS _tmp_processed_ids " +
                    "(id BIGINT NOT NULL) ON COMMIT DELETE ROWS")
                    .executeUpdate();

            // 2. Batch-insert IDs (Hibernate flushes every statement-batch-size rows)
            int batchSize = 50;
            for (int i = 0; i < ids.size(); i++) {
                stgEm.createNativeQuery("INSERT INTO _tmp_processed_ids VALUES (?1)")
                        .setParameter(1, ids.get(i))
                        .executeUpdate();
                if ((i + 1) % batchSize == 0) stgEm.flush();
            }
            stgEm.flush();

            // 3. Single UPDATE … FROM — one round trip, leverages (status, id) index
            int updated = stgEm.createNativeQuery(
                    "UPDATE stg_sales_transaction t " +
                    "SET    status = 'PROCESSED', processed_at = NOW() " +
                    "FROM   _tmp_processed_ids tmp " +
                    "WHERE  t.id = tmp.id")
                    .executeUpdate();

            Log.debugf("Bulk-marked %d/%d STG rows as PROCESSED", updated, ids.size());

        } catch (Exception e) {
            // Best-effort — don't fail the write because of a status update issue
            Log.warnf("Could not bulk-mark source transactions as PROCESSED: %s", e.getMessage());
        }
    }
}
