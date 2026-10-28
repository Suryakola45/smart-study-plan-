package com.smartstudyplanner.service;

import com.smartstudyplanner.dao.AvailabilityDAO;
import com.smartstudyplanner.exception.InvalidStudyDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Availability Validation Tests")
class AvailabilityValidationTest {

    private AvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
        // Stub DAO for validation unit testing
        availabilityService = new AvailabilityService(new AvailabilityDAO() {
            @Override
            public com.smartstudyplanner.model.Availability save(com.smartstudyplanner.model.Availability a) {
                a.setId(99L);
                return a;
            }
        });
    }

    @Test
    @DisplayName("Availability with end time before start time should throw InvalidStudyDataException")
    void testEndTimeBeforeStartTime() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalTime start = LocalTime.of(20, 0);
        LocalTime end = LocalTime.of(18, 0);

        InvalidStudyDataException ex = assertThrows(InvalidStudyDataException.class, () ->
                availabilityService.addAvailability(1L, tomorrow, start, end, 45, 15));

        assertEquals("time", ex.getFieldName());
    }

    @Test
    @DisplayName("Availability with window less than 30 minutes should throw InvalidStudyDataException")
    void testSpanTooShort() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalTime start = LocalTime.of(18, 0);
        LocalTime end = LocalTime.of(18, 20); // only 20 mins

        InvalidStudyDataException ex = assertThrows(InvalidStudyDataException.class, () ->
                availabilityService.addAvailability(1L, tomorrow, start, end, 45, 15));

        assertEquals("span", ex.getFieldName());
    }

    @Test
    @DisplayName("Availability date in the past should throw InvalidStudyDataException")
    void testPastDateRejected() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalTime start = LocalTime.of(18, 0);
        LocalTime end = LocalTime.of(20, 0);

        InvalidStudyDataException ex = assertThrows(InvalidStudyDataException.class, () ->
                availabilityService.addAvailability(1L, yesterday, start, end, 45, 15));

        assertEquals("availableDate", ex.getFieldName());
    }

    @Test
    @DisplayName("Slot duration under 15 minutes should throw InvalidStudyDataException")
    void testSlotDurationTooSmall() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalTime start = LocalTime.of(18, 0);
        LocalTime end = LocalTime.of(20, 0);

        InvalidStudyDataException ex = assertThrows(InvalidStudyDataException.class, () ->
                availabilityService.addAvailability(1L, tomorrow, start, end, 10, 15));

        assertEquals("slotMinutes", ex.getFieldName());
    }

    @Test
    @DisplayName("Valid availability configuration succeeds")
    void testValidAvailability() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalTime start = LocalTime.of(18, 0);
        LocalTime end = LocalTime.of(21, 0);

        assertDoesNotThrow(() ->
                availabilityService.addAvailability(1L, tomorrow, start, end, 45, 15));
    }
}

