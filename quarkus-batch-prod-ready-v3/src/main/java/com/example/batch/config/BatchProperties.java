package com.example.batch.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Strongly-typed configuration for the batch pipeline.
 * All values are overridable via environment variables.
 */
@ConfigMapping(prefix = "batch")
public interface BatchProperties {

    /** Number of records claimed and processed per scheduled execution. */
    @WithName("chunk-size")
    @WithDefault("500")
    int chunkSize();

    /** Back-off delay (ms) after an empty poll before the scheduler considers the next cycle. */
    @WithName("empty-poll-backoff-ms")
    @WithDefault("5000")
    long emptyPollBackoffMs();

    Schedule schedule();

    interface Schedule {
        @WithDefault("0/30 * * * * ?")
        String cron();

        @WithDefault("true")
        boolean enabled();

        CentreSchedule centre();

        StudentSchedule student();
    }

    interface CentreSchedule {
        @WithDefault("0/30 * * * * ?")
        String cron();

        @WithDefault("true")
        boolean enabled();
    }

    interface StudentSchedule {
        @WithDefault("0/30 * * * * ?")
        String cron();

        @WithDefault("true")
        boolean enabled();
    }
}
