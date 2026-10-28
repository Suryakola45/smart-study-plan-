package com.smartstudyplanner.strategy;

import com.smartstudyplanner.model.Topic;

import java.time.LocalDate;
import java.util.Comparator;

/**
 * Concrete strategy implementing Deadline-First scheduling.
 * Prioritizes subjects and topics strictly by the closest examination date,
 * followed by difficulty and workload.
 */
public class DeadlineBasedPlanningStrategy extends AbstractPlanningStrategy {

    public static final String NAME = "DEADLINE_BASED";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Deadline-First Strategy: Prioritizes subjects with the closest upcoming examination dates first.";
    }

    @Override
    public Comparator<Topic> getTopicComparator(LocalDate referenceDate) {
        return (t1, t2) -> {
            // 1. Primary order: Earliest examination date first
            if (t1.getExamDate() != null && t2.getExamDate() != null) {
                int examCmp = t1.getExamDate().compareTo(t2.getExamDate());
                if (examCmp != 0) {
                    return examCmp;
                }
            } else if (t1.getExamDate() != null) {
                return -1; // t1 has exam date, prioritizes over null
            } else if (t2.getExamDate() != null) {
                return 1;
            }

            // 2. Secondary order: Higher difficulty first
            int diffCmp = Integer.compare(t2.getDifficulty(), t1.getDifficulty());
            if (diffCmp != 0) {
                return diffCmp;
            }

            // 3. Tertiary order: Lower confidence first
            int confCmp = Integer.compare(t1.getConfidenceLevel(), t2.getConfidenceLevel());
            if (confCmp != 0) {
                return confCmp;
            }

            // 4. Quaternary: Larger remaining work first
            return Integer.compare(t2.getRemainingMinutes(), t1.getRemainingMinutes());
        };
    }
}

