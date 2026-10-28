package com.smartstudyplanner.service;

import com.smartstudyplanner.dao.StudySessionDAO;
import com.smartstudyplanner.dao.SubjectDAO;
import com.smartstudyplanner.dao.TopicDAO;
import com.smartstudyplanner.model.SessionStatus;
import com.smartstudyplanner.model.StudySession;
import com.smartstudyplanner.model.Subject;
import com.smartstudyplanner.model.Topic;
import com.smartstudyplanner.model.TopicStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for calculating comprehensive study analytics, completion metrics,
 * subject-wise progress, and weekly statistics (Requirement 9).
 */
public class ProgressService {

    private final SubjectDAO subjectDAO;
    private final TopicDAO topicDAO;
    private final StudySessionDAO sessionDAO;

    public ProgressService() {
        this.subjectDAO = new SubjectDAO();
        this.topicDAO = new TopicDAO();
        this.sessionDAO = new StudySessionDAO();
    }

    public ProgressService(SubjectDAO subjectDAO, TopicDAO topicDAO, StudySessionDAO sessionDAO) {
        this.subjectDAO = subjectDAO;
        this.topicDAO = topicDAO;
        this.sessionDAO = sessionDAO;
    }

    public ProgressSummary getProgressSummary(Long userId) {
        ProgressSummary summary = new ProgressSummary();
        LocalDate today = LocalDate.now();

        // 1. Subjects & Topics
        List<Subject> subjects = subjectDAO.findByUserId(userId);
        List<Topic> allTopics = topicDAO.findByUserId(userId);

        summary.setTotalSubjects(subjects.size());
        summary.setTotalTopics(allTopics.size());

        int completedTopics = 0;
        int inProgressTopics = 0;
        int pendingTopics = 0;
        int totalEstMinutes = 0;
        int totalCompMinutes = 0;

        for (Topic t : allTopics) {
            totalEstMinutes += t.getEstimatedMinutes();
            totalCompMinutes += t.getCompletedMinutes();

            if (TopicStatus.COMPLETED.name().equalsIgnoreCase(t.getStatus())) {
                completedTopics++;
            } else if (TopicStatus.IN_PROGRESS.name().equalsIgnoreCase(t.getStatus())) {
                inProgressTopics++;
            } else {
                pendingTopics++;
            }
        }

        summary.setCompletedTopics(completedTopics);
        summary.setInProgressTopics(inProgressTopics);
        summary.setPendingTopics(pendingTopics);
        summary.setTotalEstimatedMinutes(totalEstMinutes);
        summary.setTotalCompletedMinutes(totalCompMinutes);
        summary.setRemainingMinutes(Math.max(0, totalEstMinutes - totalCompMinutes));

        double overallPct = (allTopics.isEmpty()) ? 0.0 :
                ((double) completedTopics / allTopics.size()) * 100.0;
        summary.setOverallCompletionPercentage(Math.round(overallPct * 10.0) / 10.0);

        // 2. Study Sessions
        List<StudySession> sessions = sessionDAO.findByUserId(userId);
        summary.setTotalSessions(sessions.size());

        int completedSessions = 0;
        int skippedSessions = 0;
        int scheduledSessions = 0;
        int actualMinutesStudied = 0;

        for (StudySession s : sessions) {
            actualMinutesStudied += s.getActualMinutesStudied();
            if (SessionStatus.COMPLETED.name().equalsIgnoreCase(s.getStatus()) ||
                SessionStatus.PARTIALLY_COMPLETED.name().equalsIgnoreCase(s.getStatus())) {
                completedSessions++;
            } else if (SessionStatus.SKIPPED.name().equalsIgnoreCase(s.getStatus())) {
                skippedSessions++;
            } else if (SessionStatus.SCHEDULED.name().equalsIgnoreCase(s.getStatus())) {
                scheduledSessions++;
            }
        }

        summary.setCompletedSessions(completedSessions);
        summary.setSkippedSessions(skippedSessions);
        summary.setScheduledSessions(scheduledSessions);
        summary.setActualMinutesStudied(actualMinutesStudied);

        // 3. Subject-wise Progress
        List<SubjectProgressItem> subjectProgressList = new ArrayList<>();
        for (Subject s : subjects) {
            List<Topic> subTopics = topicDAO.findBySubjectId(s.getId());
            int subTotal = subTopics.size();
            long subCompleted = subTopics.stream()
                    .filter(t -> TopicStatus.COMPLETED.name().equalsIgnoreCase(t.getStatus()))
                    .count();
            int subEst = subTopics.stream().mapToInt(Topic::getEstimatedMinutes).sum();
            int subComp = subTopics.stream().mapToInt(Topic::getCompletedMinutes).sum();
            double subPct = subTotal == 0 ? 0.0 : ((double) subCompleted / subTotal) * 100.0;

            SubjectProgressItem item = new SubjectProgressItem(
                    s.getId(),
                    s.getName(),
                    s.getCode(),
                    s.getColorHex(),
                    s.getExamDate(),
                    s.getDaysUntilExam(today),
                    subTotal,
                    (int) subCompleted,
                    Math.round(subPct * 10.0) / 10.0,
                    subEst,
                    subComp
            );
            subjectProgressList.add(item);
        }
        summary.setSubjectProgressList(subjectProgressList);

        // 4. Weekly Study Statistics (Last 7 days)
        LocalDate weekStart = today.minusDays(6);
        Map<LocalDate, Integer> dbWeekly = sessionDAO.getWeeklyStudyMinutes(userId, weekStart, today);
        Map<String, Integer> weeklyStats = new LinkedHashMap<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE (MMM d)");

        for (int i = 0; i < 7; i++) {
            LocalDate d = weekStart.plusDays(i);
            String label = d.format(dayFormatter);
            weeklyStats.put(label, dbWeekly.getOrDefault(d, 0));
        }
        summary.setWeeklyStudyStats(weeklyStats);

        // 5. AI Recommendations
        List<String> recommendations = new ArrayList<>();
        if (summary.getOverallCompletionPercentage() < 30.0) {
            recommendations.add("Your overall progress is in the early stages. Allocate focused slots every evening.");
        }
        for (SubjectProgressItem sp : subjectProgressList) {
            if (sp.daysUntilExam <= 14 && sp.completionPercentage < 50.0) {
                recommendations.add("Urgent: " + sp.subjectName + " exam is in " + sp.daysUntilExam +
                        " days with only " + sp.completionPercentage + "% completed! Consider running the AI Rescheduler.");
            }
        }
        if (recommendations.isEmpty()) {
            recommendations.add("You are pacing well! Maintain consistency and complete your scheduled daily slots.");
        }
        summary.setAiRecommendations(recommendations);

        return summary;
    }

