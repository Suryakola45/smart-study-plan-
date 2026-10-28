package com.smartstudyplanner.service;

import com.smartstudyplanner.model.Topic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AI Priority Recommendation Engine Tests")
class PriorityCalculatorTest {

    @Test
    @DisplayName("Difficulty normalization should map [1, 5] to [0.2, 1.0]")
    void testNormalizeDifficulty() {
        assertEquals(0.2, PriorityCalculator.normalizeDifficulty(1), 0.001);
        assertEquals(0.4, PriorityCalculator.normalizeDifficulty(2), 0.001);
        assertEquals(0.6, PriorityCalculator.normalizeDifficulty(3), 0.001);
        assertEquals(0.8, PriorityCalculator.normalizeDifficulty(4), 0.001);
        assertEquals(1.0, PriorityCalculator.normalizeDifficulty(5), 0.001);

        // Clamping tests
        assertEquals(0.2, PriorityCalculator.normalizeDifficulty(0), 0.001);
        assertEquals(1.0, PriorityCalculator.normalizeDifficulty(10), 0.001);
    }

    @Test
    @DisplayName("Urgency normalization should yield 1.0 for today/past exams and 0.05 for distant exams")
    void testNormalizeUrgency() {
        LocalDate today = LocalDate.now();

        // Exam today -> maximum urgency
        assertEquals(1.0, PriorityCalculator.normalizeUrgency(today, today), 0.001);

        // Exam in past -> maximum urgency
        assertEquals(1.0, PriorityCalculator.normalizeUrgency(today.minusDays(2), today), 0.001);

        // Exam 30+ days away -> minimal urgency (0.05)
        assertEquals(0.05, PriorityCalculator.normalizeUrgency(today.plusDays(30), today), 0.001);
        assertEquals(0.05, PriorityCalculator.normalizeUrgency(today.plusDays(60), today), 0.001);

        // Null exam date fallback
        assertEquals(0.5, PriorityCalculator.normalizeUrgency(null, today), 0.001);
    }

    @Test
    @DisplayName("Workload normalization should reflect pending study ratio")
    void testNormalizeWorkload() {
        // 0 of 100 mins completed -> 1.0 workload remaining
        assertEquals(1.0, PriorityCalculator.normalizeWorkload(100, 0), 0.001);

        // 50 of 100 mins completed -> 0.5 workload remaining
        assertEquals(0.5, PriorityCalculator.normalizeWorkload(100, 50), 0.001);

        // 100 of 100 mins completed -> 0.0 workload remaining
        assertEquals(0.0, PriorityCalculator.normalizeWorkload(100, 100), 0.001);

        // Over-completed mins -> clamped to 0.0
        assertEquals(0.0, PriorityCalculator.normalizeWorkload(100, 120), 0.001);
    }

    @Test
    @DisplayName("Confidence deficit should invert confidence: 1 -> 1.0 (high need), 5 -> 0.0 (mastered)")
    void testNormalizeConfidenceDeficit() {
        assertEquals(1.0, PriorityCalculator.normalizeConfidenceDeficit(1), 0.001);
        assertEquals(0.75, PriorityCalculator.normalizeConfidenceDeficit(2), 0.001);
        assertEquals(0.50, PriorityCalculator.normalizeConfidenceDeficit(3), 0.001);
        assertEquals(0.25, PriorityCalculator.normalizeConfidenceDeficit(4), 0.001);
        assertEquals(0.0, PriorityCalculator.normalizeConfidenceDeficit(5), 0.001);
    }

    @Test
    @DisplayName("Composite formula: Priority = 0.35D + 0.30U + 0.20W + 0.15P")
    void testCompositePriorityCalculation() {
        LocalDate today = LocalDate.now();
        LocalDate examToday = today; // U = 1.0

        Topic highUrgencyTopic = new Topic();
        highUrgencyTopic.setDifficulty(5);       // D = 1.0
        highUrgencyTopic.setConfidenceLevel(1);  // P = 1.0
        highUrgencyTopic.setEstimatedMinutes(100);
        highUrgencyTopic.setCompletedMinutes(0); // W = 1.0

        // Expected: 0.35(1.0) + 0.30(1.0) + 0.20(1.0) + 0.15(1.0) = 1.00
        double priority = PriorityCalculator.calculate(highUrgencyTopic, examToday, today);
        assertEquals(1.00, priority, 0.001);

        // Low urgency, mastered, easy topic:
        Topic easyTopic = new Topic();
        easyTopic.setDifficulty(1);       // D = 0.2
        easyTopic.setConfidenceLevel(5);  // P = 0.0
        easyTopic.setEstimatedMinutes(100);
        easyTopic.setCompletedMinutes(100); // W = 0.0 (isCompleted = true)

        // Completed topics should always yield 0.0 priority
        assertEquals(0.0, PriorityCalculator.calculate(easyTopic, today.plusDays(30), today), 0.001);
    }

    @Test
    @DisplayName("Detailed priority explanation generates human-readable breakdown")
    void testPriorityExplanation() {
        Topic t = new Topic();
        t.setDifficulty(4);
        t.setConfidenceLevel(2);
        t.setEstimatedMinutes(120);
        t.setCompletedMinutes(0);

        LocalDate today = LocalDate.now();
        PriorityCalculator.Explanation exp = PriorityCalculator.explain(t, today.plusDays(10), today);

        assertNotNull(exp);
        assertTrue(exp.total > 0.0 && exp.total <= 1.0);
        assertNotNull(exp.getFormulaString());
        assertTrue(exp.getFormulaString().contains("0.35*"));
    }
}

