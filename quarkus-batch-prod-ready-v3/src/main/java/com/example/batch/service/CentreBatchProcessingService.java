package com.example.batch.service;

import com.example.batch.dto.ChunkResult;
import com.example.batch.entity.main.Centre;
import com.example.batch.entity.stg.StagingCentre;
import com.example.batch.entity.stg.StagingSynchLog;
import com.example.batch.exception.RecordValidationException;
import com.example.batch.mapper.CentreMapper;
import com.example.batch.repository.main.CentreRepository;
import com.example.batch.repository.stg.StagingCentreRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Batch processing service for <b>Centre</b> records.
 *
 * <p>Claims only {@code stg_centres} rows from {@code staging_synch_log},
 * validates and maps them via {@link CentreMapper}, and upserts the resulting
 * {@link Centre} entities to mainDB through {@link CentreRepository}.
 *
 * <p>Inherits the full four-phase lifecycle, concurrency guard, metrics, and
 * audit-logging from {@link BatchProcessingService}.
 */
@ApplicationScoped
public class CentreBatchProcessingService extends BatchProcessingService {

    private static final String TABLE_NAME = "stg_centres";

    @Inject
    @io.quarkus.hibernate.orm.PersistenceUnit("stgdb")
    StagingCentreRepository stagingCentreRepo;

    @Inject CentreMapper centreMapper;
    @Inject CentreRepository centreRepo;

    // -----------------------------------------------------------------------
    // Template method implementations
    // -----------------------------------------------------------------------

    @Override
    protected String entityName() {
        return "CENTRE";
    }

    @Override
    protected String tableName() {
        return TABLE_NAME;
    }

    /**
     * Validate synch-log records, fetch corresponding {@link StagingCentre}
     * entities, validate each staging record, and map to {@link Centre}.
     */
    @Override
    protected ChunkResult processRecords(List<StagingSynchLog> records,
                                         String batchId, String nodeId) {
        List<Centre>       centres    = new ArrayList<>();
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
            return new ChunkResult(centres, List.of(), successIds, failedIds);
        }

        // Step 2 — fetch staging centres by table_reference
        List<String> centreIds = validRecords.stream()
                .map(r -> r.tableReference)
                .distinct()
                .toList();

        List<StagingCentre> stagingCentres = stagingCentreRepo.findByCentreIds(centreIds);

        Map<String, StagingCentre> centreMap = stagingCentres.stream()
                .collect(Collectors.toMap(sc -> sc.centreId, Function.identity(), (a, b) -> a));

        // Step 3 — validate each staging centre and map to Centre entity
        for (StagingSynchLog record : validRecords) {
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

        Log.debugf("Centre chunk aggregation complete: %d ok, %d failed",
                successIds.size(), failedIds.size());

        return new ChunkResult(centres, List.of(), successIds, failedIds);
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    protected void commitToMainDb(ChunkResult result) {
        if (!result.centres().isEmpty()) {
            centreRepo.upsertAll(result.centres());
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

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}

