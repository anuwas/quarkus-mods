package com.example.batch.entity.main;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Main DB entity for student data.
 *
 * Lives exclusively in the default (main) persistence unit.
 */
@Entity
@Table(name = "student")
public class Student {

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
    @Column(name = "student_name", nullable = false)
    public String studentName;

    // -----------------------------------------------------------------------
    // String representation
    // -----------------------------------------------------------------------

    @Override
    public String toString() {
        return "Student{studentId='%s', studentName='%s', studentStatus='%s'}".formatted(studentId, studentName, studentStatus);
    }
}

