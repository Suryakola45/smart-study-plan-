package com.smartstudyplanner.service;

import com.smartstudyplanner.model.Topic;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * AI Recommendation Engine component responsible for calculating dynamic topic priorities.
 *
 * Implements the normalized rule-based priority formula:
 * Priority = 0.35*D + 0.30*U + 0.20*W + 0.15*P
 *
 * Where:
 * - D (Difficulty): Normalized between [0.2, 1.0] based on difficulty level (1-5)
 * - U (Urgency): Normalized between [0.05, 1.0] based on days until exam date
 * - W (Pending Work): Normalized between [0.0, 1.0] representing remaining study ratio
 * - P (Confidence Deficit): Normalized between [0.0, 1.0] where lower confidence yields higher study need
 */
public class PriorityCalculator {

    public static final double WEIGHT_DIFFICULTY = 0.35;
    public static final double WEIGHT_URGENCY = 0.30;
    public static final double WEIGHT_WORKLOAD = 0.20;
    public static final double WEIGHT_CONFIDENCE_DEFICIT = 0.15;
    public static final int MAX_PLANNING_HORIZON_DAYS = 30;

    /**
     * Calculates the normalized priority score (0.0 to 1.0) for a given topic.
     *
     * @param topic Topic entity containing difficulty, confidence, and progress
     * @param examDate Subject examination date
     * @param referenceDate Base date (usually today) for calculating urgency
     * @return Double score between 0.0 and 1.0
     */
    public static double calculate(Topic topic, LocalDate examDate, LocalDate referenceDate) {
        if (topic == null) {
            return 0.0;
        }

        // If topic is already marked completed, priority is 0
        if (topic.isCompleted()) {
            return 0.0;
        }

        double d = normalizeDifficulty(topic.getDifficulty());
        double u = normalizeUrgency(examDate, referenceDate);
        double w = normalizeWorkload(topic.getEstimatedMinutes(), topic.getCompletedMinutes());
        double p = normalizeConfidenceDeficit(topic.getConfidenceLevel());

        double priority = (WEIGHT_DIFFICULTY * d) +
                          (WEIGHT_URGENCY * u) +
                          (WEIGHT_WORKLOAD * w) +
                          (WEIGHT_CONFIDENCE_DEFICIT * p);

        // Round to 3 decimal places and clamp between 0.0 and 1.0
        return Math.max(0.0, Math.min(1.0, Math.round(priority * 1000.0) / 1000.0));
    }

    /**
     * Overload defaulting to today's date and the topic's embedded exam date.
     */
    public static double calculate(Topic topic) {
        return calculate(topic, topic != null ? topic.getExamDate() : null, LocalDate.now());
    }

    /**
     * Normalizes difficulty (1 to 5) into [0.2, 1.0].
     */
    public static double normalizeDifficulty(int difficulty) {
        int clamped = Math.max(1, Math.min(5, difficulty));
        return clamped / 5.0;
    }

    /**
     * Normalizes urgency based on days remaining until exam.
     * 0 or negative days -> 1.0 (Maximum urgency)
     * >= 30 days -> 0.05 (Low baseline urgency)
     * Intermediate days scale linearly.
     */
    public static double normalizeUrgency(LocalDate examDate, LocalDate referenceDate) {
        if (examDate == null || referenceDate == null) {
            return 0.5; // neutral fallback
        }
        long days = ChronoUnit.DAYS.between(referenceDate, examDate);
        if (days <= 0) {
            return 1.0;
        }
        if (days >= MAX_PLANNING_HORIZON_DAYS) {
            return 0.05;
        }
        double ratio = (double) days / MAX_PLANNING_HORIZON_DAYS;
        double urgency = 1.0 - (ratio * 0.95);
        return Math.max(0.05, Math.min(1.0, urgency));
    }

    /**
     * Normalizes pending workload based on remaining minutes / estimated minutes.
     * 100% remaining -> 1.0
     * 50% remaining -> 0.5
     * 0% remaining -> 0.0
     */
    public static double normalizeWorkload(int estimatedMinutes, int completedMinutes) {
        if (estimatedMinutes <= 0) {
            return 0.0;
        }
        int remaining = Math.max(0, estimatedMinutes - Math.max(0, completedMinutes));
        return Math.min(1.0, (double) remaining / estimatedMinutes);
    }

    /**
     * Normalizes confidence deficit (1 = very low confidence -> 1.0 deficit; 5 = mastered -> 0.0 deficit).
     */
    public static double normalizeConfidenceDeficit(int confidenceLevel) {
        int clamped = Math.max(1, Math.min(5, confidenceLevel));
        return (5.0 - clamped) / 4.0;
    }

    /**
     * Returns a detailed calculation breakdown for student explanation.
     */
    public static Explanation explain(Topic topic, LocalDate examDate, LocalDate referenceDate) {
        double d = normalizeDifficulty(topic != null ? topic.getDifficulty() : 3);
        double u = normalizeUrgency(examDate, referenceDate);
        double w = normalizeWorkload(topic != null ? topic.getEstimatedMinutes() : 60,
                                     topic != null ? topic.getCompletedMinutes() : 0);
        double p = normalizeConfidenceDeficit(topic != null ? topic.getConfidenceLevel() : 3);
        double total = calculate(topic, examDate, referenceDate);

        return new Explanation(d, u, w, p, total);
    }

    public static class Explanation {
        public final double d;
        public final double u;
        public final double w;
        public final double p;
        public final double total;

        public Explanation(double d, double u, double w, double p, double total) {
            this.d = Math.round(d * 100.0) / 100.0;
            this.u = Math.round(u * 100.0) / 100.0;
            this.w = Math.round(w * 100.0) / 100.0;
            this.p = Math.round(p * 100.0) / 100.0;
            this.total = total;
        }

        public String getFormulaString() {
            return String.format("0.35*(%.2f) + 0.30*(%.2f) + 0.20*(%.2f) + 0.15*(%.2f) = %.2f",
                    d, u, w, p, total);
        }
    }
}

