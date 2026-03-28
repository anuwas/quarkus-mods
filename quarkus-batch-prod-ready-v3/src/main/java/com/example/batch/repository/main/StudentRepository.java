package com.example.batch.repository.main;

import com.example.batch.entity.main.Student;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Student operations on mainDB.
 *
 * Uses upsert semantics: if a row already exists for the same studentId
 * the record is updated rather than duplicated. This makes the batch
 * idempotent — safe to retry.
 */
@ApplicationScoped
public class StudentRepository implements PanacheRepositoryBase<Student, String> {

    @Inject
    EntityManager em;

    /**
     * Upsert a list of Student entities.
     * Must be called inside an active transaction (MANDATORY).
     */
    @Transactional(Transactional.TxType.MANDATORY)
    public void upsertAll(List<Student> students) {
        for (Student student : students) {
            findByStudentId(student.studentId)
                .ifPresentOrElse(
                    existing -> merge(existing, student),
                    () -> em.persist(student)
                );
        }
        em.flush();
    }

    /**
     * Find a Student by its studentId (business key / primary key).
     */
    public Optional<Student> findByStudentId(String studentId) {
        return find("studentId", studentId).firstResultOptional();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void merge(Student existing, Student incoming) {
        existing.awardingOrganisationId = incoming.awardingOrganisationId;
        existing.batchId                = incoming.batchId;
        existing.parentCentreId         = incoming.parentCentreId;
        existing.loadTimestamp           = incoming.loadTimestamp;
        existing.studentStatus          = incoming.studentStatus;
        existing.operationalName        = incoming.operationalName;
        existing.studentName            = incoming.studentName;
    }
}

