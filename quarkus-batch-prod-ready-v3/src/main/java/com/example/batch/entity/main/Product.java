package com.example.batch.entity.main;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Main DB entity — aggregated and validated result written by the batch processor.
 *
 * Aggregation key: table_name
 * Upsert semantics: if a row for the same key already exists it is updated;
 * otherwise a new row is inserted.
 *
 * Lives exclusively in the default (main) persistence unit.
 */
@Entity
@Table(
    name = "product",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_agg_product",
            columnNames = {"table_name"})
    },
    indexes = {
        @Index(name = "idx_agg_product", columnList = "table_name"),
        @Index(name = "idx_agg_batch",   columnList = "batch_id")
    }
)
public class Product {

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


    @Column(name = "table_name", nullable = false, length = 200)
    public String tableName;

    // -----------------------------------------------------------------------
    // Aggregated metrics
    // -----------------------------------------------------------------------

    @Column(name = "total_quantity",      nullable = false)
    public Long totalQuantity;


    @Column(name = "transaction_count",   nullable = false)
    public Long transactionCount;


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

}

