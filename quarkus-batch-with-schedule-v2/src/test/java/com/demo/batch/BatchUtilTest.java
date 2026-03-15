package com.demo.batch;

import com.demo.batch.util.BatchUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BatchUtilTest {

    @Test void testPartition() {
        var parts = BatchUtil.partition(java.util.List.of(1,2,3,4,5), 2);
        assertEquals(3, parts.size());
        assertEquals(java.util.List.of(1,2), parts.get(0));
        assertEquals(java.util.List.of(5),   parts.get(2));
    }

    @Test void testPartitionEmpty() {
        assertTrue(BatchUtil.partition(java.util.List.of(), 10).isEmpty());
    }

    @Test void testFormatDuration() {
        assertEquals("500ms",     BatchUtil.formatDuration(500));
        assertEquals("5s 200ms",  BatchUtil.formatDuration(5200));
        assertEquals("1m 5s 0ms", BatchUtil.formatDuration(65000));
    }

    @Test void testThroughput() {
        assertEquals(500.0, BatchUtil.throughputPerSecond(1000, 2000), 0.001);
    }

    @Test void testRecommendedChunkSize() {
        // 64MB budget / 512 bytes per row = 131072, capped at 10000
        assertEquals(10000, BatchUtil.recommendedChunkSize(512, 64));
        // 10MB budget / 10000 bytes per row = 1048
        assertEquals(1048, BatchUtil.recommendedChunkSize(10000, 10));
    }
}
