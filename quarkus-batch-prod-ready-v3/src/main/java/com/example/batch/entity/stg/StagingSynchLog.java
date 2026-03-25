package com.example.batch.entity.stg;

import com.example.batch.config.RecordStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Staging entity — raw inbound records waiting to be processed.
 *
 * Status lifecycle: PENDING → PROCESSING → COMPLETED | FAILED
 *
 * Table indexes:
 *   (status, id)   — composite; drives the keyset claim query (O(1) seek)
 *   (status, created_at) — for monitoring / ops queries
 *
 * NOTE: This entity lives in the "stgdb" persistence unit only.
 *       Never inject the default (main) EntityManager here.
 */
@Entity
@Table(
    name = "staging_synch_log",
    indexes = {
        @Index(name = "idx_stg_status_id",         columnList = "status, id"),
        @Index(name = "idx_stg_status_created_at", columnList = "status, created_at")
    }
)
public class StagingSynchLog {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stg_synch_log_seq")
    @SequenceGenerator(
        name          = "stg_synch_log_seq",
        sequenceName  = "stg_synch_log_sequence",
        allocationSize = 100
    )
    public Long id;


    @NotBlank
    @Column(name = "table_name", nullable = false, length = 200)
    public String tableName;

    @NotBlank
    @Column(name = "table_reference", nullable = false, length = 255)
    public String tableReference;


    @Column(name = "notes", length = 500)
    public String notes;

    // -----------------------------------------------------------------------
    // Status / audit
    // -----------------------------------------------------------------------

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public RecordStatus status = RecordStatus.PENDING;

    @Column(name = "node_id", length = 100)
    public String nodeId;                    // which node claimed this record

    @Column(name = "error_message", length = 1000)
    public String errorMessage;

    @Column(name = "retry_count", nullable = false)
    public int retryCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @Column(name = "processed_at")
    public LocalDateTime processedAt;

    // -----------------------------------------------------------------------
    // Lifecycle hooks
    // -----------------------------------------------------------------------

    @PrePersist
    public void onPersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // -----------------------------------------------------------------------
    // String representation
    // -----------------------------------------------------------------------


    @Override
    public String toString() {
        return "StagingSynchLog{id=%d, table='%s', status=%s}".formatted(id, tableName, status);
    }
}
