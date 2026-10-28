package com.smartstudyplanner.servlet;

import com.google.gson.Gson;
import com.smartstudyplanner.model.StudySession;
import com.smartstudyplanner.model.Subject;
import com.smartstudyplanner.model.User;
import com.smartstudyplanner.service.ProgressService;
import com.smartstudyplanner.service.StudyPlanService;
import com.smartstudyplanner.service.SubjectService;
import com.smartstudyplanner.util.GsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller servlet for the central student dashboard.
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private SubjectService subjectService;
    private StudyPlanService studyPlanService;
    private ProgressService progressService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        this.subjectService = new SubjectService();
        this.studyPlanService = new StudyPlanService();
        this.progressService = new ProgressService();
        this.gson = GsonUtil.getGson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute("currentUser");
        LocalDate today = LocalDate.now();

        // 1. Fetch Today's scheduled sessions
        List<StudySession> todaySessions = studyPlanService.getSessionsForDate(user.getId(), today);

        // 2. Fetch Subjects
        List<Subject> subjects = subjectService.getSubjectsWithTopics(user.getId());

        // 3. Fetch comprehensive progress summary
        ProgressService.ProgressSummary summary = progressService.getProgressSummary(user.getId());

        req.setAttribute("todaySessions", todaySessions);
        req.setAttribute("subjects", subjects);
        req.setAttribute("summary", summary);
        req.setAttribute("todayDate", today);
        req.setAttribute("weeklyStatsJson", gson.toJson(summary.getWeeklyStudyStats()));

        req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
    }
}

