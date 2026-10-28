package com.smartstudyplanner.servlet;

import com.smartstudyplanner.service.StudyPlanService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller servlet for execution actions on individual study sessions (Requirement 7).
 * Supports: Complete, Skip, and Partially Complete.
 */
@WebServlet("/session/action")
public class SessionActionServlet extends HttpServlet {

    private StudyPlanService studyPlanService;

    @Override
    public void init() throws ServletException {
        this.studyPlanService = new StudyPlanService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        String idStr = req.getParameter("id");
        String action = req.getParameter("action");
        String notes = req.getParameter("notes");
        String returnUrl = req.getParameter("returnUrl");

        if (idStr == null || action == null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        try {
            Long sessionId = Long.parseLong(idStr);

            if ("complete".equalsIgnoreCase(action)) {
                String minutesStr = req.getParameter("actualMinutes");
                int minutes = (minutesStr != null && !minutesStr.isBlank()) ? Integer.parseInt(minutesStr) : 0;
                studyPlanService.completeSession(sessionId, minutes, notes);
                session.setAttribute("successMessage", "Session marked as completed! Progress updated.");

            } else if ("skip".equalsIgnoreCase(action)) {
                studyPlanService.skipSession(sessionId, notes != null ? notes : "Skipped by user");
                session.setAttribute("warningMessage", "Session marked as skipped. You can reschedule anytime!");

            } else if ("partial".equalsIgnoreCase(action)) {
                String minutesStr = req.getParameter("actualMinutes");
                int minutes = (minutesStr != null && !minutesStr.isBlank()) ? Integer.parseInt(minutesStr) : 30;
                studyPlanService.partiallyCompleteSession(sessionId, minutes, notes);
                session.setAttribute("successMessage", "Recorded " + minutes + " minutes of partial study.");
            }
        } catch (Exception e) {
            session.setAttribute("errorMessage", "Could not update session: " + e.getMessage());
        }

        if (returnUrl != null && !returnUrl.isBlank()) {
            resp.sendRedirect(req.getContextPath() + returnUrl);
        } else {
            resp.sendRedirect(req.getContextPath() + "/schedule");
        }
    }
}

