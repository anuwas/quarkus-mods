package com.example.batch.repository.main;

import com.example.batch.entity.main.Centre;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Centre operations on mainDB.
 *
 * Uses upsert semantics: if a row already exists for the same centreId
 * the record is updated rather than duplicated. This makes the batch
 * idempotent — safe to retry.
 */
@ApplicationScoped
public class CentreRepository implements PanacheRepositoryBase<Centre, UUID> {

    @Inject
    EntityManager em;

    /**
     * Upsert a list of Centre entities.
     * Must be called inside an active transaction (MANDATORY).
     */
    @Transactional(Transactional.TxType.MANDATORY)
    public void upsertAll(List<Centre> centres) {
        for (Centre centre : centres) {
            findByCentreId(centre.centreId)
                .ifPresentOrElse(
                    existing -> merge(existing, centre),
                    () -> em.persist(centre)
                );
        }
        em.flush();
    }

    /**
     * Find a Centre by its centreId (business key).
     */
    public Optional<Centre> findByCentreId(String centreId) {
        return find("centreId", centreId).firstResultOptional();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void merge(Centre existing, Centre incoming) {
        existing.awardingOrganisationId = incoming.awardingOrganisationId;
        existing.centreName             = incoming.centreName;
        existing.operationalName        = incoming.operationalName;
        existing.centreStatus           = incoming.centreStatus;
        existing.insertedDate           = incoming.insertedDate;
    }
}

