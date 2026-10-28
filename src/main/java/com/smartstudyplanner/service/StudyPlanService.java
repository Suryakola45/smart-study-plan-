package com.smartstudyplanner.service;

import com.smartstudyplanner.dao.AvailabilityDAO;
import com.smartstudyplanner.dao.StudySessionDAO;
import com.smartstudyplanner.dao.TopicDAO;
import com.smartstudyplanner.exception.DatabaseException;
import com.smartstudyplanner.exception.InsufficientStudyTimeException;
import com.smartstudyplanner.exception.PlanGenerationException;
import com.smartstudyplanner.model.Availability;
import com.smartstudyplanner.model.SessionStatus;
import com.smartstudyplanner.model.StudyPlan;
import com.smartstudyplanner.model.StudySession;
import com.smartstudyplanner.model.Topic;
import com.smartstudyplanner.strategy.PlanningStrategy;
import com.smartstudyplanner.strategy.PlanningStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Core StudyPlanService generating daily and weekly schedules and orchestrating rescheduling.
 * Fulfills Requirements 6, 7, and 8.
 */
public class StudyPlanService {

    private static final Logger logger = LoggerFactory.getLogger(StudyPlanService.class);

    private final TopicDAO topicDAO;
    private final AvailabilityDAO availabilityDAO;
    private final StudySessionDAO sessionDAO;
    private final TopicService topicService;

    public StudyPlanService() {
        this.topicDAO = new TopicDAO();
        this.availabilityDAO = new AvailabilityDAO();
        this.sessionDAO = new StudySessionDAO();
        this.topicService = new TopicService();
    }

    public StudyPlanService(TopicDAO topicDAO, AvailabilityDAO availabilityDAO,
                            StudySessionDAO sessionDAO, TopicService topicService) {
        this.topicDAO = topicDAO;
        this.availabilityDAO = availabilityDAO;
        this.sessionDAO = sessionDAO;
        this.topicService = topicService;
    }

    /**
     * Generates a study plan for a single day.
     */
    public StudyPlan generateDailyPlan(Long userId, LocalDate targetDate, String strategyName)
            throws InsufficientStudyTimeException, PlanGenerationException {
        return generateAndPersistPlan(userId, targetDate, targetDate, strategyName);
    }

    /**
     * Generates a 7-day weekly study plan starting from startDate.
     */
    public StudyPlan generateWeeklyPlan(Long userId, LocalDate startDate, String strategyName)
            throws InsufficientStudyTimeException, PlanGenerationException {
        LocalDate endDate = startDate.plusDays(6);
        return generateAndPersistPlan(userId, startDate, endDate, strategyName);
    }

    /**
     * Core generation and persistence engine.
     */
    public StudyPlan generateAndPersistPlan(Long userId, LocalDate startDate, LocalDate endDate, String strategyName)
            throws InsufficientStudyTimeException, PlanGenerationException {

        // 1. Recalculate AI priorities before scheduling
        topicService.refreshAllPrioritiesForUser(userId);

        // 2. Fetch pending topics
        List<Topic> pendingTopics = topicDAO.findPendingByUserId(userId);
        if (pendingTopics.isEmpty()) {
            throw new PlanGenerationException("No pending topics found to schedule. All topics are completed!");
        }

        // 3. Fetch availability for date range
        List<Availability> availabilities = availabilityDAO.findByUserIdAndDateRange(userId, startDate, endDate);
        if (availabilities.isEmpty()) {
            throw new PlanGenerationException(String.format("No availability windows found between %s and %s. " +
                    "Please configure your study availability first.", startDate, endDate));
        }

        // 4. Resolve strategy (Priority-based or Deadline-based)
        PlanningStrategy strategy = PlanningStrategyFactory.getStrategy(strategyName);

        // 5. Generate schedule through Strategy Polymorphism
        StudyPlan plan = strategy.generateSchedule(userId, pendingTopics, availabilities, startDate, endDate);

        // 6. Delete previous SCHEDULED sessions in this date range to avoid duplicates
        // Note: COMPLETED and SKIPPED sessions are strictly preserved!
        sessionDAO.deleteFutureScheduledSessions(userId, startDate);

        // 7. Batch persist the generated sessions
        sessionDAO.saveAll(plan.getSessions());

        return plan;
    }

