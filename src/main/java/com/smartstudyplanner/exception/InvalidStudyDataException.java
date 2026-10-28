package com.smartstudyplanner.exception;

/**
 * Exception thrown when domain data violates business validation rules
 * (e.g., negative duration, invalid difficulty level, inverted dates).
 */
public class InvalidStudyDataException extends Exception {
    
    private final String fieldName;

    public InvalidStudyDataException(String message) {
        super(message);
        this.fieldName = null;
    }

    public InvalidStudyDataException(String fieldName, String message) {
        super("Validation error on '" + fieldName + "': " + message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
