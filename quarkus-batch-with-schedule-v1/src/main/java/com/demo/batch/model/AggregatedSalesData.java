package com.demo.batch.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Intermediate aggregated result produced by the processor.
 * Groups raw transactions by date + product + region into a summary.
 */
public final class AggregatedSalesData {

    public final LocalDate summaryDate;
    public final String productCode;
    public final String productName;
    public final String category;
    public final String region;

    public long totalQuantity;
    public BigDecimal totalRevenue;
    public BigDecimal averageUnitPrice;
    public BigDecimal minUnitPrice;
    public BigDecimal maxUnitPrice;
    public long transactionCount;

    /** Source transaction IDs that contributed to this aggregate (for marking as processed). */
    public final List<Long> sourceTransactionIds = new ArrayList<>();

    public AggregatedSalesData(LocalDate summaryDate, String productCode, String productName,
                                String category, String region) {
        this.summaryDate = summaryDate;
        this.productCode = productCode;
        this.productName = productName;
        this.category = category;
        this.region = region;
        this.totalRevenue = BigDecimal.ZERO;
        this.minUnitPrice = null;
        this.maxUnitPrice = null;
    }

    /**
     * Accumulate one transaction into this aggregate.
     */
    public void accumulate(long quantity, BigDecimal unitPrice, Long transactionId) {
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.totalQuantity += quantity;
        this.totalRevenue = this.totalRevenue.add(lineTotal);
        this.transactionCount++;

        if (this.minUnitPrice == null || unitPrice.compareTo(this.minUnitPrice) < 0) {
            this.minUnitPrice = unitPrice;
        }
        if (this.maxUnitPrice == null || unitPrice.compareTo(this.maxUnitPrice) > 0) {
            this.maxUnitPrice = unitPrice;
        }
        if (transactionId != null) {
            this.sourceTransactionIds.add(transactionId);
        }
    }

    /**
     * Finalize averages after all accumulations are complete.
     */
    public void finalizeAggregation() {
        if (transactionCount > 0) {
            this.averageUnitPrice = this.totalRevenue
                    .divide(BigDecimal.valueOf(totalQuantity), 2, RoundingMode.HALF_UP);
        } else {
            this.averageUnitPrice = BigDecimal.ZERO;
        }
    }

    @Override
    public String toString() {
        return "AggregatedSalesData{date=%s, product='%s', region='%s', qty=%d, revenue=%s, txns=%d}"
                .formatted(summaryDate, productCode, region, totalQuantity, totalRevenue, transactionCount);
    }
}