    /**
     * Automatically reorganizes pending and missed tasks based on future availability,
     * priorities, and examination deadlines without deleting completed study history!
     * (Requirement 8)
     */
    public StudyPlan reschedule(Long userId, LocalDate fromDate, String strategyName)
            throws InsufficientStudyTimeException, PlanGenerationException {

        LocalDate baseDate = fromDate != null ? fromDate : LocalDate.now();
        LocalTime now = LocalTime.now();

        logger.info("Initiating reschedule for user {} starting from {}", userId, baseDate);

        // 1. Identify and mark past missed sessions as SKIPPED so history reflects actual attendance
        List<StudySession> existingSessions = sessionDAO.findByUserId(userId);
        for (StudySession s : existingSessions) {
            if (s.isMissed(baseDate, now)) {
                sessionDAO.updateStatusAndActualTime(s.getId(), SessionStatus.SKIPPED.name(), 0,
                        "Automatically marked missed by rescheduling engine on " + LocalDate.now());
            }
        }

        // 2. Clear only future SCHEDULED sessions from baseDate onward
        // Completed, partially completed, and historical sessions remain completely untouched!
        sessionDAO.deleteFutureScheduledSessions(userId, baseDate);

        // 3. Refresh AI priorities taking current date urgency into account
        topicService.refreshAllPrioritiesForUser(userId);

        // 4. Retrieve uncompleted topics
        List<Topic> pendingTopics = topicDAO.findPendingByUserId(userId);
        if (pendingTopics.isEmpty()) {
            throw new PlanGenerationException("All topics are completed. No rescheduling required!");
        }

        // 5. Determine planning window (from baseDate through maximum exam date or +14 days)
        LocalDate maxExamDate = pendingTopics.stream()
                .map(Topic::getExamDate)
                .filter(d -> d != null && !d.isBefore(baseDate))
                .max(LocalDate::compareTo)
                .orElse(baseDate.plusDays(14));

        LocalDate windowEnd = maxExamDate.isAfter(baseDate.plusDays(14)) ? baseDate.plusDays(14) : maxExamDate;
        if (windowEnd.isBefore(baseDate)) {
            windowEnd = baseDate.plusDays(7);
        }

        // 6. Fetch future availability
        List<Availability> availabilities = availabilityDAO.findByUserIdAndDateRange(userId, baseDate, windowEnd);
        if (availabilities.isEmpty()) {
            throw new PlanGenerationException("No future availability slots found from " + baseDate +
                    ". Please define your available study slots to reschedule.");
        }

        // 7. Execute Strategy
        PlanningStrategy strategy = PlanningStrategyFactory.getStrategy(strategyName);
        StudyPlan plan = strategy.generateSchedule(userId, pendingTopics, availabilities, baseDate, windowEnd);

        // 8. Batch persist rescheduled sessions
        sessionDAO.saveAll(plan.getSessions());

        return plan;
    }

    /**
     * Mark a study session as COMPLETED, recording actual minutes studied and updating topic progress.
     */
    public boolean completeSession(Long sessionId, int actualMinutes, String notes) {
        Optional<StudySession> opt = sessionDAO.findById(sessionId);
        if (opt.isEmpty()) return false;

        StudySession session = opt.get();
        int minutes = actualMinutes > 0 ? actualMinutes : session.getDurationMinutes();
        session.markCompleted(minutes, notes);

        sessionDAO.updateStatusAndActualTime(session.getId(), session.getStatus(),
                session.getActualMinutesStudied(), session.getNotes());

        // Update Topic progress
        topicDAO.updateProgress(session.getTopicId(), minutes);

        return true;
    }

    /**
     * Mark a study session as SKIPPED. Does not record completed minutes on the topic.
     */
    public boolean skipSession(Long sessionId, String reason) {
        Optional<StudySession> opt = sessionDAO.findById(sessionId);
        if (opt.isEmpty()) return false;

        StudySession session = opt.get();
        session.markSkipped(reason);

        return sessionDAO.updateStatusAndActualTime(session.getId(), session.getStatus(), 0, session.getNotes());
    }

    /**
     * Mark a study session as PARTIALLY_COMPLETED with recorded partial minutes.
     */
    public boolean partiallyCompleteSession(Long sessionId, int actualMinutes, String notes) {
        Optional<StudySession> opt = sessionDAO.findById(sessionId);
        if (opt.isEmpty()) return false;

        StudySession session = opt.get();
        int minutes = Math.max(1, actualMinutes);
        session.markPartiallyCompleted(minutes, notes);

        sessionDAO.updateStatusAndActualTime(session.getId(), session.getStatus(),
                session.getActualMinutesStudied(), session.getNotes());

        // Update Topic progress with partial minutes
        topicDAO.updateProgress(session.getTopicId(), minutes);

        return true;
    }

    public List<StudySession> getSessionsForDate(Long userId, LocalDate date) {
        return sessionDAO.findByUserIdAndDateRange(userId, date, date);
    }

    public List<StudySession> getSessionsForDateRange(Long userId, LocalDate start, LocalDate end) {
        return sessionDAO.findByUserIdAndDateRange(userId, start, end);
    }

    public List<StudySession> getAllSessionsByUser(Long userId) {
        return sessionDAO.findByUserId(userId);
    }
}

