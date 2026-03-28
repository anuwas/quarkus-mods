package com.example.batch.dto;

import com.example.batch.entity.main.Centre;
import com.example.batch.entity.main.Student;

import java.util.List;
import java.util.Map;

/**
 * Result of processing one chunk.
 *
 * @param centres      Centre objects ready for upsert into mainDB (centres table)
 * @param students     Student objects ready for upsert into mainDB (student table)
 * @param successIds   IDs of StagingSynchLogs that passed validation
 * @param failedIds    IDs that failed validation, with their error messages
 */
public record ChunkResult(
    List<Centre>      centres,
    List<Student>     students,
    List<Long>        successIds,
    Map<Long, String> failedIds
) {}

