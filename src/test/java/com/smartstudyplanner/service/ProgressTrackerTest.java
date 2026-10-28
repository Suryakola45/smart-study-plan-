package com.smartstudyplanner.service;

import com.smartstudyplanner.dao.StudySessionDAO;
import com.smartstudyplanner.dao.SubjectDAO;
import com.smartstudyplanner.dao.TopicDAO;
import com.smartstudyplanner.model.SessionStatus;
import com.smartstudyplanner.model.StudySession;
import com.smartstudyplanner.model.Subject;
import com.smartstudyplanner.model.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Progress Tracking & Analytics Calculation Tests")
class ProgressTrackerTest {

    private ProgressService progressService;
    private List<Subject> mockSubjects;
    private List<Topic> mockTopics;
    private List<StudySession> mockSessions;

    @BeforeEach
    void setUp() {
        mockSubjects = new ArrayList<>();
        mockTopics = new ArrayList<>();
        mockSessions = new ArrayList<>();

        Subject s1 = new Subject(1L, 1L, "Data Structures", "CS201", LocalDate.now().plusDays(10), "#4361ee", "DSA");
        Subject s2 = new Subject(2L, 1L, "Databases", "CS202", LocalDate.now().plusDays(20), "#3a0ca3", "DBMS");
        mockSubjects.add(s1);
        mockSubjects.add(s2);

        // Topic 1: Completed (60/60)
        Topic t1 = new Topic(1L, 1L, "Sorting", "", 2, 4, 60);
        t1.setCompletedMinutes(60);
        t1.setStatus("COMPLETED");

        // Topic 2: In Progress (30/90)
        Topic t2 = new Topic(2L, 1L, "Trees", "", 4, 2, 90);
        t2.setCompletedMinutes(30);
        t2.setStatus("IN_PROGRESS");

        // Topic 3: Pending (0/120)
        Topic t3 = new Topic(3L, 2L, "Normalization", "", 4, 2, 120);
        t3.setCompletedMinutes(0);
        t3.setStatus("PENDING");

        // Topic 4: Completed (60/60)
        Topic t4 = new Topic(4L, 2L, "SQL Joins", "", 2, 5, 60);
        t4.setCompletedMinutes(60);
        t4.setStatus("COMPLETED");

        mockTopics.add(t1);
        mockTopics.add(t2);
        mockTopics.add(t3);
        mockTopics.add(t4);

        // Sessions
        StudySession sess1 = new StudySession(1L, 1L, 1L, LocalDate.now(), LocalTime.of(18, 0), LocalTime.of(19, 0), 60);
        sess1.markCompleted(60, "Done");
        StudySession sess2 = new StudySession(2L, 1L, 2L, LocalDate.now(), LocalTime.of(19, 15), LocalTime.of(20, 0), 45);
        sess2.markSkipped("Tired");

        mockSessions.add(sess1);
        mockSessions.add(sess2);

        progressService = new ProgressService(
                new SubjectDAO() {
                    @Override
                    public List<Subject> findByUserId(Long userId) {
                        return mockSubjects;
                    }
                },
                new TopicDAO() {
                    @Override
                    public List<Topic> findByUserId(Long userId) {
                        return mockTopics;
                    }

                    @Override
                    public List<Topic> findBySubjectId(Long subjectId) {
                        return mockTopics.stream()
                                .filter(t -> t.getSubjectId().equals(subjectId))
                                .toList();
                    }
                },
                new StudySessionDAO() {
                    @Override
                    public List<StudySession> findByUserId(Long userId) {
                        return mockSessions;
                    }

                    @Override
                    public java.util.Map<LocalDate, Integer> getWeeklyStudyMinutes(Long userId, LocalDate start, LocalDate end) {
                        return Collections.singletonMap(LocalDate.now(), 60);
                    }
                }
        );
    }

    @Test
    @DisplayName("Progress Summary correctly calculates overall completion and remaining hours")
    void testProgressCalculations() {
        ProgressService.ProgressSummary summary = progressService.getProgressSummary(1L);

        assertEquals(2, summary.getTotalSubjects());
        assertEquals(4, summary.getTotalTopics());
        assertEquals(2, summary.getCompletedTopics());
        assertEquals(1, summary.getInProgressTopics());
        assertEquals(1, summary.getPendingTopics());

        // 2 of 4 topics completed -> 50.0%
        assertEquals(50.0, summary.getOverallCompletionPercentage(), 0.01);

        // Total estimated: 60 + 90 + 120 + 60 = 330 mins
        assertEquals(330, summary.getTotalEstimatedMinutes());

        // Total completed: 60 + 30 + 0 + 60 = 150 mins
        assertEquals(150, summary.getTotalCompletedMinutes());

        // Remaining: 330 - 150 = 180 mins
        assertEquals(180, summary.getRemainingMinutes());

        // Sessions count
        assertEquals(2, summary.getTotalSessions());
        assertEquals(1, summary.getCompletedSessions());
        assertEquals(1, summary.getSkippedSessions());
        assertEquals(60, summary.getActualMinutesStudied());
    }

    @Test
    @DisplayName("Subject-wise progress reflects individual course completion percentages")
    void testSubjectWiseProgress() {
        ProgressService.ProgressSummary summary = progressService.getProgressSummary(1L);
        List<ProgressService.SubjectProgressItem> subjectItems = summary.getSubjectProgressList();

        assertEquals(2, subjectItems.size());

        // Subject 1 (Data Structures): 1 completed out of 2 = 50.0%
        ProgressService.SubjectProgressItem dsa = subjectItems.get(0);
        assertEquals("Data Structures", dsa.subjectName);
        assertEquals(50.0, dsa.completionPercentage, 0.01);

        // Subject 2 (Databases): 1 completed out of 2 = 50.0%
        ProgressService.SubjectProgressItem dbms = subjectItems.get(1);
        assertEquals("Databases", dbms.subjectName);
        assertEquals(50.0, dbms.completionPercentage, 0.01);
    }
}

