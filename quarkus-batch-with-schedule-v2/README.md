# Quarkus Batch Demo — V2 (High-Volume Edition)

High-performance batch processing with **Quarkus 3.9**, **Panache ORM**, **Java 21**, and dual PostgreSQL databases.
Designed to process **millions of rows** with predictable latency and minimal DB load.

---

## What changed from V1

| # | Problem in V1 | Fix in V2 |
|---|---|---|
| 1 | `OFFSET` pagination — O(N) scan per page | **Keyset pagination** — `WHERE id > lastId`, always O(1) |
| 2 | `COUNT(*)` on startup — full table scan | **Removed** — loop self-terminates on empty page; O(1) pg_class estimate for logs |
| 3 | Single-threaded pipeline | **Parallel chunk workers** via `ManagedExecutor` (default 8 threads) |
| 4 | `IN (...)` bulk update — breaks above ~1000 IDs | **Temp-table join** — `CREATE TEMP TABLE … UPDATE … FROM` — one round trip |
| 5 | Skip limit = 10 (fixed) | **Skip limit as % of chunk** — `batch.skip-limit-pct=5` scales automatically |
| 6 | Chunk size = 100 | **Chunk size = 1000** — 10x fewer DB round trips per million rows |

---

## Architecture

```
┌───────────────────────────────────────────────────────────────────┐
│  SalesBatchJobService V2 (coordinator — main thread)              │
│                                                                   │
│  SalesTransactionReader                                           │
│  readPage(lastSeenId, chunkSize)  ◄── keyset cursor advances      │
│        │                                                          │
│        │  List<StgSalesTransaction>  (chunk)                      │
│        ▼                                                          │
│  ManagedExecutor  ──► Worker 1: aggregate() → write()             │
│  (N parallel)     ──► Worker 2: aggregate() → write()             │
│                   ──► Worker N: aggregate() → write()             │
│                                                                   │
│  [stgdb] stg_sales_transaction     [mndb] mn_daily_sales_summary  │
│          status: PENDING → PROCESSED       (upsert per run)       │
└───────────────────────────────────────────────────────────────────┘
```

---

## Quick Start

### 1. Start databases
```bash
docker-compose up -d stgdb mndb
```

### 2. Run in dev mode
```bash
./mvnw quarkus:dev
```

### 3. Trigger a batch run
```bash
curl -X POST http://localhost:8080/batch/run
```

### Example response
```json
{
  "runId": "RUN-A1B2C3D4",
  "status": "COMPLETED",
  "recordsRead": 1000000,
  "recordsWritten": 42381,
  "chunksProcessed": 1000,
  "durationMs": 28400,
  "durationHuman": "28s 400ms",
  "throughputPerSec": "35211"
}
```

---

## Configuration Reference

| Property | Env Var | Default | Description |
|---|---|---|---|
| `batch.chunk-size` | `BATCH_CHUNK_SIZE` | `1000` | Records per chunk |
| `batch.thread-pool-size` | `BATCH_THREAD_POOL_SIZE` | `8` | Parallel workers |
| `batch.retry-attempts` | — | `3` | Retries per chunk on failure |
| `batch.skip-limit-pct` | `BATCH_SKIP_LIMIT_PCT` | `5` | Max skipped rows as % of chunk |
| `batch.progress-log-interval` | — | `10` | Log progress every N chunks |
| `batch.schedule.cron` | `BATCH_CRON` | `0 */5 * * * ?` | Cron expression |
| `batch.schedule.enabled` | `BATCH_SCHEDULE_ENABLED` | `true` | Enable/disable scheduler |
| `quarkus.datasource.stgdb.jdbc.url` | `STG_DB_URL` | localhost:5432/stgdb | STG JDBC URL |
| `quarkus.datasource.mndb.jdbc.url` | `MN_DB_URL` | localhost:5433/mndb | MN JDBC URL |

### Chunk size tuning guide

```
Use BatchUtil.recommendedChunkSize(avgRowBytes, heapBudgetMb) at runtime.

Rule of thumb:
  chunk_size × avg_row_bytes < 50 MB  (fits comfortably in default heap)

Typical values:
  Thin rows  (~200 bytes): chunk-size=2000
  Medium rows (~500 bytes): chunk-size=1000  ← default
  Wide rows (~2000 bytes): chunk-size=250
```

---

## REST API

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/batch/run` | Trigger manual run (synchronous) |
| `GET` | `/batch/status` | App + DB stats + approx row count |
| `GET` | `/batch/jobs?limit=20` | Recent job logs |
| `GET` | `/batch/jobs/{runId}` | Specific job log |
| `GET` | `/batch/summaries?from=2024-01-01&to=2024-01-31` | Aggregated results |
| `GET` | `/health/ready` | Readiness — checks both DBs |
| `GET` | `/metrics` | Prometheus metrics |

---

## Performance Expectations

| Config | Est. throughput |
|---|---|
| V1 defaults (chunk=100, 1 thread, OFFSET) | ~5K–10K rows/sec |
| V2 defaults (chunk=1000, 8 threads, keyset) | ~200K–400K rows/sec |
| V2 + read replica for stgdb | ~500K+ rows/sec |

At 300K rows/sec: **10M rows in ~33 seconds**.

---

## Project Structure

```
src/main/java/com/demo/batch/
├── config/      BatchConfig.java               typed config (chunk-size, threads, skip-limit-pct)
├── entity/
│   ├── stg/     StgSalesTransaction.java        keyset query: findPendingAfter(lastId, size)
│   └── mn/      MnDailySalesSummary.java        upsert target
│                MnBatchJobLog.java               audit log
├── model/       BatchContext.java               thread-safe AtomicLong counters + TPS
│                AggregatedSalesData.java         intermediate aggregate
├── reader/      SalesTransactionReader.java     keyset-based page reader
├── processor/   SalesDataAggregator.java        stateless, parallel-safe
├── writer/      SalesSummaryWriter.java         upsert + temp-table bulk update
├── service/     SalesBatchJobService.java       coordinator: ManagedExecutor parallel workers
│                BatchMetricsService.java         job log persistence + Prometheus
├── scheduler/   BatchScheduler.java             @Scheduled cron trigger
├── resource/    BatchJobResource.java           REST API (includes throughput in response)
├── exception/   Batch*Exception.java
└── util/        BatchUtil.java                  partition, formatDuration, recommendedChunkSize
                 DatabaseHealthCheck.java         /health/ready — pings both DBs
```

---

## Build & Run

```bash
# Run tests
./mvnw test

# Package
./mvnw package -DskipTests

# Full Docker Compose stack
./mvnw package -DskipTests
docker-compose up --build

# Native build (GraalVM)
./mvnw package -Pnative -DskipTests
```
