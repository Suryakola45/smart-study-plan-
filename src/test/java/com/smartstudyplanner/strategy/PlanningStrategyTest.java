package com.smartstudyplanner.strategy;

import com.smartstudyplanner.exception.InsufficientStudyTimeException;
import com.smartstudyplanner.exception.PlanGenerationException;
import com.smartstudyplanner.model.Availability;
import com.smartstudyplanner.model.StudyPlan;
import com.smartstudyplanner.model.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Planning Strategy and Schedule Generation Tests")
class PlanningStrategyTest {

    private List<Topic> testTopics;
    private List<Availability> testAvailabilities;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        testTopics = new ArrayList<>();

        // Topic 1: Difficult, low confidence, exam in 5 days
        Topic t1 = new Topic(1L, 10L, "Binary Trees", "Trees", 5, 1, 90);
        t1.setExamDate(today.plusDays(5));
        t1.setSubjectName("DSA");

        // Topic 2: Medium, average confidence, exam in 25 days
        Topic t2 = new Topic(2L, 11L, "SQL Joins", "SQL", 3, 3, 90);
        t2.setExamDate(today.plusDays(25));
        t2.setSubjectName("DBMS");

        // Topic 3: Easy, high confidence, exam in 2 days (urgent deadline!)
        Topic t3 = new Topic(3L, 12L, "IP Addressing", "Networks", 2, 4, 90);
        t3.setExamDate(today.plusDays(2));
        t3.setSubjectName("CN");

        testTopics.add(t1);
        testTopics.add(t2);
        testTopics.add(t3);

        // Availability: 2 days, 18:00 to 20:00 (two 45-min slots each day = 4 total slots)
        testAvailabilities = new ArrayList<>();

        Availability a1 = new Availability(1L, 1L, today, LocalTime.of(18, 0), LocalTime.of(20, 0), 45, 15);
        Availability a2 = new Availability(2L, 1L, today.plusDays(1), LocalTime.of(18, 0), LocalTime.of(20, 0), 45, 15);

        testAvailabilities.add(a1);
        testAvailabilities.add(a2);
    }

    @Test
    @DisplayName("Availability slot generation should slice time windows with breaks")
    void testSlotGeneration() {
        Availability a = new Availability(1L, 1L, today, LocalTime.of(18, 0), LocalTime.of(20, 0), 45, 15);
        List<Availability.TimeSlot> slots = a.generateStudySlots();

        assertEquals(2, slots.size());
        assertEquals(LocalTime.of(18, 0), slots.get(0).getStart());
        assertEquals(LocalTime.of(18, 45), slots.get(0).getEnd());
        assertEquals(LocalTime.of(19, 0), slots.get(1).getStart());
        assertEquals(LocalTime.of(19, 45), slots.get(1).getEnd());
    }

    @Test
    @DisplayName("PriorityBasedStrategy should order by AI score (0.35D + 0.30U + 0.20W + 0.15P)")
    void testPriorityBasedStrategyOrdering() {
        PlanningStrategy strategy = new PriorityBasedPlanningStrategy();
        Comparator<Topic> cmp = strategy.getTopicComparator(today);

        testTopics.sort(cmp);

        // Topic 1 (Diff 5, Conf 1, Exam 5d) has the highest combined score
        assertEquals("Binary Trees", testTopics.get(0).getTitle());
    }

    @Test
    @DisplayName("DeadlineBasedStrategy should order strictly by earliest examination date first")
    void testDeadlineBasedStrategyOrdering() {
        PlanningStrategy strategy = new DeadlineBasedPlanningStrategy();
        Comparator<Topic> cmp = strategy.getTopicComparator(today);

        testTopics.sort(cmp);

        // Topic 3 (Exam in 2 days) must be scheduled first
        assertEquals("IP Addressing", testTopics.get(0).getTitle());
        // Topic 1 (Exam in 5 days) second
        assertEquals("Binary Trees", testTopics.get(1).getTitle());
        // Topic 2 (Exam in 25 days) last
        assertEquals("SQL Joins", testTopics.get(2).getTitle());
    }

    @Test
    @DisplayName("StudyPlan generation produces scheduled sessions across availability slots")
    void testGenerateSchedule() throws InsufficientStudyTimeException, PlanGenerationException {
        PlanningStrategy strategy = new PriorityBasedPlanningStrategy();
        StudyPlan plan = strategy.generateSchedule(1L, testTopics, testAvailabilities, today, today.plusDays(1));

        assertNotNull(plan);
        assertEquals(4, plan.getTotalSessions()); // 2 slots on day 1 + 2 slots on day 2
        assertEquals(180, plan.getTotalPlannedMinutes());
        assertFalse(plan.getSessions().isEmpty());
    }

    @Test
    @DisplayName("PlanningStrategyFactory returns correct instances")
    void testStrategyFactory() {
        PlanningStrategy priority = PlanningStrategyFactory.getStrategy("PRIORITY_BASED");
        assertInstanceOf(PriorityBasedPlanningStrategy.class, priority);

        PlanningStrategy deadline = PlanningStrategyFactory.getStrategy("DEADLINE_BASED");
        assertInstanceOf(DeadlineBasedPlanningStrategy.class, deadline);

        // Default fallback
        PlanningStrategy fallback = PlanningStrategyFactory.getStrategy("UNKNOWN");
        assertInstanceOf(PriorityBasedPlanningStrategy.class, fallback);
    }
}

