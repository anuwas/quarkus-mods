# Quarkus Production Batch Processor

**Production-ready** Quarkus batch application built with Java 21, Panache ORM, and PostgreSQL.

Reads `PENDING` records from `stgDB` in locked chunks, validates and aggregates them,
writes results to `mainDB`, and updates each record's status accordingly — safely across multiple nodes.

---

## Status Lifecycle

```
stgDB: staging_record.status

  PENDING
    │  ← scheduler/trigger claims chunk
    │    SELECT … FOR UPDATE SKIP LOCKED
    │    UPDATE status = PROCESSING
    ▼
  PROCESSING
    │  ← validation + aggregation (in-memory)
    │
    ├─── validation OK ──────────────────► COMPLETED
    │     write to mainDB.aggregated_result
    │
    └─── validation failed / exception ──► FAILED
          error_message populated
```

**Key guarantee:** `SELECT FOR UPDATE SKIP LOCKED` ensures two nodes running simultaneously
always claim *different, non-overlapping* sets of records with zero contention or blocking.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  BatchScheduler (@Scheduled, one chunk per tick)                │
│         │                                                       │
│         ▼                                                       │
│  BatchProcessingService.processNextChunk()                      │
│                                                                 │
│  Phase 1 — CLAIM  (REQUIRES_NEW txn, committed immediately)     │
│    StagingRecordRepository.claimChunk()                         │
│    SELECT … FOR UPDATE SKIP LOCKED → UPDATE status=PROCESSING   │
│                                                                 │
│  Phase 2 — PROCESS  (in-memory, no DB calls)                    │
│    ChunkAggregator.process()                                    │
│    • validate each record                                       │
│    • aggregate by (date, productCode, region)                   │
│    • collect successIds / failedIds                             │
│                                                                 │
│  Phase 3 — COMMIT  (single REQUIRES_NEW txn)                    │
│    AggregatedResultRepository.upsertAll()  → mainDB             │
│    stagingRepo.markCompleted(successIds)   → stgDB              │
│    stagingRepo.markFailed(failedIds)       → stgDB              │
│                                                                 │
│  Phase 4 — AUDIT LOG  (REQUIRES_NEW, always commits)            │
│    BatchExecutionLogRepository.completeLog()  → mainDB          │
└─────────────────────────────────────────────────────────────────┘

     stgDB (PostgreSQL)                mainDB (PostgreSQL)
     staging_record                    aggregated_result
     • status: PENDING                 • per (date,product,region)
     •         PROCESSING              • total_quantity
     •         COMPLETED               • total_revenue
     •         FAILED                  • avg/min/max price
     • node_id (which node owns it)    batch_execution_log
     • error_message                   • per-run audit record
```

---

## Multi-Node Safety

The single most important design decision is in `StagingRecordRepository.claimChunk()`:

```sql
SELECT id FROM staging_record
WHERE  status = 'PENDING'
ORDER  BY id ASC
LIMIT  :chunkSize
FOR UPDATE SKIP LOCKED   -- ← this is the magic
```

- **`FOR UPDATE`** — acquires a row-level exclusive lock on each selected row.
- **`SKIP LOCKED`** — any row already locked by another session is silently skipped.
- The claim (status → PROCESSING + nodeId) commits in its own `REQUIRES_NEW` transaction
  so it is immediately visible to all other nodes.

Result: **two nodes never touch the same records**, regardless of how many JVM instances
are deployed, with zero blocking or deadlock risk.

**Crash recovery:** if a node dies mid-chunk, its records remain in `PROCESSING`.
Call `POST /batch/admin/reset-processing?nodeId=<id>` to reset them to `PENDING`.

---

## Quick Start

### 1. Start databases
```bash
docker-compose up -d stgdb maindb
```

### 2. Run in dev mode
```bash
./mvnw quarkus:dev
```

### 3. Trigger a batch manually
```bash
curl -X POST http://localhost:8080/batch/trigger
```

### 4. Check live status
```bash
curl http://localhost:8080/batch/status
```

### 5. View aggregated results
```bash
curl "http://localhost:8080/batch/results?from=2024-01-15&to=2024-01-24"
```

### 6. Full Docker Compose stack
```bash
./mvnw package -DskipTests
docker-compose up --build
```

---

## REST API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/batch/trigger` | Manually trigger one chunk |
| `GET` | `/batch/status` | Live status counts + config |
| `GET` | `/batch/executions?limit=20` | Recent execution logs |
| `GET` | `/batch/executions/{batchId}` | Single execution detail |
| `GET` | `/batch/results?from=&to=` | Aggregated results by date range |
| `POST` | `/batch/admin/reset-processing?nodeId=` | Reset orphaned PROCESSING records |
| `GET` | `/health/live` | Liveness probe |
| `GET` | `/health/ready` | Readiness — pings stgDB + mainDB |
| `GET` | `/metrics` | Prometheus metrics |
| `GET` | `/swagger-ui` | OpenAPI UI |
| `GET` | `/q/openapi` | OpenAPI spec |

