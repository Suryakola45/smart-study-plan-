package com.smartstudyplanner.exception;

/**
 * Exception thrown when study plan generation encounters an unrecoverable failure
 * (e.g., empty topic list, conflicting constraints, or strategy execution error).
 */
public class PlanGenerationException extends Exception {

    public PlanGenerationException(String message) {
        super(message);
    }

    public PlanGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
