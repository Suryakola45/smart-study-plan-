package com.smartstudyplanner.exception;

/**
 * Runtime exception wrapping underlying SQL and database connectivity failures.
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