    public static class ProgressSummary implements Serializable {
        private static final long serialVersionUID = 1L;

        private int totalSubjects;
        private int totalTopics;
        private int completedTopics;
        private int inProgressTopics;
        private int pendingTopics;
        private double overallCompletionPercentage;

        private int totalEstimatedMinutes;
        private int totalCompletedMinutes;
        private int remainingMinutes;

        private int totalSessions;
        private int completedSessions;
        private int skippedSessions;
        private int scheduledSessions;
        private int actualMinutesStudied;

        private List<SubjectProgressItem> subjectProgressList = new ArrayList<>();
        private Map<String, Integer> weeklyStudyStats = new LinkedHashMap<>();
        private List<String> aiRecommendations = new ArrayList<>();

        public int getTotalSubjects() { return totalSubjects; }
        public void setTotalSubjects(int totalSubjects) { this.totalSubjects = totalSubjects; }

        public int getTotalTopics() { return totalTopics; }
        public void setTotalTopics(int totalTopics) { this.totalTopics = totalTopics; }

        public int getCompletedTopics() { return completedTopics; }
        public void setCompletedTopics(int completedTopics) { this.completedTopics = completedTopics; }

        public int getInProgressTopics() { return inProgressTopics; }
        public void setInProgressTopics(int inProgressTopics) { this.inProgressTopics = inProgressTopics; }

