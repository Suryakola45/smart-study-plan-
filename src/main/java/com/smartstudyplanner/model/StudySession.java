package com.smartstudyplanner.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * StudySession entity representing a single scheduled study execution block.
 * Demonstrates Encapsulation through state transition methods.
 */
public class StudySession extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long topicId;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int durationMinutes;
    private int actualMinutesStudied;
    private String status;           // SCHEDULED, COMPLETED, SKIPPED, PARTIALLY_COMPLETED
    private String notes;

    // Transient attributes for view layer display
    private String topicTitle;
    private String subjectName;
    private String subjectCode;
    private String subjectColorHex;
    private LocalDate examDate;

    public StudySession() {
        super();
        this.status = SessionStatus.SCHEDULED.name();
        this.actualMinutesStudied = 0;
    }

    public StudySession(Long id, Long userId, Long topicId, LocalDate sessionDate,
                        LocalTime startTime, LocalTime endTime, int durationMinutes) {
        super(id);
        this.userId = userId;
        this.topicId = topicId;
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.actualMinutesStudied = 0;
        this.status = SessionStatus.SCHEDULED.name();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
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

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = Math.max(1, durationMinutes);
    }

    public int getActualMinutesStudied() {
        return actualMinutesStudied;
    }

    public void setActualMinutesStudied(int actualMinutesStudied) {
        this.actualMinutesStudied = Math.max(0, actualMinutesStudied);
    }

    public String getStatus() {
        return status != null ? status : SessionStatus.SCHEDULED.name();
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTopicTitle() {
        return topicTitle;
    }

    public void setTopicTitle(String topicTitle) {
        this.topicTitle = topicTitle;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectColorHex() {
        return subjectColorHex != null ? subjectColorHex : "#4361ee";
    }

    public void setSubjectColorHex(String subjectColorHex) {
        this.subjectColorHex = subjectColorHex;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    // Domain state transition methods

    public void markCompleted(int minutes, String completionNotes) {
        this.actualMinutesStudied = minutes > 0 ? minutes : this.durationMinutes;
        this.status = SessionStatus.COMPLETED.name();
        if (completionNotes != null && !completionNotes.isBlank()) {
            this.notes = completionNotes;
        }
    }

    public void markSkipped(String skipReason) {
        this.status = SessionStatus.SKIPPED.name();
        this.actualMinutesStudied = 0;
        if (skipReason != null && !skipReason.isBlank()) {
            this.notes = "Skipped: " + skipReason;
        }
    }

    public void markPartiallyCompleted(int minutes, String partialNotes) {
        this.actualMinutesStudied = Math.max(1, minutes);
        this.status = SessionStatus.PARTIALLY_COMPLETED.name();
        if (partialNotes != null && !partialNotes.isBlank()) {
            this.notes = partialNotes;
        }
    }

    public boolean isFinished() {
        return SessionStatus.COMPLETED.name().equalsIgnoreCase(status)
                || SessionStatus.PARTIALLY_COMPLETED.name().equalsIgnoreCase(status);
    }

    public boolean isMissed(LocalDate today, LocalTime now) {
        if (!SessionStatus.SCHEDULED.name().equalsIgnoreCase(status)) {
            return false;
        }
        if (sessionDate.isBefore(today)) {
            return true;
        }
        return sessionDate.isEqual(today) && endTime.isBefore(now);
    }

    @Override
    public String toString() {
        return "StudySession{" +
                "id=" + id +
                ", date=" + sessionDate +
                ", " + startTime + "-" + endTime +
                ", duration=" + durationMinutes + "m" +
                ", status='" + status + '\'' +
                ", topicTitle='" + topicTitle + '\'' +
                '}';
    }
}

