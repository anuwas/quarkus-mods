package com.demo.batch.entity.mn;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MN Database Entity - Audit log for every batch job execution.
 */
@Entity
@Table(name = "mn_batch_job_log", indexes = {
        @Index(name = "idx_mn_job_run_id", columnList = "run_id"),
        @Index(name = "idx_mn_job_status", columnList = "status"),
        @Index(name = "idx_mn_job_started_at", columnList = "started_at")
})
@PersistenceUnit(unitName = "mndb")
public class MnBatchJobLog extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mn_job_log_seq")
    @SequenceGenerator(name = "mn_job_log_seq", sequenceName = "mn_batch_job_log_seq", allocationSize = 10)
    public Long id;

    @Column(name = "run_id", nullable = false, unique = true, length = 50)
    public String runId;

    @Column(name = "job_name", nullable = false, length = 100)
    public String jobName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public JobStatus status;

    @Column(name = "started_at", nullable = false)
    public LocalDateTime startedAt;

    @Column(name = "completed_at")
    public LocalDateTime completedAt;

    @Column(name = "records_read")
    public Long recordsRead = 0L;

    @Column(name = "records_processed")
    public Long recordsProcessed = 0L;

    @Column(name = "records_written")
    public Long recordsWritten = 0L;

    @Column(name = "records_skipped")
    public Long recordsSkipped = 0L;

    @Column(name = "records_failed")
    public Long recordsFailed = 0L;

    @Column(name = "duration_ms")
    public Long durationMs;

    @Column(name = "error_message", length = 2000)
    public String errorMessage;

    @Column(name = "chunk_size")
    public Integer chunkSize;

    // -------------------------
    // Repository-style queries
    // -------------------------

    public static List<MnBatchJobLog> findRecentJobs(int limit) {
        return find("ORDER BY startedAt DESC").page(0, limit).list();
    }

    public static java.util.Optional<MnBatchJobLog> findByRunId(String runId) {
        return find("runId", runId).firstResultOptional();
    }

    public enum JobStatus {
        STARTED, RUNNING, COMPLETED, COMPLETED_WITH_ERRORS, FAILED
    }

    @Override
    public String toString() {
        return "MnBatchJobLog{runId='%s', status=%s, read=%d, written=%d}"
                .formatted(runId, status, recordsRead, recordsWritten);
    }
}
