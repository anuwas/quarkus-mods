package com.example.batch.repository.stg;

import com.example.batch.entity.stg.StagingStudent;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.Collections;
import java.util.List;

/**
 * Repository for StagingStudent operations on stgDB.
 *
 * Fetches staging student data by student IDs (from staging_synch_log.table_reference).
 */
@ApplicationScoped
@io.quarkus.hibernate.orm.PersistenceUnit("stgdb")
public class StagingStudentRepository implements PanacheRepositoryBase<StagingStudent, String> {

    @Inject
    @io.quarkus.hibernate.orm.PersistenceUnit("stgdb")
    EntityManager em;

    /**
     * Find all staging students whose studentId is in the given list.
     *
     * @param studentIds list of student IDs (from staging_synch_log.table_reference)
     * @return list of matching StagingStudent entities
     */
    public List<StagingStudent> findByStudentIds(List<String> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Collections.emptyList();
        }
        return list("studentId IN ?1", studentIds);
    }
}

