package com.example.batch.dto;

import com.example.batch.entity.main.Product;

import java.util.List;
import java.util.Map;

/**
 * Result of processing one chunk.
 *
 * @param aggregated   Product objects ready for upsert into mainDB
 * @param successIds   IDs of StagingSynchLogs that passed validation
 * @param failedIds    IDs that failed validation, with their error messages
 */
public record ChunkResult(
    List<Product>     aggregated,
    List<Long>        successIds,
    Map<Long, String> failedIds
) {}

