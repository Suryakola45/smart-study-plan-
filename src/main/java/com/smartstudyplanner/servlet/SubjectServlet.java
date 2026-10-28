package com.smartstudyplanner.servlet;

import com.smartstudyplanner.exception.InvalidStudyDataException;
import com.smartstudyplanner.model.Subject;
import com.smartstudyplanner.model.User;
import com.smartstudyplanner.service.SubjectService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller servlet for Subject CRUD operations (Requirement 2).
 */
@WebServlet(urlPatterns = {"/subjects", "/subjects/create", "/subjects/update", "/subjects/delete"})
public class SubjectServlet extends HttpServlet {

    private SubjectService subjectService;

    @Override
    public void init() throws ServletException {
        this.subjectService = new SubjectService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute("currentUser");
        List<Subject> subjects = subjectService.getSubjectsWithTopics(user.getId());

        req.setAttribute("subjects", subjects);
        req.setAttribute("today", LocalDate.now());
        req.getRequestDispatcher("/WEB-INF/views/subjects.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();
        User user = (User) req.getSession().getAttribute("currentUser");
        HttpSession session = req.getSession();

        try {
            if ("/subjects/create".equals(path)) {
                String name = req.getParameter("name");
                String code = req.getParameter("code");
                String examDateStr = req.getParameter("examDate");
                String colorHex = req.getParameter("colorHex");
                String description = req.getParameter("description");

                LocalDate examDate = (examDateStr != null && !examDateStr.isBlank())
                        ? LocalDate.parse(examDateStr) : null;

                subjectService.createSubject(user.getId(), name, code, examDate, colorHex, description);
                session.setAttribute("successMessage", "Subject '" + name + "' added successfully!");

            } else if ("/subjects/update".equals(path)) {
                Long id = Long.parseLong(req.getParameter("id"));
                String name = req.getParameter("name");
                String code = req.getParameter("code");
                String examDateStr = req.getParameter("examDate");
                String colorHex = req.getParameter("colorHex");
                String description = req.getParameter("description");

                LocalDate examDate = (examDateStr != null && !examDateStr.isBlank())
                        ? LocalDate.parse(examDateStr) : null;

                subjectService.updateSubject(id, user.getId(), name, code, examDate, colorHex, description);
                session.setAttribute("successMessage", "Subject '" + name + "' updated successfully!");

            } else if ("/subjects/delete".equals(path)) {
                Long id = Long.parseLong(req.getParameter("id"));
                subjectService.deleteSubject(id, user.getId());
                session.setAttribute("successMessage", "Subject deleted successfully.");
            }
        } catch (InvalidStudyDataException e) {
            session.setAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            session.setAttribute("errorMessage", "Operation failed: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/subjects");
    }
}

