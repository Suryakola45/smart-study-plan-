package com.smartstudyplanner.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StudyPlan represents a generated or rescheduled schedule composite.
 * Demonstrates Composition by encapsulating a collection of StudySessions
 * and aggregated schedule statistics.
 */
public class StudyPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate startDate;
    private LocalDate endDate;
    private String strategyName;
    private List<StudySession> sessions = new ArrayList<>();

    public StudyPlan() {
    }

    public StudyPlan(LocalDate startDate, LocalDate endDate, String strategyName) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.strategyName = strategyName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public List<StudySession> getSessions() {
        return sessions;
    }

    public void setSessions(List<StudySession> sessions) {
        this.sessions = sessions != null ? sessions : new ArrayList<>();
    }

    public void addSession(StudySession session) {
        if (session != null) {
            this.sessions.add(session);
        }
    }

    public int getTotalSessions() {
        return sessions.size();
    }

    public int getTotalPlannedMinutes() {
        return sessions.stream().mapToInt(StudySession::getDurationMinutes).sum();
    }

    public int getTotalActualMinutesStudied() {
        return sessions.stream().mapToInt(StudySession::getActualMinutesStudied).sum();
    }

    /**
     * Groups sessions by date into a Map for convenient day-by-day calendar display.
     */
    public Map<LocalDate, List<StudySession>> getSessionsByDate() {
        Map<LocalDate, List<StudySession>> map = new HashMap<>();
        for (StudySession s : sessions) {
            map.computeIfAbsent(s.getSessionDate(), k -> new ArrayList<>()).add(s);
        }
        return map;
    }
}

