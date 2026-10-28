package com.smartstudyplanner.strategy;

import com.smartstudyplanner.exception.InsufficientStudyTimeException;
import com.smartstudyplanner.exception.PlanGenerationException;
import com.smartstudyplanner.model.Availability;
import com.smartstudyplanner.model.SessionStatus;
import com.smartstudyplanner.model.StudyPlan;
import com.smartstudyplanner.model.StudySession;
import com.smartstudyplanner.model.Topic;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Abstract base planning strategy encapsulating the core scheduling algorithm.
 * Demonstrates Abstraction and Template Method design pattern.
 *
 * Employs:
 * - PriorityQueue for greedy selection of highest-priority topics
 * - ArrayList for storing generated scheduled sessions
 * - HashMap for grouping and balancing sessions by date
 * - Comparator for dynamic strategy-specific ordering
 * - Java Date/Time classes (LocalDate, LocalTime)
 */
public abstract class AbstractPlanningStrategy implements PlanningStrategy {

    @Override
    public StudyPlan generateSchedule(Long userId,
                                      List<Topic> pendingTopics,
                                      List<Availability> availabilities,
                                      LocalDate startDate,
                                      LocalDate endDate)
            throws InsufficientStudyTimeException, PlanGenerationException {

        if (pendingTopics == null || pendingTopics.isEmpty()) {
            throw new PlanGenerationException("No pending topics found to generate a study plan.");
        }

        if (availabilities == null || availabilities.isEmpty()) {
            throw new PlanGenerationException("No study availability slots configured. Please add your available study hours first.");
        }

        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new PlanGenerationException("Invalid scheduling date range specified.");
        }

        // 1. Filter and sort availability slots within the target range
        List<Availability.TimeSlot> allSlots = new ArrayList<>();
        for (Availability avail : availabilities) {
            LocalDate d = avail.getAvailableDate();
            if (d != null && !d.isBefore(startDate) && !d.isAfter(endDate)) {
                allSlots.addAll(avail.generateStudySlots());
            }
        }

        // Sort slots chronologically
        allSlots.sort(Comparator.comparing(Availability.TimeSlot::getDate)
                .thenComparing(Availability.TimeSlot::getStart));

        if (allSlots.isEmpty()) {
            throw new PlanGenerationException("No valid study slots found within the selected date range: "
                    + startDate + " to " + endDate);
        }

        // 2. Calculate total available minutes and pending minutes
        int totalAvailableMinutes = allSlots.stream()
                .mapToInt(Availability.TimeSlot::getDurationMinutes)
                .sum();

        int totalRequiredMinutes = pendingTopics.stream()
                .mapToInt(Topic::getRemainingMinutes)
                .sum();

        // 3. Initialize PriorityQueue with strategy-specific comparator
        Comparator<Topic> comparator = getTopicComparator(startDate);
        PriorityQueue<Topic> queue = new PriorityQueue<>(comparator);

        // Make working copies of topics so we track virtual scheduling progress without mutating persistent state yet
        for (Topic original : pendingTopics) {
            if (original.getRemainingMinutes() > 0) {
                Topic workingCopy = copyTopic(original);
                queue.offer(workingCopy);
            }
        }

        if (queue.isEmpty()) {
            throw new PlanGenerationException("All topics in the selected scope are already completed.");
        }

        // 4. Greedy Allocation of Topics into Time Slots
        List<StudySession> scheduledSessions = new ArrayList<>();
        Map<LocalDate, List<StudySession>> dailySessionsMap = new HashMap<>();

        for (Availability.TimeSlot slot : allSlots) {
            if (queue.isEmpty()) {
                break; // All pending topics have been fully scheduled
            }

            Topic currentTopic = queue.poll();

            StudySession session = new StudySession();
            session.setUserId(userId);
            session.setTopicId(currentTopic.getId());
            session.setSessionDate(slot.getDate());
            session.setStartTime(slot.getStart());
            session.setEndTime(slot.getEnd());
            session.setDurationMinutes(slot.getDurationMinutes());
            session.setStatus(SessionStatus.SCHEDULED.name());
            session.setNotes("Scheduled by AI " + getName());

            // View fields
            session.setTopicTitle(currentTopic.getTitle());
            session.setSubjectName(currentTopic.getSubjectName());
            session.setSubjectCode(currentTopic.getSubjectCode());
            session.setSubjectColorHex(currentTopic.getSubjectColorHex());
            session.setExamDate(currentTopic.getExamDate());

            scheduledSessions.add(session);
            dailySessionsMap.computeIfAbsent(slot.getDate(), k -> new ArrayList<>()).add(session);

            // Deduct allocated time
            int remaining = currentTopic.getRemainingMinutes() - slot.getDurationMinutes();
            currentTopic.setCompletedMinutes(currentTopic.getCompletedMinutes() + slot.getDurationMinutes());

            if (remaining > 0) {
                // Topic still requires more study time: re-insert into priority queue
                queue.offer(currentTopic);
            }
        }

        // 5. Construct and return composite StudyPlan
        StudyPlan plan = new StudyPlan(startDate, endDate, getName());
        plan.setSessions(scheduledSessions);

        return plan;
    }

    /**
     * Helper to clone topic working state during schedule simulation.
     */
    protected Topic copyTopic(Topic t) {
        Topic copy = new Topic();
        copy.setId(t.getId());
        copy.setSubjectId(t.getSubjectId());
        copy.setTitle(t.getTitle());
        copy.setDescription(t.getDescription());
        copy.setDifficulty(t.getDifficulty());
        copy.setConfidenceLevel(t.getConfidenceLevel());
        copy.setEstimatedMinutes(t.getEstimatedMinutes());
        copy.setCompletedMinutes(t.getCompletedMinutes());
        copy.setStatus(t.getStatus());
        copy.setCalculatedPriority(t.getCalculatedPriority());
        copy.setSubjectName(t.getSubjectName());
        copy.setSubjectCode(t.getSubjectCode());
        copy.setExamDate(t.getExamDate());
        copy.setSubjectColorHex(t.getSubjectColorHex());
        return copy;
    }
}

