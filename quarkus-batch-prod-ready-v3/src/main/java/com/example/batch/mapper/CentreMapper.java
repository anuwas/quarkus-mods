package com.example.batch.mapper;

import com.example.batch.entity.main.Centre;
import com.example.batch.entity.stg.StagingCentre;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper that converts a {@link StagingCentre} (stgDB) into a {@link Centre} (mainDB).
 *
 * Stateless — safe for concurrent use.
 */
@ApplicationScoped
public class CentreMapper {

    /**
     * Map a StagingCentre entity to a new Centre entity.
     *
     * @param src the staging centre record
     * @return a new Centre entity ready for persistence in mainDB
     */
    public Centre toEntity(StagingCentre src) {
        Centre centre = new Centre();
        centre.centresUuid            = UUID.randomUUID();
        centre.awardingOrganisationId = src.awardingOrganisationId;
        centre.centreId               = src.centreId;
        centre.centreName             = src.centreName;
        centre.operationalName        = src.operationalName;
        centre.centreStatus           = src.centreStatus;
        centre.insertedDate           = LocalDateTime.now();
        return centre;
    }
}

