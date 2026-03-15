package com.demo.batch.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Readiness probe — verifies both stgdb and mndb are reachable.
 * Exposed at GET /health/ready (via SmallRye Health).
 */
@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    @Inject
    @io.quarkus.hibernate.orm.PersistenceUnit("stgdb")
    EntityManager stgEm;

    @Inject
    @io.quarkus.hibernate.orm.PersistenceUnit("mndb")
    EntityManager mnEm;

    @Override
    public HealthCheckResponse call() {
        try {
            stgEm.createNativeQuery("SELECT 1").getSingleResult();
            mnEm.createNativeQuery("SELECT 1").getSingleResult();
            return HealthCheckResponse.named("databases")
                    .up()
                    .withData("stgdb", "UP")
                    .withData("mndb", "UP")
                    .build();
        } catch (Exception e) {
            return HealthCheckResponse.named("databases")
                    .down()
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}
