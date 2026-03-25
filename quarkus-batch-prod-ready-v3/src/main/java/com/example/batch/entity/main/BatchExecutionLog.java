package com.example.batch.entity.main;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Audit log for every batch execution — one row per scheduled run.
 * Written to mainDB so it survives across node restarts.
 */
@Entity
@Table(
    name = "batch_execution_log",
    indexes = {
        @Index(name = "idx_bel_batch_id",    columnList = "batch_id"),
        @Index(name = "idx_bel_status",      columnList = "status"),
        @Index(name = "idx_bel_started_at",  columnList = "started_at"),
        @Index(name = "idx_bel_node_id",     columnList = "node_id")
    }
)
public class BatchExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bel_seq")
    @SequenceGenerator(name = "bel_seq", sequenceName = "bel_sequence", allocationSize = 10)
    public Long id;

    @Column(name = "batch_id",       nullable = false, unique = true, length = 60)
    public String batchId;

    @Column(name = "node_id",        nullable = false, length = 100)
    public String nodeId;

    @Column(name = "status",         nullable = false, length = 30)
    public String status;            // STARTED | COMPLETED | COMPLETED_WITH_ERRORS | FAILED | EMPTY

    @Column(name = "chunk_size",     nullable = false)
    public int chunkSize;

    @Column(name = "records_read",   nullable = false)
    public long recordsRead    = 0;

    @Column(name = "records_ok",     nullable = false)
    public long recordsOk      = 0;

    @Column(name = "records_failed", nullable = false)
    public long recordsFailed  = 0;

    @Column(name = "started_at",     nullable = false)
    public LocalDateTime startedAt;

    @Column(name = "completed_at")
    public LocalDateTime completedAt;

    @Column(name = "duration_ms")
    public Long durationMs;

    @Column(name = "throughput_rps")
    public Double throughputRps;

    @Column(name = "error_summary",  length = 2000)
    public String errorSummary;

    @PrePersist
    public void onPersist() {
        if (startedAt == null) startedAt = LocalDateTime.now();
    }

}
