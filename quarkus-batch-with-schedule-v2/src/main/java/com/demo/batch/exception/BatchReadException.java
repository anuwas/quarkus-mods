package com.demo.batch.exception;

/**
 * Thrown when the batch reader encounters an unrecoverable error.
 */
public class BatchReadException extends RuntimeException {
    public BatchReadException(String message) { super(message); }
    public BatchReadException(String message, Throwable cause) { super(message, cause); }
}