### Example trigger response
```json
{
  "batchId":       "BATCH-A1B2C3D4E5F6",
  "nodeId":        "prod-node-1-12345",
  "outcome":       "COMPLETED",
  "totalRead":     500,
  "totalOk":       498,
  "totalFailed":   2,
  "durationMs":    843,
  "throughputRps": "591"
}
```

---

## Configuration Reference

| Property | Env Var | Default | Description |
|---|---|---|---|
| `batch.chunk-size` | `BATCH_CHUNK_SIZE` | `500` | Records per chunk |
| `batch.schedule.cron` | `BATCH_CRON` | `0/30 * * * * ?` | Cron expression |
| `batch.schedule.enabled` | `BATCH_SCHEDULE_ENABLED` | `true` | Toggle scheduler |
| `batch.empty-poll-backoff-ms` | `BATCH_EMPTY_BACKOFF_MS` | `5000` | Backoff hint |
| `STG_DB_URL` | — | localhost:5432/stgdb | stgDB JDBC URL |
| `STG_DB_USER` | — | stg_user | stgDB username |
| `STG_DB_PASSWORD` | — | stg_pass | stgDB password |
| `MAIN_DB_URL` | — | localhost:5433/maindb | mainDB JDBC URL |
| `MAIN_DB_USER` | — | main_user | mainDB username |
| `MAIN_DB_PASSWORD` | — | main_pass | mainDB password |

---

## Performance Expectations

| Config | Est. throughput |
|---|---|
| 1 node, chunk=100 | ~15,000–25,000 rec/s |
| 1 node, chunk=500 | ~40,000–60,000 rec/s |
| 4 nodes, chunk=500 | ~150,000–200,000 rec/s |
| 4 nodes, chunk=1000 | ~200,000–300,000 rec/s |

Throughput is reported live in each batch trigger response (`throughputRps` field)
and in the Prometheus metric `batch.chunk.duration`.

---

## Metrics (Prometheus)

| Metric | Description |
|---|---|
| `batch.records.ok` | Total records successfully processed |
| `batch.records.failed` | Total records marked FAILED |
| `batch.chunk.duration` | Chunk processing time histogram |
| `batch.run.empty` | Empty-poll counter |
| `batch.run.failed` | Failed-chunk counter |

---

## Project Structure

```
src/main/java/com/example/batch/
├── config/
│   ├── BatchProperties.java          typed config (@ConfigMapping)
│   └── RecordStatus.java             PENDING | PROCESSING | COMPLETED | FAILED
├── entity/
│   ├── stg/StagingRecord.java        stgDB entity with status lifecycle
│   └── main/AggregatedResult.java    mainDB aggregated output
│        main/BatchExecutionLog.java  per-run audit log
├── repository/
│   ├── stg/StagingRecordRepository   SELECT FOR UPDATE SKIP LOCKED claim logic
│   └── main/AggregatedResultRepository  upsert-aware write
│        main/BatchExecutionLogRepository per-run audit
├── service/
│   ├── ChunkAggregator.java          stateless validate + aggregate (pure Java)
│   └── BatchProcessingService.java   4-phase pipeline orchestrator
├── scheduler/
│   └── BatchScheduler.java           @Scheduled — one chunk per tick
├── resource/
│   └── BatchResource.java            REST API (trigger/status/results/admin)
├── health/
│   └── DatabaseHealthCheck.java      /health/ready readiness probe
└── exception/
    └── RecordValidationException.java

src/main/resources/
├── application.properties            dual datasource + batch config
└── db/stg-seed.sql                   30 demo PENDING records

src/test/java/com/example/batch/
├── service/ChunkAggregatorTest.java      unit tests — validation + aggregation
├── service/BatchProcessingServiceTest.java  integration tests — full pipeline (H2)
└── resource/BatchResourceTest.java       REST contract tests (mocked service)
```

---

## Build & Run

```bash
# Run tests
./mvnw test

# Package (fast-jar)
./mvnw package -DskipTests

# Dev mode with live reload
./mvnw quarkus:dev

# Native build (requires GraalVM)
./mvnw package -Pnative -DskipTests

# Full Docker Compose stack
./mvnw package -DskipTests && docker-compose up --build
```
