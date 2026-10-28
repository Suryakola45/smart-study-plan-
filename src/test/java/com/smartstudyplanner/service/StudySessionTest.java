package com.smartstudyplanner.service;

import com.smartstudyplanner.model.SessionStatus;
import com.smartstudyplanner.model.StudySession;
import com.smartstudyplanner.model.Topic;
import com.smartstudyplanner.model.TopicStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Study Session Lifecycle and Execution Tests")
class StudySessionTest {

    @Test
    @DisplayName("Completing a session records actual minutes and updates state")
    void testCompleteSession() {
        StudySession session = new StudySession(1L, 1L, 10L, LocalDate.now(),
                LocalTime.of(18, 0), LocalTime.of(18, 45), 45);

        session.markCompleted(50, "Reviewed all search trees thoroughly.");

        assertEquals(SessionStatus.COMPLETED.name(), session.getStatus());
        assertEquals(50, session.getActualMinutesStudied());
        assertEquals("Reviewed all search trees thoroughly.", session.getNotes());
        assertTrue(session.isFinished());
    }

    @Test
    @DisplayName("Skipping a session marks status SKIPPED with zero actual minutes")
    void testSkipSession() {
        StudySession session = new StudySession(1L, 1L, 10L, LocalDate.now(),
                LocalTime.of(18, 0), LocalTime.of(18, 45), 45);

        session.markSkipped("Had lab exam revision.");

        assertEquals(SessionStatus.SKIPPED.name(), session.getStatus());
        assertEquals(0, session.getActualMinutesStudied());
        assertTrue(session.getNotes().contains("Had lab exam revision"));
    }

    @Test
    @DisplayName("Partially completing a session records actual partial minutes")
    void testPartiallyCompleteSession() {
        StudySession session = new StudySession(1L, 1L, 10L, LocalDate.now(),
                LocalTime.of(18, 0), LocalTime.of(18, 45), 45);

        session.markPartiallyCompleted(30, "Power outage after 30 mins.");

        assertEquals(SessionStatus.PARTIALLY_COMPLETED.name(), session.getStatus());
        assertEquals(30, session.getActualMinutesStudied());
        assertTrue(session.isFinished());
    }

    @Test
    @DisplayName("Overdue scheduled session is correctly identified as missed")
    void testMissedSessionDetection() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Yesterday's scheduled session is missed
        StudySession yesterdaySession = new StudySession(1L, 1L, 10L, today.minusDays(1),
                LocalTime.of(10, 0), LocalTime.of(11, 0), 60);
        assertTrue(yesterdaySession.isMissed(today, now));

        // Tomorrow's session is not missed
        StudySession tomorrowSession = new StudySession(2L, 1L, 10L, today.plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(11, 0), 60);
        assertFalse(tomorrowSession.isMissed(today, now));

        // Already completed session is never marked missed
        yesterdaySession.setStatus(SessionStatus.COMPLETED.name());
        assertFalse(yesterdaySession.isMissed(today, now));
    }

    @Test
    @DisplayName("Topic progress updates dynamically transition status")
    void testTopicStatusProgression() {
        Topic topic = new Topic(1L, 1L, "Graphs", "Desc", 3, 3, 100);
        assertEquals(TopicStatus.PENDING.name(), topic.getStatus());

        // Add 40 minutes -> IN_PROGRESS
        topic.addCompletedMinutes(40);
        assertEquals(TopicStatus.IN_PROGRESS.name(), topic.getStatus());
        assertEquals(60, topic.getRemainingMinutes());
        assertEquals(40.0, topic.getCompletionPercentage(), 0.01);

        // Add remaining 60 minutes -> COMPLETED
        topic.addCompletedMinutes(60);
        assertEquals(TopicStatus.COMPLETED.name(), topic.getStatus());
        assertEquals(0, topic.getRemainingMinutes());
        assertTrue(topic.isCompleted());
        assertEquals(100.0, topic.getCompletionPercentage(), 0.01);
    }
}

