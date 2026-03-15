package com.demo.batch.entity.mn;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MN Database Entity - Aggregated daily sales summary written by the batch processor.
 * Represents one row per product per day with aggregated metrics.
 */
@Entity
@Table(name = "mn_daily_sales_summary", uniqueConstraints = {
        @UniqueConstraint(name = "uq_mn_summary", columnNames = {"summary_date", "product_code", "region"})
}, indexes = {
        @Index(name = "idx_mn_summary_date", columnList = "summary_date"),
        @Index(name = "idx_mn_summary_product", columnList = "product_code"),
        @Index(name = "idx_mn_summary_region", columnList = "region"),
        @Index(name = "idx_mn_summary_category", columnList = "category")
})
@PersistenceUnit(unitName = "mndb")
public class MnDailySalesSummary extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mn_summary_seq")
    @SequenceGenerator(name = "mn_summary_seq", sequenceName = "mn_daily_sales_summary_seq", allocationSize = 50)
    public Long id;

    @NotNull
    @Column(name = "summary_date", nullable = false)
    public LocalDate summaryDate;

    @NotNull
    @Column(name = "product_code", nullable = false, length = 20)
    public String productCode;

    @NotNull
    @Column(name = "product_name", nullable = false, length = 100)
    public String productName;

    @NotNull
    @Column(name = "category", nullable = false, length = 50)
    public String category;

    @NotNull
    @Column(name = "region", nullable = false, length = 20)
    public String region;

    @Column(name = "total_quantity", nullable = false)
    public Long totalQuantity;

    @Column(name = "total_revenue", nullable = false, precision = 15, scale = 2)
    public BigDecimal totalRevenue;

    @Column(name = "average_unit_price", nullable = false, precision = 12, scale = 2)
    public BigDecimal averageUnitPrice;

    @Column(name = "transaction_count", nullable = false)
    public Long transactionCount;

    @Column(name = "min_unit_price", precision = 12, scale = 2)
    public BigDecimal minUnitPrice;

    @Column(name = "max_unit_price", precision = 12, scale = 2)
    public BigDecimal maxUnitPrice;

    @Column(name = "batch_run_id", nullable = false, length = 50)
    public String batchRunId;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // -------------------------
    // Repository-style queries
    // -------------------------

    public static List<MnDailySalesSummary> findByDateRange(LocalDate from, LocalDate to) {
        return list("summaryDate >= ?1 AND summaryDate <= ?2 ORDER BY summaryDate ASC, productCode ASC", from, to);
    }

    public static Optional<MnDailySalesSummary> findByDateProductRegion(LocalDate date, String productCode, String region) {
        return find("summaryDate = ?1 AND productCode = ?2 AND region = ?3", date, productCode, region)
                .firstResultOptional();
    }

    public static List<MnDailySalesSummary> findByBatchRunId(String batchRunId) {
        return list("batchRunId", batchRunId);
    }

    @Override
    public String toString() {
        return "MnDailySalesSummary{date=%s, product='%s', region='%s', revenue=%s}"
                .formatted(summaryDate, productCode, region, totalRevenue);
    }
}
