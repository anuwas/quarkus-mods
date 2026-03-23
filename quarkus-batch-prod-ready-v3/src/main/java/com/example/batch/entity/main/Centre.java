package com.example.batch.entity.main;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Main DB entity for centres data.
 *
 * Lives exclusively in the "maindb" persistence unit.
 */
@Entity
@Table(name = "centres")
public class Centre extends PanacheEntityBase {

    @Id
    @NotNull
    @Column(name = "centres_uuid", nullable = false)
    public UUID centresUuid;

    @NotBlank
    @Column(name = "awarding_organisation_id", nullable = false, length = 2)
    public String awardingOrganisationId;

    @NotBlank
    @Column(name = "centre_id", nullable = false, length = 6)
    public String centreId;

    @NotBlank
    @Column(name = "centre_name", nullable = false, length = 255)
    public String centreName;

    @Column(name = "operational_name", length = 100)
    public String operationalName;

    @Column(name = "centre_status", length = 20)
    public String centreStatus;

    @Column(name = "inserted_date")
    public LocalDateTime insertedDate;

    // -----------------------------------------------------------------------
    // String representation
    // -----------------------------------------------------------------------

    @Override
    public String toString() {
        return "Centre{centresUuid='%s', centreId='%s', centreName='%s', centreStatus='%s'}".formatted(centresUuid, centreId, centreName, centreStatus);
    }
}

