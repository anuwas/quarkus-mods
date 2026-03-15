package com.demo.batch.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Strongly-typed batch configuration backed by application.properties.
 */
@ConfigMapping(prefix = "batch")
public interface BatchConfig {

    @WithName("chunk-size")
    @WithDefault("100")
    int chunkSize();

    @WithName("thread-pool-size")
    @WithDefault("4")
    int threadPoolSize();

    @WithName("retry-attempts")
    @WithDefault("3")
    int retryAttempts();

    @WithName("retry-delay-ms")
    @WithDefault("1000")
    long retryDelayMs();

    @WithName("skip-limit")
    @WithDefault("10")
    int skipLimit();

    Schedule schedule();

    interface Schedule {
        @WithDefault("0 */5 * * * ?")
        String cron();

        @WithDefault("true")
        boolean enabled();
    }
}
