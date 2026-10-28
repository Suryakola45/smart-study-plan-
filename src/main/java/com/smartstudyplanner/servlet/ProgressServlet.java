package com.smartstudyplanner.servlet;

import com.google.gson.Gson;
import com.smartstudyplanner.model.User;
import com.smartstudyplanner.service.ProgressService;
import com.smartstudyplanner.util.GsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller servlet for Progress Tracking and Analytics (Requirement 9).
 */
@WebServlet("/progress")
public class ProgressServlet extends HttpServlet {

    private ProgressService progressService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        this.progressService = new ProgressService();
        this.gson = GsonUtil.getGson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute("currentUser");
        ProgressService.ProgressSummary summary = progressService.getProgressSummary(user.getId());

        req.setAttribute("summary", summary);
        req.setAttribute("weeklyStatsJson", gson.toJson(summary.getWeeklyStudyStats()));
        req.setAttribute("subjectProgressJson", gson.toJson(summary.getSubjectProgressList()));

        req.getRequestDispatcher("/WEB-INF/views/progress.jsp").forward(req, resp);
    }
}

