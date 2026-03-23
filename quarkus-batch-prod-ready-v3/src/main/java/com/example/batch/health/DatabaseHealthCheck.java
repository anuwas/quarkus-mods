package com.example.batch.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Readiness health check — verifies connectivity to both stgDB and mainDB.
 * Exposed at GET /health/ready.
 */
@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    @Inject
    @io.quarkus.hibernate.orm.PersistenceUnit("stgdb")
    EntityManager stgEm;

    @Inject
    @io.quarkus.hibernate.orm.PersistenceUnit("maindb")
    EntityManager mainEm;

    @Override
    public HealthCheckResponse call() {
        try {
            stgEm.createNativeQuery("SELECT 1").getSingleResult();
            mainEm.createNativeQuery("SELECT 1").getSingleResult();
            return HealthCheckResponse.named("databases")
                    .up()
                    .withData("stgdb",  "UP")
                    .withData("maindb", "UP")
                    .build();
        } catch (Exception e) {
            return HealthCheckResponse.named("databases")
                    .down()
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}