        public int getPendingTopics() { return pendingTopics; }
        public void setPendingTopics(int pendingTopics) { this.pendingTopics = pendingTopics; }

        public double getOverallCompletionPercentage() { return overallCompletionPercentage; }
        public void setOverallCompletionPercentage(double overallCompletionPercentage) { this.overallCompletionPercentage = overallCompletionPercentage; }

        public int getTotalEstimatedMinutes() { return totalEstimatedMinutes; }
        public void setTotalEstimatedMinutes(int totalEstimatedMinutes) { this.totalEstimatedMinutes = totalEstimatedMinutes; }

        public int getTotalCompletedMinutes() { return totalCompletedMinutes; }
        public void setTotalCompletedMinutes(int totalCompletedMinutes) { this.totalCompletedMinutes = totalCompletedMinutes; }

        public int getRemainingMinutes() { return remainingMinutes; }
        public void setRemainingMinutes(int remainingMinutes) { this.remainingMinutes = remainingMinutes; }

        public int getTotalSessions() { return totalSessions; }
        public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }

        public int getCompletedSessions() { return completedSessions; }
        public void setCompletedSessions(int completedSessions) { this.completedSessions = completedSessions; }

        public int getSkippedSessions() { return skippedSessions; }
        public void setSkippedSessions(int skippedSessions) { this.skippedSessions = skippedSessions; }

        public int getScheduledSessions() { return scheduledSessions; }
        public void setScheduledSessions(int scheduledSessions) { this.scheduledSessions = scheduledSessions; }

        public int getActualMinutesStudied() { return actualMinutesStudied; }
        public void setActualMinutesStudied(int actualMinutesStudied) { this.actualMinutesStudied = actualMinutesStudied; }

        public List<SubjectProgressItem> getSubjectProgressList() { return subjectProgressList; }
        public void setSubjectProgressList(List<SubjectProgressItem> subjectProgressList) { this.subjectProgressList = subjectProgressList; }

        public Map<String, Integer> getWeeklyStudyStats() { return weeklyStudyStats; }
        public void setWeeklyStudyStats(Map<String, Integer> weeklyStudyStats) { this.weeklyStudyStats = weeklyStudyStats; }

        public List<String> getAiRecommendations() { return aiRecommendations; }
        public void setAiRecommendations(List<String> aiRecommendations) { this.aiRecommendations = aiRecommendations; }
    }

    public static class SubjectProgressItem implements Serializable {
        private static final long serialVersionUID = 1L;

        public final Long subjectId;
        public final String subjectName;
        public final String subjectCode;
        public final String colorHex;
        public final LocalDate examDate;
        public final long daysUntilExam;
        public final int totalTopics;
        public final int completedTopics;
        public final double completionPercentage;
        public final int estimatedMinutes;
        public final int completedMinutes;

        public SubjectProgressItem(Long subjectId, String subjectName, String subjectCode,
                                   String colorHex, LocalDate examDate, long daysUntilExam,
                                   int totalTopics, int completedTopics, double completionPercentage,
                                   int estimatedMinutes, int completedMinutes) {
            this.subjectId = subjectId;
            this.subjectName = subjectName;
            this.subjectCode = subjectCode;
            this.colorHex = colorHex;
            this.examDate = examDate;
            this.daysUntilExam = daysUntilExam;
            this.totalTopics = totalTopics;
            this.completedTopics = completedTopics;
            this.completionPercentage = completionPercentage;
            this.estimatedMinutes = estimatedMinutes;
            this.completedMinutes = completedMinutes;
        }

        public Long getSubjectId() { return subjectId; }
        public String getSubjectName() { return subjectName; }
        public String getSubjectCode() { return subjectCode; }
        public String getColorHex() { return colorHex; }
        public LocalDate getExamDate() { return examDate; }
        public long getDaysUntilExam() { return daysUntilExam; }
        public int getTotalTopics() { return totalTopics; }
        public int getCompletedTopics() { return completedTopics; }
        public double getCompletionPercentage() { return completionPercentage; }
        public int getEstimatedMinutes() { return estimatedMinutes; }
        public int getCompletedMinutes() { return completedMinutes; }
    }
}

