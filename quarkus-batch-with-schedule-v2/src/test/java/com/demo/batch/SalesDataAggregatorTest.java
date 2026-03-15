package com.demo.batch;

import com.demo.batch.entity.stg.StgSalesTransaction;
import com.demo.batch.model.AggregatedSalesData;
import com.demo.batch.model.BatchContext;
import com.demo.batch.processor.SalesDataAggregator;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SalesDataAggregatorTest {

    @Inject SalesDataAggregator aggregator;

    private BatchContext ctx;

    @BeforeEach
    void setUp() {
        ctx = new BatchContext("TEST-001", "TestJob", 10, 5);
    }

    @Test @DisplayName("Empty input returns empty output")
    void testEmptyInput() {
        assertTrue(aggregator.aggregate(List.of(), ctx).isEmpty());
    }

    @Test @DisplayName("Single transaction aggregated correctly")
    void testSingleTransaction() {
        var txn = txn(1L, LocalDate.of(2024, 1, 15), "P001", "Laptop", "Electronics", 2, "199.99", "NORTH");
        var result = aggregator.aggregate(List.of(txn), ctx);
        assertEquals(1, result.size());
        var s = result.get(0);
        assertEquals(2L, s.totalQuantity);
        assertEquals(new BigDecimal("399.98"), s.totalRevenue);
        assertEquals(1L, s.transactionCount);
    }

    @Test @DisplayName("Same key merges into one summary")
    void testAggregationByKey() {
        var date = LocalDate.of(2024, 1, 15);
        var result = aggregator.aggregate(List.of(
                txn(1L, date, "P001", "Laptop", "Electronics", 2, "100.00", "NORTH"),
                txn(2L, date, "P001", "Laptop", "Electronics", 3, "100.00", "NORTH")), ctx);
        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).totalQuantity);
        assertEquals(2L, result.get(0).transactionCount);
    }

    @Test @DisplayName("Different regions produce separate summaries")
    void testDifferentRegions() {
        var date = LocalDate.of(2024, 1, 15);
        var result = aggregator.aggregate(List.of(
                txn(1L, date, "P001", "Laptop", "Electronics", 1, "100.00", "NORTH"),
                txn(2L, date, "P001", "Laptop", "Electronics", 1, "100.00", "SOUTH")), ctx);
        assertEquals(2, result.size());
    }

    @Test @DisplayName("Zero quantity is skipped, counter incremented")
    void testInvalidTransactionSkipped() {
        aggregator.aggregate(List.of(txn(99L, LocalDate.now(), "X", "X", "X", 0, "10.00", "NORTH")), ctx);
        assertEquals(1, ctx.getRecordsSkipped());
    }

    @Test @DisplayName("V2: skip limit pct scales with chunk size")
    void testDynamicSkipLimit() {
        var ctx2 = new BatchContext("T2", "J", 1000, 5);
        assertEquals(50, ctx2.effectiveSkipLimit()); // 5% of 1000
    }

    // ---- helper ----
    private StgSalesTransaction txn(Long id, LocalDate date, String code, String name,
                                     String cat, int qty, String price, String region) {
        var t = new StgSalesTransaction();
        t.id = id; t.transactionDate = date; t.productCode = code;
        t.productName = name; t.category = cat; t.quantity = qty;
        t.unitPrice = new BigDecimal(price); t.region = region;
        t.customerId = "CUST-T"; t.status = StgSalesTransaction.TransactionStatus.PENDING;
        return t;
    }
}
