package com.example.batch.service;

import com.example.batch.dto.ChunkResult;
import com.example.batch.entity.main.Centre;
import com.example.batch.entity.stg.StagingCentre;
import com.example.batch.entity.stg.StagingSynchLog;
import com.example.batch.exception.RecordValidationException;
import com.example.batch.mapper.CentreMapper;
import com.example.batch.repository.stg.StagingCentreRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Stateless service that validates and aggregates a chunk of StagingSynchLogs.
 *
 * Processing flow:
 *   1. Filter records where tableName = "stg_centres"
 *   2. Collect tableReference values (these are centre IDs)
 *   3. Fetch corresponding StagingCentre entities from the stg_centres table
 *   4. Validate each StagingCentre record
 *   5. Map validated StagingCentre → Centre using CentreMapper
 *
 * This class is intentionally free of @Transactional — it performs pure in-memory
 * computation (apart from the read to stg_centres which runs in the caller's transaction).
 */
@ApplicationScoped
public class ChunkAggregator {

    private static final String TABLE_NAME_STG_CENTRES = "stg_centres";

    @Inject
    @io.quarkus.hibernate.orm.PersistenceUnit("stgdb")
    StagingCentreRepository stagingCentreRepo;

    @Inject
    CentreMapper centreMapper;

    // -----------------------------------------------------------------------
    // Core method
    // -----------------------------------------------------------------------

    /**
     * Validate and aggregate a chunk of staging records into Centre entities.
     *
     * @param records   the claimed PROCESSING records from staging_synch_log
     * @param batchId   current batch run identifier (for logging)
     * @param nodeId    current node identifier
     * @return          ChunkResult with Centre entities + success/failure IDs
     */
    public ChunkResult process(List<StagingSynchLog> records, String batchId, String nodeId) {
        List<Centre>       centres    = new ArrayList<>();
        List<Long>         successIds = new ArrayList<>(records.size());
        Map<Long, String>  failedIds  = new LinkedHashMap<>();

        // Step 1: Validate synch log records and group by table name
        List<StagingSynchLog> centreRecords = new ArrayList<>();
        for (StagingSynchLog record : records) {
            try {
                validateSynchLog(record);

                if (TABLE_NAME_STG_CENTRES.equals(record.tableName)) {
                    centreRecords.add(record);
                } else {
                    // Unknown table_name — mark as failed
                    failedIds.put(record.id, "Unsupported table_name: " + record.tableName);
                }
            } catch (RecordValidationException e) {
                Log.warnf("Validation failed for record id=%d: %s", record.id, e.getMessage());
                failedIds.put(record.id, e.getMessage());
            } catch (Exception e) {
                Log.errorf(e, "Unexpected error processing record id=%d", record.id);
                failedIds.put(record.id, "Internal error: " + e.getMessage());
            }
        }

        // Step 2: Fetch StagingCentre records from stg_centres table
        if (!centreRecords.isEmpty()) {
            List<String> centreIds = centreRecords.stream()
                    .map(r -> r.tableReference)
                    .distinct()
                    .toList();

            List<StagingCentre> stagingCentres = stagingCentreRepo.findByCentreIds(centreIds);

            // Index by centreId for quick lookup
            Map<String, StagingCentre> centreMap = stagingCentres.stream()
                    .collect(Collectors.toMap(sc -> sc.centreId, Function.identity(), (a, b) -> a));

            // Step 3: Validate each staging centre and map to Centre
            for (StagingSynchLog record : centreRecords) {
                try {
                    StagingCentre stagingCentre = centreMap.get(record.tableReference);
                    if (stagingCentre == null) {
                        throw new RecordValidationException(
                                "No stg_centres row found for centre_id=" + record.tableReference);
                    }

                    validateStagingCentre(stagingCentre);

                    Centre centre = centreMapper.toEntity(stagingCentre);
                    centres.add(centre);
                    successIds.add(record.id);

                } catch (RecordValidationException e) {
                    Log.warnf("Validation failed for record id=%d (centre_id=%s): %s",
                            record.id, record.tableReference, e.getMessage());
                    failedIds.put(record.id, e.getMessage());
                } catch (Exception e) {
                    Log.errorf(e, "Unexpected error processing record id=%d", record.id);
                    failedIds.put(record.id, "Internal error: " + e.getMessage());
                }
            }
        }

        Log.debugf("Chunk aggregation complete: %d ok, %d failed, %d centres",
                successIds.size(), failedIds.size(), centres.size());

        return new ChunkResult(centres, successIds, failedIds);
    }

    // -----------------------------------------------------------------------
    // Validation — staging_synch_log record
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

    // -----------------------------------------------------------------------
    // Validation — stg_centres record
    // -----------------------------------------------------------------------

    private void validateStagingCentre(StagingCentre sc) {
        List<String> errors = new ArrayList<>();

        if (isBlank(sc.centreId))               errors.add("centreId is blank");
        if (isBlank(sc.awardingOrganisationId)) errors.add("awardingOrganisationId is blank");
        if (isBlank(sc.centreName))             errors.add("centreName is blank");

        if (!errors.isEmpty()) {
            throw new RecordValidationException(
                    "StagingCentre centre_id=" + sc.centreId +
                    " failed validation: " + String.join("; ", errors));
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------


    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
