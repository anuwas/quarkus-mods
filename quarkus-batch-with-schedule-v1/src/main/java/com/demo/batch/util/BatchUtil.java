package com.demo.batch.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * General-purpose batch utility methods.
 */
public final class BatchUtil {

    private BatchUtil() {}

    /**
     * Partition a list into sublists of at most {@code size} elements.
     *
     * @param source  source list
     * @param size    max partition size
     * @param <T>     element type
     * @return list of partitions
     */
    public static <T> List<List<T>> partition(List<T> source, int size) {
        if (source == null || source.isEmpty()) return List.of();
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < source.size(); i += size) {
            result.add(source.subList(i, Math.min(i + size, source.size())));
        }
        return result;
    }

    /**
     * Human-friendly elapsed time string, e.g. "1m 23s 456ms".
     */
    public static String formatDuration(long millis) {
        long minutes = millis / 60_000;
        long seconds = (millis % 60_000) / 1_000;
        long ms      = millis % 1_000;
        if (minutes > 0) return "%dm %ds %dms".formatted(minutes, seconds, ms);
        if (seconds > 0) return "%ds %dms".formatted(seconds, ms);
        return "%dms".formatted(ms);
    }

    /**
     * Calculate throughput as records per second.
     */
    public static double throughputPerSecond(long records, long durationMs) {
        if (durationMs <= 0) return 0.0;
        return (records * 1000.0) / durationMs;
    }
}
