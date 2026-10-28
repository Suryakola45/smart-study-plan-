package com.smartstudyplanner.service;

import com.smartstudyplanner.dao.AvailabilityDAO;
import com.smartstudyplanner.dao.StudySessionDAO;
import com.smartstudyplanner.dao.SubjectDAO;
import com.smartstudyplanner.dao.TopicDAO;
import com.smartstudyplanner.exception.InsufficientStudyTimeException;
import com.smartstudyplanner.exception.PlanGenerationException;
import com.smartstudyplanner.model.Availability;
import com.smartstudyplanner.model.SessionStatus;
import com.smartstudyplanner.model.StudyPlan;
import com.smartstudyplanner.model.StudySession;
import com.smartstudyplanner.model.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Automated Rescheduling Engine Tests (Requirement 8)")
class ReschedulingTest {

    private StudyPlanService studyPlanService;
    private List<StudySession> storedSessions;
    private List<Topic> pendingTopics;
    private List<Availability> futureAvailabilities;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        storedSessions = new ArrayList<>();
        pendingTopics = new ArrayList<>();
        futureAvailabilities = new ArrayList<>();

        // 1. Existing Completed Session (HISTORICAL - MUST BE PRESERVED)
        StudySession completedSession = new StudySession(1L, 1L, 101L, today.minusDays(2),
                LocalTime.of(18, 0), LocalTime.of(19, 0), 60);
        completedSession.markCompleted(60, "Successfully completed AVL rotations");
        storedSessions.add(completedSession);

        // 2. Existing Missed Session from yesterday (SCHEDULED in the past)
        StudySession missedSession = new StudySession(2L, 1L, 102L, today.minusDays(1),
                LocalTime.of(18, 0), LocalTime.of(19, 0), 60);
        missedSession.setStatus(SessionStatus.SCHEDULED.name());
        storedSessions.add(missedSession);

        // 3. Pending topic requiring study
        Topic t = new Topic(102L, 1L, "Graph BFS", "BFS traversal", 4, 2, 90);
        t.setExamDate(today.plusDays(5));
        pendingTopics.add(t);

        // 4. Future availability
        Availability futureAvail = new Availability(1L, 1L, today.plusDays(1),
                LocalTime.of(18, 0), LocalTime.of(20, 0), 45, 15);
        futureAvailabilities.add(futureAvail);

        // Wire Mock DAOs
        StudySessionDAO mockSessionDAO = new StudySessionDAO() {
            @Override
            public List<StudySession> findByUserId(Long userId) {
                return new ArrayList<>(storedSessions);
            }

            @Override
            public boolean updateStatusAndActualTime(Long sessionId, String status, int actualMinutes, String notes) {
                for (StudySession s : storedSessions) {
                    if (s.getId().equals(sessionId)) {
                        s.setStatus(status);
                        s.setActualMinutesStudied(actualMinutes);
                        s.setNotes(notes);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public int deleteFutureScheduledSessions(Long userId, LocalDate fromDate) {
                storedSessions.removeIf(s -> s.getSessionDate().compareTo(fromDate) >= 0 &&
                        SessionStatus.SCHEDULED.name().equalsIgnoreCase(s.getStatus()));
                return 1;
            }

            @Override
            public boolean saveAll(List<StudySession> sessions) {
                long nextId = 100L;
                for (StudySession s : sessions) {
                    s.setId(nextId++);
                    storedSessions.add(s);
                }
                return true;
            }
        };

        TopicDAO mockTopicDAO = new TopicDAO() {
            @Override
            public List<Topic> findPendingByUserId(Long userId) {
                return pendingTopics;
            }

            @Override
            public List<Topic> findByUserId(Long userId) {
                return pendingTopics;
            }

            @Override
            public boolean updateCalculatedPriority(Long topicId, double priority) {
                return true;
            }
        };

        AvailabilityDAO mockAvailDAO = new AvailabilityDAO() {
            @Override
            public List<Availability> findByUserIdAndDateRange(Long userId, LocalDate start, LocalDate end) {
                return futureAvailabilities;
            }
        };

        TopicService topicService = new TopicService(mockTopicDAO, new SubjectDAO());
        studyPlanService = new StudyPlanService(mockTopicDAO, mockAvailDAO, mockSessionDAO, topicService);
    }

    @Test
    @DisplayName("Rescheduling marks past missed session as SKIPPED and retains completed session history")
    void testReschedulePreservesHistoryAndReallocates()
            throws InsufficientStudyTimeException, PlanGenerationException {

        StudyPlan newPlan = studyPlanService.reschedule(1L, today, "PRIORITY_BASED");

        assertNotNull(newPlan);
        assertFalse(newPlan.getSessions().isEmpty());

        // 1. Verify Completed Session is intact and was NOT deleted
        StudySession completed = storedSessions.stream()
                .filter(s -> s.getId().equals(1L))
                .findFirst()
                .orElse(null);
        assertNotNull(completed, "Completed study session must remain in history!");
        assertEquals(SessionStatus.COMPLETED.name(), completed.getStatus());
        assertEquals(60, completed.getActualMinutesStudied());

        // 2. Verify Missed Session was transitioned to SKIPPED
        StudySession missed = storedSessions.stream()
                .filter(s -> s.getId().equals(2L))
                .findFirst()
                .orElse(null);
        assertNotNull(missed);
        assertEquals(SessionStatus.SKIPPED.name(), missed.getStatus());
        assertTrue(missed.getNotes().contains("missed"));

        // 3. Verify newly rescheduled future sessions were added
        long futureScheduledCount = storedSessions.stream()
                .filter(s -> s.getSessionDate().isAfter(today) &&
                        SessionStatus.SCHEDULED.name().equalsIgnoreCase(s.getStatus()))
                .count();
        assertTrue(futureScheduledCount > 0, "New future sessions must be scheduled!");
    }
}

