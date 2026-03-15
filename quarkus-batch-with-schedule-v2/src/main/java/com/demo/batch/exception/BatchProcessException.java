package com.demo.batch.exception;

public class BatchProcessException extends RuntimeException {
    public BatchProcessException(String message) { super(message); }
    public BatchProcessException(String message, Throwable cause) { super(message, cause); }
}
