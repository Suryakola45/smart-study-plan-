package com.smartstudyplanner.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Subject entity representing an academic subject or course.
 * Demonstrates Composition by owning a collection of Topics.
 */
public class Subject extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String name;
    private String code;
    private LocalDate examDate;
    private String colorHex;
    private String description;

    // Composition: A Subject is composed of multiple Topics
    private List<Topic> topics = new ArrayList<>();

    public Subject() {
        super();
        this.colorHex = "#4361ee";
    }

    public Subject(Long id, Long userId, String name, String code, LocalDate examDate, String colorHex, String description) {
        super(id);
        this.userId = userId;
        this.name = name;
        this.code = code;
        this.examDate = examDate;
        this.colorHex = (colorHex != null && !colorHex.isBlank()) ? colorHex : "#4361ee";
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code != null ? code.trim().toUpperCase() : null;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public String getColorHex() {
        return colorHex != null ? colorHex : "#4361ee";
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics != null ? topics : new ArrayList<>();
    }

    public void addTopic(Topic topic) {
        if (topic != null) {
            topic.setSubjectId(this.id);
            this.topics.add(topic);
        }
    }

    /**
     * Calculates days until examination relative to a given date.
     * Returns negative value if exam date is in the past.
     */
    public long getDaysUntilExam(LocalDate referenceDate) {
        if (examDate == null || referenceDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(referenceDate, examDate);
    }

    public boolean isExamPast(LocalDate referenceDate) {
        return getDaysUntilExam(referenceDate) < 0;
    }

    /**
     * Calculates completion percentage for all topics within this subject.
     */
    public double getProgressPercentage() {
        if (topics.isEmpty()) {
            return 0.0;
        }
        long completed = topics.stream()
                .filter(t -> TopicStatus.COMPLETED.name().equalsIgnoreCase(t.getStatus()))
                .count();
        return Math.round(((double) completed / topics.size()) * 1000.0) / 10.0;
    }

    public int getTotalEstimatedMinutes() {
        return topics.stream().mapToInt(Topic::getEstimatedMinutes).sum();
    }

    public int getTotalCompletedMinutes() {
        return topics.stream().mapToInt(Topic::getCompletedMinutes).sum();
    }

    @Override
    public String toString() {
        return "Subject{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", examDate=" + examDate +
                ", topicsCount=" + topics.size() +
                '}';
    }
}

