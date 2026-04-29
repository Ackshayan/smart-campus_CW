package com.smartcampus.exception;

/**
 * Thrown when a sensor is created referencing a roomId that does not exist.
 * The payload is syntactically valid JSON, but semantically unprocessable
 * because the linked resource (room) is missing.
 * Mapped to HTTP 422 Unprocessable Entity by LinkedResourceNotFoundExceptionMapper.
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
