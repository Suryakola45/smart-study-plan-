package com.smartstudyplanner.servlet;

import com.smartstudyplanner.exception.InvalidStudyDataException;
import com.smartstudyplanner.model.Availability;
import com.smartstudyplanner.model.User;
import com.smartstudyplanner.service.AvailabilityService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller servlet for Availability management (Requirement 4).
 */
@WebServlet(urlPatterns = {"/availability", "/availability/add", "/availability/delete", "/availability/quick-week"})
public class AvailabilityServlet extends HttpServlet {

    private AvailabilityService availabilityService;

    @Override
    public void init() throws ServletException {
        this.availabilityService = new AvailabilityService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute("currentUser");
        List<Availability> list = availabilityService.getAvailabilityByUser(user.getId());

        req.setAttribute("availabilities", list);
        req.setAttribute("today", LocalDate.now());
        req.getRequestDispatcher("/WEB-INF/views/availability.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();
        User user = (User) req.getSession().getAttribute("currentUser");
        HttpSession session = req.getSession();

        try {
            if ("/availability/add".equals(path)) {
                LocalDate date = LocalDate.parse(req.getParameter("availableDate"));
                LocalTime startTime = LocalTime.parse(req.getParameter("startTime"));
                LocalTime endTime = LocalTime.parse(req.getParameter("endTime"));
                int slotMinutes = Integer.parseInt(req.getParameter("slotMinutes"));
                int breakMinutes = Integer.parseInt(req.getParameter("breakMinutes"));

                availabilityService.addAvailability(user.getId(), date, startTime, endTime, slotMinutes, breakMinutes);
                session.setAttribute("successMessage", "Availability slot added for " + date + ".");

            } else if ("/availability/quick-week".equals(path)) {
                // Convenience generator: adds evening slots for the next 7 days
                LocalTime start = LocalTime.parse(req.getParameter("startTime"));
                LocalTime end = LocalTime.parse(req.getParameter("endTime"));
                int slotMinutes = Integer.parseInt(req.getParameter("slotMinutes"));
                int breakMinutes = Integer.parseInt(req.getParameter("breakMinutes"));

                LocalDate today = LocalDate.now();
                int added = 0;
                for (int i = 0; i < 7; i++) {
                    LocalDate d = today.plusDays(i);
                    try {
                        availabilityService.addAvailability(user.getId(), d, start, end, slotMinutes, breakMinutes);
                        added++;
                    } catch (Exception ignored) {
                    }
                }
                session.setAttribute("successMessage", "Generated " + added + " daily availability slots for the upcoming week!");

            } else if ("/availability/delete".equals(path)) {
                Long id = Long.parseLong(req.getParameter("id"));
                availabilityService.deleteAvailability(id, user.getId());
                session.setAttribute("successMessage", "Availability slot removed.");
            }
        } catch (InvalidStudyDataException e) {
            session.setAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            session.setAttribute("errorMessage", "Error: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/availability");
    }
}

