package com.example.batch.mapper;

import com.example.batch.entity.main.Student;
import com.example.batch.entity.stg.StagingStudent;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper that converts a {@link StagingStudent} (stgDB) into a {@link Student} (mainDB).
 *
 * Stateless — safe for concurrent use.
 */
@ApplicationScoped
public class StudentMapper {

    /**
     * Map a StagingStudent entity to a new Student entity.
     *
     * @param src the staging student record
     * @return a new Student entity ready for persistence in mainDB
     */
    public Student toEntity(StagingStudent src) {
        Student student = new Student();
        student.studentId              = src.studentId;
        student.awardingOrganisationId = src.awardingOrganisationId;
        student.batchId                = src.batchId;
        student.parentCentreId         = src.parentCentreId;
        student.loadTimestamp           = src.loadTimestamp;
        student.studentStatus          = src.studentStatus;
        student.operationalName        = src.operationalName;
        student.studentName            = src.studentName;
        return student;
    }
}

