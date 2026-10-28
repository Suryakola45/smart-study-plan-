package com.smartstudyplanner.strategy;

import com.smartstudyplanner.exception.InsufficientStudyTimeException;
import com.smartstudyplanner.exception.PlanGenerationException;
import com.smartstudyplanner.model.Availability;
import com.smartstudyplanner.model.StudyPlan;
import com.smartstudyplanner.model.Topic;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Strategy interface demonstrating Polymorphism and Abstraction in OOP.
 * Defines the contract for different study schedule generation algorithms.
 */
public interface PlanningStrategy {

    /**
     * Unique identifier for the strategy.
     */
    String getName();

    /**
     * Human-readable description explaining how the strategy prioritizes topics.
     */
    String getDescription();

    /**
     * Returns the topic comparator specific to this planning strategy.
     */
    Comparator<Topic> getTopicComparator(LocalDate referenceDate);

    /**
     * Generates a scheduled study plan for the given topics across declared availabilities.
     *
     * @param userId Owner student id
     * @param pendingTopics Uncompleted topics to schedule
     * @param availabilities Available time windows
     * @param startDate Scheduling window start
     * @param endDate Scheduling window end
     * @return Generated StudyPlan containing scheduled sessions
     * @throws InsufficientStudyTimeException if available slots cannot accommodate critical work
     * @throws PlanGenerationException if configuration or constraints fail
     */
    StudyPlan generateSchedule(Long userId,
                               List<Topic> pendingTopics,
                               List<Availability> availabilities,
                               LocalDate startDate,
                               LocalDate endDate) throws InsufficientStudyTimeException, PlanGenerationException;
}

