package com.example.batch.repository.stg;

import com.example.batch.entity.stg.StagingCentre;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.Collections;
import java.util.List;

/**
 * Repository for StagingCentre operations on stgDB.
 *
 * Fetches staging centre data by centre IDs (from staging_synch_log.table_reference).
 */
@ApplicationScoped
@io.quarkus.hibernate.orm.PersistenceUnit("stgdb")
public class StagingCentreRepository implements PanacheRepositoryBase<StagingCentre, String> {

    @Inject
    @io.quarkus.hibernate.orm.PersistenceUnit("stgdb")
    EntityManager em;

    /**
     * Find all staging centres whose centreId is in the given list.
     *
     * @param centreIds list of centre IDs (from staging_synch_log.table_reference)
     * @return list of matching StagingCentre entities
     */
    public List<StagingCentre> findByCentreIds(List<String> centreIds) {
        if (centreIds == null || centreIds.isEmpty()) {
            return Collections.emptyList();
        }
        return list("centreId IN ?1", centreIds);
    }
}

