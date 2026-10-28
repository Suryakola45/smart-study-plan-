package com.smartstudyplanner.service;

import com.smartstudyplanner.dao.AvailabilityDAO;
import com.smartstudyplanner.exception.InvalidStudyDataException;
import com.smartstudyplanner.model.Availability;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Service managing student study availability time windows.
 */
public class AvailabilityService {

    private final AvailabilityDAO availabilityDAO;

    public AvailabilityService() {
        this.availabilityDAO = new AvailabilityDAO();
    }

    public AvailabilityService(AvailabilityDAO availabilityDAO) {
        this.availabilityDAO = availabilityDAO;
    }

    public Availability addAvailability(Long userId, LocalDate date, LocalTime startTime, LocalTime endTime,
                                        int slotMinutes, int breakMinutes) throws InvalidStudyDataException {

        validateAvailability(date, startTime, endTime, slotMinutes, breakMinutes);

        Availability avail = new Availability();
        avail.setUserId(userId);
        avail.setAvailableDate(date);
        avail.setStartTime(startTime);
        avail.setEndTime(endTime);
        avail.setSlotDurationMinutes(slotMinutes);
        avail.setBreakDurationMinutes(breakMinutes);

        return availabilityDAO.save(avail);
    }

    public boolean updateAvailability(Long id, Long userId, LocalDate date, LocalTime startTime, LocalTime endTime,
                                      int slotMinutes, int breakMinutes) throws InvalidStudyDataException {

        validateAvailability(date, startTime, endTime, slotMinutes, breakMinutes);

        Optional<Availability> opt = availabilityDAO.findById(id);
        if (opt.isEmpty() || !opt.get().getUserId().equals(userId)) {
            return false;
        }

        Availability avail = opt.get();
        avail.setAvailableDate(date);
        avail.setStartTime(startTime);
        avail.setEndTime(endTime);
        avail.setSlotDurationMinutes(slotMinutes);
        avail.setBreakDurationMinutes(breakMinutes);

        return availabilityDAO.update(avail);
    }

    public boolean deleteAvailability(Long id, Long userId) {
        return availabilityDAO.deleteByIdAndUserId(id, userId);
    }

    public List<Availability> getAvailabilityByUser(Long userId) {
        return availabilityDAO.findByUserId(userId);
    }

    public List<Availability> getAvailabilityByDateRange(Long userId, LocalDate start, LocalDate end) {
        return availabilityDAO.findByUserIdAndDateRange(userId, start, end);
    }

    private void validateAvailability(LocalDate date, LocalTime startTime, LocalTime endTime,
                                     int slotMinutes, int breakMinutes) throws InvalidStudyDataException {
        if (date == null) {
            throw new InvalidStudyDataException("availableDate", "Availability date is required.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new InvalidStudyDataException("availableDate", "Cannot set availability for past dates.");
        }
        if (startTime == null || endTime == null) {
            throw new InvalidStudyDataException("time", "Start time and end time are required.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new InvalidStudyDataException("time", "End time must be after start time.");
        }

        long span = Duration.between(startTime, endTime).toMinutes();
        if (span < 30) {
            throw new InvalidStudyDataException("span", "Available study window must be at least 30 minutes.");
        }
        if (slotMinutes < 15 || slotMinutes > 180) {
            throw new InvalidStudyDataException("slotMinutes", "Study session slot must be between 15 and 180 minutes.");
        }
        if (breakMinutes < 0 || breakMinutes > 60) {
            throw new InvalidStudyDataException("breakMinutes", "Break duration must be between 0 and 60 minutes.");
        }
    }
}

