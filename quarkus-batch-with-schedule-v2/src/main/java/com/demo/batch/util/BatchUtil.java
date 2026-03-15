package com.demo.batch.util;

import java.util.ArrayList;
import java.util.List;

/**
 * General-purpose batch utility methods — V2.
 */
public final class BatchUtil {

    private BatchUtil() {}

    /** Partition a list into sublists of at most {@code size} elements. */
    public static <T> List<List<T>> partition(List<T> source, int size) {
        if (source == null || source.isEmpty()) return List.of();
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < source.size(); i += size) {
            result.add(source.subList(i, Math.min(i + size, source.size())));
        }
        return result;
    }

    /** Human-friendly elapsed time, e.g. "1m 23s 456ms". */
    public static String formatDuration(long millis) {
        long minutes = millis / 60_000;
        long seconds = (millis % 60_000) / 1_000;
        long ms      = millis % 1_000;
        if (minutes > 0) return "%dm %ds %dms".formatted(minutes, seconds, ms);
        if (seconds > 0) return "%ds %dms".formatted(seconds, ms);
        return "%dms".formatted(ms);
    }

    /** Records per second. */
    public static double throughputPerSecond(long records, long durationMs) {
        if (durationMs <= 0) return 0.0;
        return (records * 1000.0) / durationMs;
    }

    /**
     * Recommended chunk size given average row size in bytes and a target heap budget.
     * Useful for tuning batch.chunk-size at deployment time.
     *
     * @param avgRowBytes    estimated average bytes per deserialized row
     * @param heapBudgetMb   max JVM heap you want one chunk to consume (e.g. 64)
     */
    public static int recommendedChunkSize(int avgRowBytes, int heapBudgetMb) {
        long budget = (long) heapBudgetMb * 1024 * 1024;
        return (int) Math.min(budget / Math.max(avgRowBytes, 1), 10_000);
    }
}
