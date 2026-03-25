package com.example.batch.entity.stg;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Staging entity for centres data.
 *
 * NOTE: This entity lives in the "stgdb" persistence unit only.
 */
@Entity
@Table(name = "stg_centres")
public class StagingCentre {

    @Id
    @NotBlank
    @Column(name = "centre_id", nullable = false, length = 6)
    public String centreId;

    @NotBlank
    @Column(name = "awarding_organisation_id", nullable = false, length = 2)
    public String awardingOrganisationId;

    @NotBlank
    @Column(name = "centre_name", nullable = false, length = 255)
    public String centreName;

    @Column(name = "parent_centre_id", length = 6)
    public String parentCentreId;

    @Column(name = "operational_name", length = 100)
    public String operationalName;

    @Column(name = "centre_status", length = 20)
    public String centreStatus;

    @Column(name = "batch_id")
    public Integer batchId;

    @NotNull
    @Column(name = "load_timestamp", nullable = false)
    public LocalDateTime loadTimestamp;

    // -----------------------------------------------------------------------
    // String representation
    // -----------------------------------------------------------------------

    @Override
    public String toString() {
        return "StagingCentre{centreId='%s', centreName='%s', centreStatus='%s'}".formatted(centreId, centreName, centreStatus);
    }
}

