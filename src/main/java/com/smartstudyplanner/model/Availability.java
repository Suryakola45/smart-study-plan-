package com.smartstudyplanner.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Availability entity representing a student's available study window for a specific date.
 */
public class Availability extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private LocalDate availableDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int slotDurationMinutes;   // e.g., 45 mins
    private int breakDurationMinutes;  // e.g., 15 mins

    public Availability() {
        super();
        this.slotDurationMinutes = 45;
        this.breakDurationMinutes = 15;
    }

    public Availability(Long id, Long userId, LocalDate availableDate,
                        LocalTime startTime, LocalTime endTime,
                        int slotDurationMinutes, int breakDurationMinutes) {
        super(id);
        this.userId = userId;
        this.availableDate = availableDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes > 0 ? slotDurationMinutes : 45;
        this.breakDurationMinutes = breakDurationMinutes >= 0 ? breakDurationMinutes : 15;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(LocalDate availableDate) {
        this.availableDate = availableDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public void setSlotDurationMinutes(int slotDurationMinutes) {
        this.slotDurationMinutes = Math.max(15, slotDurationMinutes);
    }

    public int getBreakDurationMinutes() {
        return breakDurationMinutes;
    }

    public void setBreakDurationMinutes(int breakDurationMinutes) {
        this.breakDurationMinutes = Math.max(0, breakDurationMinutes);
    }

    /**
     * Calculates total raw span in minutes between start and end time.
     */
    public int getTotalSpanMinutes() {
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            return 0;
        }
        return (int) Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Slices the availability window into discrete study slot intervals
     * separated by configured break durations.
     */
    public List<TimeSlot> generateStudySlots() {
        List<TimeSlot> slots = new ArrayList<>();
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            return slots;
        }

        LocalTime current = startTime;
        int step = slotDurationMinutes + breakDurationMinutes;

        while (true) {
            LocalTime slotEnd = current.plusMinutes(slotDurationMinutes);
            if (slotEnd.isAfter(endTime)) {
                // If remaining time is at least 30 minutes, create a final partial slot
                long remaining = Duration.between(current, endTime).toMinutes();
                if (remaining >= 30) {
                    slots.add(new TimeSlot(availableDate, current, endTime, (int) remaining));
                }
                break;
            }
            slots.add(new TimeSlot(availableDate, current, slotEnd, slotDurationMinutes));
            current = slotEnd.plusMinutes(breakDurationMinutes);
            if (!current.isBefore(endTime)) {
                break;
            }
        }

        return slots;
    }

    /**
     * Inner record/class representing a discrete executable time slot.
     */
    public static class TimeSlot {
        private final LocalDate date;
        private final LocalTime start;
        private final LocalTime end;
        private final int durationMinutes;

        public TimeSlot(LocalDate date, LocalTime start, LocalTime end, int durationMinutes) {
            this.date = date;
            this.start = start;
            this.end = end;
            this.durationMinutes = durationMinutes;
        }

        public LocalDate getDate() {
            return date;
        }

        public LocalTime getStart() {
            return start;
        }

        public LocalTime getEnd() {
            return end;
        }

        public int getDurationMinutes() {
            return durationMinutes;
        }
    }

    @Override
    public String toString() {
        return "Availability{" +
                "id=" + id +
                ", date=" + availableDate +
                ", " + startTime + " to " + endTime +
                ", slot=" + slotDurationMinutes + "m, break=" + breakDurationMinutes + "m" +
                '}';
    }
}

