package com.smartstudyplanner.model;

import java.time.LocalDate;

/**
 * Topic entity representing an individual unit of study within a subject.
 * Demonstrates Encapsulation through private fields, domain constraints,
 * and business helper methods.
 */
public class Topic extends BaseEntity implements Comparable<Topic> {

    private static final long serialVersionUID = 1L;

    private Long subjectId;
    private String title;
    private String description;
    private int difficulty;          // 1 (Easiest) to 5 (Hardest)
    private int confidenceLevel;     // 1 (Very Low) to 5 (Mastered)
    private int estimatedMinutes;    // Total planned duration in minutes
    private int completedMinutes;    // Accumulated actual study minutes
    private String status;           // PENDING, IN_PROGRESS, COMPLETED
    private double calculatedPriority; // AI calculated priority score (0.0 - 1.0)

    // Transient attributes populated from joins for view convenience
    private String subjectName;
    private String subjectCode;
    private LocalDate examDate;
    private String subjectColorHex;

    public Topic() {
        super();
        this.difficulty = 3;
        this.confidenceLevel = 3;
        this.estimatedMinutes = 60;
        this.completedMinutes = 0;
        this.status = TopicStatus.PENDING.name();
        this.calculatedPriority = 0.5;
    }

    public Topic(Long id, Long subjectId, String title, String description,
                 int difficulty, int confidenceLevel, int estimatedMinutes) {
        super(id);
        this.subjectId = subjectId;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.confidenceLevel = confidenceLevel;
        this.estimatedMinutes = estimatedMinutes;
        this.completedMinutes = 0;
        this.status = TopicStatus.PENDING.name();
        this.calculatedPriority = 0.0;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null ? title.trim() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        if (difficulty < 1) this.difficulty = 1;
        else if (difficulty > 5) this.difficulty = 5;
        else this.difficulty = difficulty;
    }

    public int getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(int confidenceLevel) {
        if (confidenceLevel < 1) this.confidenceLevel = 1;
        else if (confidenceLevel > 5) this.confidenceLevel = 5;
        else this.confidenceLevel = confidenceLevel;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(int estimatedMinutes) {
        this.estimatedMinutes = Math.max(1, estimatedMinutes);
    }

    public int getCompletedMinutes() {
        return completedMinutes;
    }

    public void setCompletedMinutes(int completedMinutes) {
        this.completedMinutes = Math.max(0, completedMinutes);
        updateStatusBasedOnProgress();
    }

    public String getStatus() {
        return status != null ? status : TopicStatus.PENDING.name();
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getCalculatedPriority() {
        return calculatedPriority;
    }

    public void setCalculatedPriority(double calculatedPriority) {
        this.calculatedPriority = Math.max(0.0, Math.min(1.0, calculatedPriority));
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

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public String getSubjectColorHex() {
        return subjectColorHex != null ? subjectColorHex : "#4361ee";
    }

    public void setSubjectColorHex(String subjectColorHex) {
        this.subjectColorHex = subjectColorHex;
    }

    // Domain methods demonstrating business logic encapsulation

    public int getRemainingMinutes() {
        return Math.max(0, estimatedMinutes - completedMinutes);
    }

    public boolean isCompleted() {
        return TopicStatus.COMPLETED.name().equalsIgnoreCase(status) || completedMinutes >= estimatedMinutes;
    }

    public void addCompletedMinutes(int additionalMinutes) {
        if (additionalMinutes > 0) {
            this.completedMinutes += additionalMinutes;
            updateStatusBasedOnProgress();
        }
    }

    private void updateStatusBasedOnProgress() {
        if (this.completedMinutes >= this.estimatedMinutes) {
            this.status = TopicStatus.COMPLETED.name();
        } else if (this.completedMinutes > 0) {
            this.status = TopicStatus.IN_PROGRESS.name();
        } else {
            this.status = TopicStatus.PENDING.name();
        }
    }

    public double getCompletionPercentage() {
        if (estimatedMinutes <= 0) return 100.0;
        double pct = ((double) completedMinutes / estimatedMinutes) * 100.0;
        return Math.min(100.0, Math.round(pct * 10.0) / 10.0);
    }

    /**
     * Default natural ordering by calculated priority descending.
     */
    @Override
    public int compareTo(Topic other) {
        if (other == null) return 1;
        return Double.compare(other.calculatedPriority, this.calculatedPriority);
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", difficulty=" + difficulty +
                ", confidenceLevel=" + confidenceLevel +
                ", estimatedMinutes=" + estimatedMinutes +
                ", completedMinutes=" + completedMinutes +
                ", status='" + status + '\'' +
                ", priority=" + String.format("%.2f", calculatedPriority) +
                '}';
    }
}

