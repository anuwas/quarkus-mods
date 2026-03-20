package com.example.batch.entity.main;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Main DB entity — aggregated and validated result written by the batch processor.
 *
 * Aggregation key: (transaction_date, product_code, region)
 * Upsert semantics: if a row for the same key already exists it is updated;
 * otherwise a new row is inserted.
 *
 * Lives exclusively in the "maindb" persistence unit.
 */
@Entity
@Table(
    name = "aggregated_result",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_agg_date_product_region",
            columnNames = {"transaction_date", "product_code", "region"})
    },
    indexes = {
        @Index(name = "idx_agg_date",    columnList = "transaction_date"),
        @Index(name = "idx_agg_product", columnList = "product_code"),
        @Index(name = "idx_agg_region",  columnList = "region"),
        @Index(name = "idx_agg_batch",   columnList = "batch_id")
    }
)
public class AggregatedResult extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "agg_result_seq")
    @SequenceGenerator(
        name          = "agg_result_seq",
        sequenceName  = "agg_result_sequence",
        allocationSize = 50
    )
    public Long id;

    // -----------------------------------------------------------------------
    // Aggregation key
    // -----------------------------------------------------------------------

    @Column(name = "transaction_date", nullable = false)
    public LocalDate transactionDate;

    @Column(name = "product_code", nullable = false, length = 30)
    public String productCode;

    @Column(name = "product_name", nullable = false, length = 200)
    public String productName;

    @Column(name = "category", nullable = false, length = 80)
    public String category;

    @Column(name = "region", nullable = false, length = 50)
    public String region;

    // -----------------------------------------------------------------------
    // Aggregated metrics
    // -----------------------------------------------------------------------

    @Column(name = "total_quantity",      nullable = false)
    public Long totalQuantity;

    @Column(name = "total_revenue",       nullable = false, precision = 18, scale = 4)
    public BigDecimal totalRevenue;

    @Column(name = "avg_unit_price",      nullable = false, precision = 14, scale = 4)
    public BigDecimal avgUnitPrice;

    @Column(name = "min_unit_price",      precision = 14, scale = 4)
    public BigDecimal minUnitPrice;

    @Column(name = "max_unit_price",      precision = 14, scale = 4)
    public BigDecimal maxUnitPrice;

    @Column(name = "transaction_count",   nullable = false)
    public Long transactionCount;

    @Column(name = "unique_customer_count", nullable = false)
    public Long uniqueCustomerCount;

    // -----------------------------------------------------------------------
    // Batch audit
    // -----------------------------------------------------------------------

    @Column(name = "batch_id",   nullable = false, length = 60)
    public String batchId;       // ties back to BatchExecutionLog

    @Column(name = "node_id",    nullable = false, length = 100)
    public String nodeId;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PrePersist  public void onPersist() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate   public void onUpdate()  { updatedAt = LocalDateTime.now(); }

    // -----------------------------------------------------------------------
    // Queries
    // -----------------------------------------------------------------------

    public static Optional<AggregatedResult> findByKey(
            LocalDate date, String productCode, String region) {
        return find("transactionDate = ?1 AND productCode = ?2 AND region = ?3",
                date, productCode, region).firstResultOptional();
    }

    public static List<AggregatedResult> findByDateRange(LocalDate from, LocalDate to) {
        return list("transactionDate >= ?1 AND transactionDate <= ?2 " +
                    "ORDER BY transactionDate ASC, productCode ASC", from, to);
    }

    public static List<AggregatedResult> findByBatchId(String batchId) {
        return list("batchId", batchId);
    }
}
