package com.demo.batch.entity.stg;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * STG Database Entity - Raw sales transaction from the staging database.
 * Read-only source data for batch processing.
 */
@Entity
@Table(name = "stg_sales_transaction", indexes = {
        @Index(name = "idx_stg_txn_date", columnList = "transaction_date"),
        @Index(name = "idx_stg_txn_status", columnList = "status"),
        @Index(name = "idx_stg_txn_category", columnList = "category"),
        @Index(name = "idx_stg_txn_region", columnList = "region")
})
@PersistenceUnit(unitName = "stgdb")
public class StgSalesTransaction extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stg_txn_seq")
    @SequenceGenerator(name = "stg_txn_seq", sequenceName = "stg_sales_transaction_seq", allocationSize = 50)
    public Long id;

    @NotNull
    @Column(name = "transaction_date", nullable = false)
    public LocalDate transactionDate;

    @NotNull
    @Column(name = "product_code", nullable = false, length = 20)
    public String productCode;

    @NotNull
    @Column(name = "product_name", nullable = false, length = 100)
    public String productName;

    @NotNull
    @Column(name = "category", nullable = false, length = 50)
    public String category;

    @Positive
    @Column(name = "quantity", nullable = false)
    public Integer quantity;

    @Positive
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    public BigDecimal unitPrice;

    @NotNull
    @Column(name = "customer_id", nullable = false, length = 20)
    public String customerId;

    @NotNull
    @Column(name = "region", nullable = false, length = 20)
    public String region;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "processed_at")
    public LocalDateTime processedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // -------------------------
    // Computed helper
    // -------------------------
    public BigDecimal getTotalAmount() {
        if (quantity == null || unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // -------------------------
    // Repository-style queries
    // -------------------------

    /** Fetch a page of PENDING transactions ordered by date for batch reading. */
    public static List<StgSalesTransaction> findPendingPage(int page, int pageSize) {
        return find("status = ?1 ORDER BY transactionDate ASC, id ASC",
                TransactionStatus.PENDING)
                .page(page, pageSize)
                .list();
    }

    /** Count pending transactions. */
    public static long countPending() {
        return count("status", TransactionStatus.PENDING);
    }

    /** Find by date range. */
    public static List<StgSalesTransaction> findByDateRange(LocalDate from, LocalDate to) {
        return list("transactionDate >= ?1 AND transactionDate <= ?2 ORDER BY transactionDate ASC", from, to);
    }

    /** Mark a batch as processed. */
    public static int markProcessed(List<Long> ids) {
        return update("status = ?1, processedAt = ?2 WHERE id IN ?3",
                TransactionStatus.PROCESSED, LocalDateTime.now(), ids);
    }

    public enum TransactionStatus {
        PENDING, PROCESSING, PROCESSED, FAILED, SKIPPED
    }

    @Override
    public String toString() {
        return "StgSalesTransaction{id=%d, productCode='%s', date=%s, status=%s}"
                .formatted(id, productCode, transactionDate, status);
    }
}
