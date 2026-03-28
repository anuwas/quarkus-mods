package com.example.batch.service;

import com.example.batch.dto.ChunkResult;
import com.example.batch.entity.main.Student;
import com.example.batch.entity.stg.StagingStudent;
import com.example.batch.entity.stg.StagingSynchLog;
import com.example.batch.exception.RecordValidationException;
import com.example.batch.mapper.StudentMapper;
import com.example.batch.repository.main.StudentRepository;
import com.example.batch.repository.stg.StagingStudentRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Batch processing service for <b>Student</b> records.
 *
 * <p>Claims only {@code stg_student} rows from {@code staging_synch_log},
 * validates and maps them via {@link StudentMapper}, and upserts the resulting
 * {@link Student} entities to mainDB through {@link StudentRepository}.
 *
 * <p>Inherits the full four-phase lifecycle, concurrency guard, metrics, and
 * audit-logging from {@link BatchProcessingService}.
 */
@ApplicationScoped
public class StudentBatchProcessingService extends BatchProcessingService {

    private static final String TABLE_NAME = "stg_student";

    @Inject
    @io.quarkus.hibernate.orm.PersistenceUnit("stgdb")
    StagingStudentRepository stagingStudentRepo;

    @Inject StudentMapper studentMapper;
    @Inject StudentRepository studentRepo;

    // -----------------------------------------------------------------------
    // Template method implementations
    // -----------------------------------------------------------------------

    @Override
    protected String entityName() {
        return "STUDENT";
    }

    @Override
    protected String tableName() {
        return TABLE_NAME;
    }

    /**
     * Validate synch-log records, fetch corresponding {@link StagingStudent}
     * entities, validate each staging record, and map to {@link Student}.
     */
    @Override
    protected ChunkResult processRecords(List<StagingSynchLog> records,
                                         String batchId, String nodeId) {
        List<Student>      students   = new ArrayList<>();
        List<Long>         successIds = new ArrayList<>(records.size());
        Map<Long, String>  failedIds  = new LinkedHashMap<>();

        // Step 1 — validate synch-log records
        List<StagingSynchLog> validRecords = new ArrayList<>();
        for (StagingSynchLog record : records) {
            try {
                validateSynchLog(record);
                validRecords.add(record);
            } catch (RecordValidationException e) {
                Log.warnf("Validation failed for record id=%d: %s", record.id, e.getMessage());
                failedIds.put(record.id, e.getMessage());
            } catch (Exception e) {
                Log.errorf(e, "Unexpected error validating record id=%d", record.id);
                failedIds.put(record.id, "Internal error: " + e.getMessage());
            }
        }

        if (validRecords.isEmpty()) {
            return new ChunkResult(List.of(), students, successIds, failedIds);
        }

        // Step 2 — fetch staging students by table_reference
        List<String> studentIds = validRecords.stream()
                .map(r -> r.tableReference)
                .distinct()
                .toList();

        List<StagingStudent> stagingStudents = stagingStudentRepo.findByStudentIds(studentIds);

        Map<String, StagingStudent> studentMap = stagingStudents.stream()
                .collect(Collectors.toMap(ss -> ss.studentId, Function.identity(), (a, b) -> a));

        // Step 3 — validate each staging student and map to Student entity
        for (StagingSynchLog record : validRecords) {
            try {
                StagingStudent stagingStudent = studentMap.get(record.tableReference);
                if (stagingStudent == null) {
                    throw new RecordValidationException(
                            "No stg_student row found for student_id=" + record.tableReference);
                }

                validateStagingStudent(stagingStudent);

                Student student = studentMapper.toEntity(stagingStudent);
                students.add(student);
                successIds.add(record.id);

            } catch (RecordValidationException e) {
                Log.warnf("Validation failed for record id=%d (student_id=%s): %s",
                        record.id, record.tableReference, e.getMessage());
                failedIds.put(record.id, e.getMessage());
            } catch (Exception e) {
                Log.errorf(e, "Unexpected error processing record id=%d", record.id);
                failedIds.put(record.id, "Internal error: " + e.getMessage());
            }
        }

        Log.debugf("Student chunk aggregation complete: %d ok, %d failed",
                successIds.size(), failedIds.size());

        return new ChunkResult(List.of(), students, successIds, failedIds);
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    protected void commitToMainDb(ChunkResult result) {
        if (!result.students().isEmpty()) {
            studentRepo.upsertAll(result.students());
        }
    }

    // -----------------------------------------------------------------------
    // Validation
    // -----------------------------------------------------------------------

    private void validateSynchLog(StagingSynchLog r) {
        List<String> errors = new ArrayList<>();
        if (isBlank(r.tableName))      errors.add("tableName is blank");
        if (isBlank(r.tableReference)) errors.add("tableReference is blank");
        if (!errors.isEmpty()) {
            throw new RecordValidationException(
                    "Record id=" + r.id + " failed validation: " + String.join("; ", errors));
        }
    }

    private void validateStagingStudent(StagingStudent ss) {
        List<String> errors = new ArrayList<>();
        if (isBlank(ss.studentId))              errors.add("studentId is blank");
        if (isBlank(ss.awardingOrganisationId)) errors.add("awardingOrganisationId is blank");
        if (isBlank(ss.studentName))            errors.add("studentName is blank");
        if (!errors.isEmpty()) {
            throw new RecordValidationException(
                    "StagingStudent student_id=" + ss.studentId +
                    " failed validation: " + String.join("; ", errors));
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}

