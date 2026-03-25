package com.example.batch.dto;

/**
 * Immutable result of a single chunk execution.
 */
public record BatchResult(
    String  batchId,
    String  nodeId,
    String  outcome,       // COMPLETED | COMPLETED_WITH_ERRORS | EMPTY | FAILED | SKIPPED
    int     totalRead,
    int     totalOk,
    int     totalFailed,
    long    durationMs,
    String  errorMessage
) {
    public static BatchResult completed(String b, String n, int read, int ok, int failed, long ms) {
        String status = failed > 0 ? "COMPLETED_WITH_ERRORS" : "COMPLETED";
        return new BatchResult(b, n, status, read, ok, failed, ms, null);
    }
    public static BatchResult empty(String b, String n) {
        return new BatchResult(b, n, "EMPTY", 0, 0, 0, 0, null);
    }
    public static BatchResult failed(String b, String n, int read, String err) {
        return new BatchResult(b, n, "FAILED", read, 0, read, 0, err);
    }
    public static BatchResult skipped(String n) {
        return new BatchResult(null, n, "SKIPPED", 0, 0, 0, 0, "Already executing");
    }
}

