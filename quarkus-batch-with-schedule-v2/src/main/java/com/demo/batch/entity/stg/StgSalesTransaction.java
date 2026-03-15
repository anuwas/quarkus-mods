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
 * STG Database Entity — raw sales transaction from the staging database.
 *
 * V2 changes:
 *  - findPendingAfter() uses KEYSET pagination (WHERE id > lastId) — O(1) at any depth.
 *  - findPendingPage() retained for compatibility but should not be used at scale.
 *  - markProcessed() removed; bulk update is now handled via native SQL in the writer.
 */
@Entity
@Table(name = "stg_sales_transaction", indexes = {
        @Index(name = "idx_stg_txn_status_id", columnList = "status, id"),
        @Index(name = "idx_stg_txn_date",      columnList = "transaction_date"),
        @Index(name = "idx_stg_txn_category",  columnList = "category"),
        @Index(name = "idx_stg_txn_region",    columnList = "region")
})
@PersistenceUnit(unitName = "stgdb")
public class StgSalesTransaction extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stg_txn_seq")
    @SequenceGenerator(name = "stg_txn_seq", sequenceName = "stg_sales_transaction_seq", allocationSize = 100)
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
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // -----------------------------------------------------------------------
    // Computed
    // -----------------------------------------------------------------------

    public BigDecimal getTotalAmount() {
        if (quantity == null || unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // -----------------------------------------------------------------------
    // V2: Keyset-based reader — O(1) regardless of dataset depth
    // -----------------------------------------------------------------------

    /**
     * Read the next page of PENDING transactions after the given cursor ID.
     * Uses a composite index on (status, id) for O(1) seeks at any depth.
     *
     * @param lastSeenId  the highest id processed in the previous chunk (0 for first page)
     * @param pageSize    number of records to fetch
     */
    public static List<StgSalesTransaction> findPendingAfter(long lastSeenId, int pageSize) {
        return find("status = ?1 AND id > ?2 ORDER BY id ASC",
                TransactionStatus.PENDING, lastSeenId)
                .page(0, pageSize)
                .list();
    }

    /**
     * Fast approximate row count using pg_class stats — O(1), no table scan.
     * Only used for logging/progress estimates; never used as a loop terminator.
     */
    public static long approximatePendingCount() {
        // Falls back to 0 if stats are stale; callers must treat this as advisory only.
        try {
            Object result = getEntityManager()
                    .createNativeQuery(
                            "SELECT reltuples::bigint FROM pg_class WHERE relname = 'stg_sales_transaction'")
                    .getSingleResult();
            return result == null ? 0L : ((Number) result).longValue();
        } catch (Exception e) {
            return -1L;
        }
    }

    /** Find by date range (unchanged). */
    public static List<StgSalesTransaction> findByDateRange(LocalDate from, LocalDate to) {
        return list("transactionDate >= ?1 AND transactionDate <= ?2 ORDER BY transactionDate ASC", from, to);
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
