package com.example.batch.exception;

/**
 * Thrown when a single staging record fails business validation.
 * Results in the record being marked FAILED — processing continues for other records.
 */
public class RecordValidationException extends RuntimeException {
    public RecordValidationException(String message) {
        super(message);
    }
    public RecordValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
