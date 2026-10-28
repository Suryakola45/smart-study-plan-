package com.smartstudyplanner.strategy;

import com.smartstudyplanner.model.Topic;
import com.smartstudyplanner.service.PriorityCalculator;

import java.time.LocalDate;
import java.util.Comparator;

/**
 * Concrete strategy implementing Priority-based scheduling.
 * Prioritizes topics based on the multi-factor AI recommendation formula:
 * Priority = 0.35*Difficulty + 0.30*Urgency + 0.20*Workload + 0.15*ConfidenceDeficit
 */
public class PriorityBasedPlanningStrategy extends AbstractPlanningStrategy {

    public static final String NAME = "PRIORITY_BASED";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "AI Balanced Priority: Optimizes schedule using Difficulty (35%), Urgency (30%), Workload (20%), and Confidence Deficit (15%).";
    }

    @Override
    public Comparator<Topic> getTopicComparator(LocalDate referenceDate) {
        LocalDate baseDate = referenceDate != null ? referenceDate : LocalDate.now();

        return (t1, t2) -> {
            // Recalculate dynamic priority relative to base reference date
            double p1 = PriorityCalculator.calculate(t1, t1.getExamDate(), baseDate);
            double p2 = PriorityCalculator.calculate(t2, t2.getExamDate(), baseDate);

            // Descending order of priority score
            int cmp = Double.compare(p2, p1);
            if (cmp != 0) {
                return cmp;
            }

            // Tie-breaker 1: nearest exam date first
            if (t1.getExamDate() != null && t2.getExamDate() != null) {
                int examCmp = t1.getExamDate().compareTo(t2.getExamDate());
                if (examCmp != 0) return examCmp;
            }

            // Tie-breaker 2: higher difficulty first
            return Integer.compare(t2.getDifficulty(), t1.getDifficulty());
        };
    }
}

