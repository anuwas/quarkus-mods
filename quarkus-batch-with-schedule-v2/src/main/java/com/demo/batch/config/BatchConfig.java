package com.demo.batch.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Strongly-typed batch configuration — V2.
 *
 * New settings:
 *  - chunk-size bumped default to 1000
 *  - thread-pool-size drives parallel chunk workers
 *  - skip-limit-pct: percentage of chunk size that may be skipped before aborting
 *  - progress-log-interval: how often (in chunks) to log a progress line
 */
@ConfigMapping(prefix = "batch")
public interface BatchConfig {

    @WithName("chunk-size")
    @WithDefault("1000")
    int chunkSize();

    @WithName("thread-pool-size")
    @WithDefault("8")
    int threadPoolSize();

    @WithName("retry-attempts")
    @WithDefault("3")
    int retryAttempts();

    @WithName("retry-delay-ms")
    @WithDefault("1000")
    long retryDelayMs();

    /** Skip limit expressed as a percentage of chunk size (e.g. 5 = 5%). */
    @WithName("skip-limit-pct")
    @WithDefault("5")
    int skipLimitPct();

    /** Log a progress line every N chunks processed. */
    @WithName("progress-log-interval")
    @WithDefault("10")
    int progressLogInterval();

    Schedule schedule();

    interface Schedule {
        @WithDefault("0 */5 * * * ?")
        String cron();

        @WithDefault("true")
        boolean enabled();
    }
}
