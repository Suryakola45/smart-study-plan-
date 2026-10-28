package com.smartstudyplanner.exception;

/**
 * Exception thrown when student's declared availability is insufficient
 * to cover pending high-priority topics before their examination dates.
 */
public class InsufficientStudyTimeException extends Exception {

    private final int requiredMinutes;
    private final int availableMinutes;

    public InsufficientStudyTimeException(int requiredMinutes, int availableMinutes) {
        super(String.format("Insufficient study time: required %d minutes, but only %d minutes available in scheduled slots.",
                requiredMinutes, availableMinutes));
        this.requiredMinutes = requiredMinutes;
        this.availableMinutes = availableMinutes;
    }

    public InsufficientStudyTimeException(String message) {
        super(message);
        this.requiredMinutes = 0;
        this.availableMinutes = 0;
    }

    public int getRequiredMinutes() {
        return requiredMinutes;
    }

    public int getAvailableMinutes() {
        return availableMinutes;
    }

    public int getDeficitMinutes() {
        return Math.max(0, requiredMinutes - availableMinutes);
    }
}
