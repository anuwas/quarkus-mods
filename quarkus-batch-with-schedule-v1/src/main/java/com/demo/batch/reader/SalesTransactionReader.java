package com.demo.batch.reader;

import com.demo.batch.entity.stg.StgSalesTransaction;
import com.demo.batch.exception.BatchReadException;
import com.demo.batch.model.BatchContext;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Batch Reader — reads raw sales transactions from the STG (staging) database
 * in configurable, memory-efficient pages.
 *
 * <p>Design principles:
 * <ul>
 *   <li>Reads only PENDING transactions to support idempotent re-runs.</li>
 *   <li>Keyset / offset pagination keeps memory usage predictable.</li>
 *   <li>Returns an empty list to signal end-of-input (no sentinel null).</li>
 * </ul>
 */
@ApplicationScoped
public class SalesTransactionReader {

    /**
     * Reads one page of pending {@link StgSalesTransaction} records.
     *
     * @param page      zero-based page index
     * @param pageSize  number of records per page
     * @param context   current batch run context (for metrics)
     * @return list of transactions; empty list signals no more data
     */
    public List<StgSalesTransaction> readPage(int page, int pageSize, BatchContext context) {
        Log.debugf("[%s] Reading page %d (size=%d)", context.runId, page, pageSize);

        try {
            List<StgSalesTransaction> records = StgSalesTransaction.findPendingPage(page, pageSize);
            context.incrementRead(records.size());

            Log.infof("[%s] Page %d → %d records read (total read so far: %d)",
                    context.runId, page, records.size(), context.getRecordsRead());

            return records;

        } catch (Exception e) {
            throw new BatchReadException(
                    "Failed to read page %d from stgdb: %s".formatted(page, e.getMessage()), e);
        }
    }

    /**
     * Returns the total count of pending records — used to pre-calculate
     * the number of pages and for progress reporting.
     */
    public long countPending(BatchContext context) {
        try {
            long count = StgSalesTransaction.countPending();
            Log.infof("[%s] Total pending records in stgdb: %d", context.runId, count);
            return count;
        } catch (Exception e) {
            throw new BatchReadException("Failed to count pending transactions: " + e.getMessage(), e);
        }
    }
}
