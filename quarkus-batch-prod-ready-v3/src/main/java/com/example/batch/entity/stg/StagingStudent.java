package com.example.batch.entity.stg;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Staging entity for student data.
 *
 * NOTE: This entity lives in the "stgdb" persistence unit only.
 */
@Entity
@Table(name = "stg_student")
public class StagingStudent {

    @Id
    @NotBlank
    @Column(name = "student_id", nullable = false, length = 6)
    public String studentId;

    @NotBlank
    @Column(name = "awarding_organisation_id", nullable = false, length = 2)
    public String awardingOrganisationId;

    @Column(name = "batch_id")
    public Integer batchId;

    @Column(name = "parent_centre_id", length = 6)
    public String parentCentreId;

    @NotNull
    @Column(name = "load_timestamp", nullable = false)
    public LocalDateTime loadTimestamp;

    @Column(name = "student_status", length = 20)
    public String studentStatus;

    @Column(name = "operational_name", length = 100)
    public String operationalName;

    @NotBlank
    @Column(name = "student_name", nullable = false, length = 255)
    public String studentName;

    // -----------------------------------------------------------------------
    // String representation
    // -----------------------------------------------------------------------

    @Override
    public String toString() {
        return "StagingStudent{studentId='%s', studentName='%s', studentStatus='%s'}".formatted(studentId, studentName, studentStatus);
    }
}

