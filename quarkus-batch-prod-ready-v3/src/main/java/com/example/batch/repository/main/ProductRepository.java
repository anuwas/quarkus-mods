package com.example.batch.repository.main;

import com.example.batch.entity.main.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Product operations on mainDB.
 *
 * Uses upsert semantics: if a row already exists for the aggregation key
 * (product_name) the metrics are merged rather
 * than duplicated.  This makes the batch idempotent — safe to retry.
 */
@ApplicationScoped
public class ProductRepository {

    @Inject
    @io.quarkus.hibernate.orm.PersistenceUnit("maindb")
    EntityManager em;

    /**
     * Upsert a list of aggregated results.
     * Must be called inside an active transaction (MANDATORY).
     */
    @Transactional(Transactional.TxType.MANDATORY)
    public void upsertAll(List<Product> results) {
        for (Product result : results) {
            findByKey(result.productName)
                .ifPresentOrElse(
                    existing -> merge(existing, result),
                    () -> em.persist(result)
                );
        }
        em.flush();
    }

    /**
     * Find a Product by its product_name using the maindb EntityManager.
     */
    private Optional<Product> findByKey(String productName) {
        return em.createQuery("SELECT p FROM Product p WHERE p.productName = :productName", Product.class)
                .setParameter("productName", productName)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void merge(Product existing, Product incoming) {
        existing.totalQuantity        = existing.totalQuantity + incoming.totalQuantity;
        existing.transactionCount     = existing.transactionCount + incoming.transactionCount;
        existing.batchId = incoming.batchId;  // update to latest batch
        existing.nodeId  = incoming.nodeId;
    }
}

