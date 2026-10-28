package com.smartstudyplanner.servlet;

import com.smartstudyplanner.exception.InsufficientStudyTimeException;
import com.smartstudyplanner.exception.PlanGenerationException;
import com.smartstudyplanner.model.StudyPlan;
import com.smartstudyplanner.model.StudySession;
import com.smartstudyplanner.model.User;
import com.smartstudyplanner.service.AuthService;
import com.smartstudyplanner.service.StudyPlanService;
import com.smartstudyplanner.strategy.PlanningStrategyFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller servlet for Study Schedule view, generation, and AI rescheduling (Requirements 6, 8, 11).
 */
@WebServlet(urlPatterns = {"/schedule", "/schedule/generate", "/schedule/reschedule"})
public class ScheduleServlet extends HttpServlet {

    private StudyPlanService studyPlanService;
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        this.studyPlanService = new StudyPlanService();
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute("currentUser");
        String viewMode = req.getParameter("view"); // "day" or "week"
        if (viewMode == null || viewMode.isBlank()) {
            viewMode = "week";
        }

        String dateParam = req.getParameter("date");
        LocalDate targetDate = (dateParam != null && !dateParam.isBlank())
                ? LocalDate.parse(dateParam) : LocalDate.now();

        List<StudySession> sessions;
        LocalDate startDate;
        LocalDate endDate;

        if ("day".equalsIgnoreCase(viewMode)) {
            startDate = targetDate;
            endDate = targetDate;
            sessions = studyPlanService.getSessionsForDate(user.getId(), targetDate);
        } else {
            startDate = targetDate;
            endDate = targetDate.plusDays(6);
            sessions = studyPlanService.getSessionsForDateRange(user.getId(), startDate, endDate);
        }

        // Group sessions by date for the calendar view
        Map<LocalDate, List<StudySession>> sessionsByDate = new LinkedHashMap<>();
        LocalDate cur = startDate;
        while (!cur.isAfter(endDate)) {
            sessionsByDate.put(cur, new ArrayList<>());
            cur = cur.plusDays(1);
        }
        for (StudySession s : sessions) {
            sessionsByDate.computeIfAbsent(s.getSessionDate(), k -> new ArrayList<>()).add(s);
        }

        req.setAttribute("viewMode", viewMode);
        req.setAttribute("targetDate", targetDate);
        req.setAttribute("startDate", startDate);
        req.setAttribute("endDate", endDate);
        req.setAttribute("sessionsByDate", sessionsByDate);
        req.setAttribute("sessions", sessions);
        req.setAttribute("strategies", PlanningStrategyFactory.getAllStrategies());
        req.setAttribute("currentStrategy", user.getPreferredStrategy());

        req.getRequestDispatcher("/WEB-INF/views/schedule.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();
        User user = (User) req.getSession().getAttribute("currentUser");
        HttpSession session = req.getSession();

        String strategy = req.getParameter("strategy");
        if (strategy == null || strategy.isBlank()) {
            strategy = user.getPreferredStrategy();
        } else {
            // Update preferred strategy
            user.setPreferredStrategy(strategy);
            authService.updatePreferredStrategy(user.getId(), strategy);
        }

        try {
            if ("/schedule/generate".equals(path)) {
                String rangeType = req.getParameter("rangeType"); // "day" or "week"
                String dateStr = req.getParameter("startDate");
                LocalDate start = (dateStr != null && !dateStr.isBlank())
                        ? LocalDate.parse(dateStr) : LocalDate.now();

                StudyPlan plan;
                if ("day".equalsIgnoreCase(rangeType)) {
                    plan = studyPlanService.generateDailyPlan(user.getId(), start, strategy);
                    session.setAttribute("successMessage", "Generated daily study plan with " +
                            plan.getTotalSessions() + " sessions (" + plan.getTotalPlannedMinutes() + " mins).");
                } else {
                    plan = studyPlanService.generateWeeklyPlan(user.getId(), start, strategy);
                    session.setAttribute("successMessage", "Generated 7-day weekly study schedule with " +
                            plan.getTotalSessions() + " sessions (" + plan.getTotalPlannedMinutes() + " mins).");
                }

            } else if ("/schedule/reschedule".equals(path)) {
                LocalDate baseDate = LocalDate.now();
                StudyPlan plan = studyPlanService.reschedule(user.getId(), baseDate, strategy);
                session.setAttribute("successMessage", "AI Rescheduling complete! Re-allocated " +
                        plan.getTotalSessions() + " sessions for pending & missed topics without altering completed history.");
            }
        } catch (InsufficientStudyTimeException e) {
            session.setAttribute("warningMessage", e.getMessage() + " Deficit: " + e.getDeficitMinutes() + " minutes.");
        } catch (PlanGenerationException e) {
            session.setAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            session.setAttribute("errorMessage", "Schedule generation error: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/schedule");
    }
}

