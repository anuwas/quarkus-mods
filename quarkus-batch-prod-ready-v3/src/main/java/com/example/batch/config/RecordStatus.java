package com.example.batch.config;

/**
 * Status lifecycle for staging records.
 *
 * State machine:
 *
 *   PENDING
 *     │  locked by scheduler node (SELECT FOR UPDATE SKIP LOCKED)
 *     ▼
 *   PROCESSING
 *     │  success: aggregated result written to mainDB
 *     ├──────────────────────► COMPLETED
 *     │  validation / business error (skippable)
 *     └──────────────────────► FAILED
 *
 * Only PENDING rows are ever targeted by the reader.
 * PROCESSING rows that are orphaned (e.g. node crash) can be
 * reset to PENDING by the admin API or a maintenance job.
 */
public enum RecordStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
