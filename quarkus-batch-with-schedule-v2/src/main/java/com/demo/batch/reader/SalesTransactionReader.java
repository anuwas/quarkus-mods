package com.demo.batch.reader;

import com.demo.batch.entity.stg.StgSalesTransaction;
import com.demo.batch.exception.BatchReadException;
import com.demo.batch.model.BatchContext;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Batch Reader V2 — keyset-based page reader from stgdb.
 *
 * Key changes from V1:
 *  - Uses WHERE id > lastSeenId (keyset) instead of OFFSET.
 *    Every call is O(1) regardless of how far into the dataset we are.
 *  - No startup COUNT(*) — callers drive the loop until an empty page is returned.
 *  - approximatePendingCount() is available for advisory logging only.
 */
@ApplicationScoped
public class SalesTransactionReader {

    /**
     * Read the next chunk of PENDING transactions strictly after {@code lastSeenId}.
     *
     * <p>Pass {@code lastSeenId = 0} for the very first page.
     * Returns an empty list when no more PENDING rows exist — this is the loop-termination signal.
     *
     * @param lastSeenId  the highest id from the previous chunk (cursor)
     * @param pageSize    records to fetch
     * @param context     current batch context (for metrics)
     */
    public List<StgSalesTransaction> readPage(long lastSeenId, int pageSize, BatchContext context) {
        Log.debugf("[%s] Reading chunk after id=%d (size=%d)", context.runId, lastSeenId, pageSize);

        try {
            List<StgSalesTransaction> records =
                    StgSalesTransaction.findPendingAfter(lastSeenId, pageSize);

            context.incrementRead(records.size());

            if (!records.isEmpty()) {
                Log.debugf("[%s] Read %d records (ids %d..%d) total-read=%d",
                        context.runId, records.size(),
                        records.get(0).id,
                        records.get(records.size() - 1).id,
                        context.getRecordsRead());
            }

            return records;

        } catch (Exception e) {
            throw new BatchReadException(
                    "Failed to read chunk after id=%d: %s".formatted(lastSeenId, e.getMessage()), e);
        }
    }

    /**
     * Fast O(1) approximate count from pg_class — used only for progress logging,
     * never as a loop-termination condition.
     */
    public long approximatePendingCount(BatchContext context) {
        long approx = StgSalesTransaction.approximatePendingCount();
        Log.infof("[%s] Approximate pending rows (pg_class estimate): %d", context.runId, approx);
        return approx;
    }
}
