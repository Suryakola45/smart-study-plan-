package com.smartstudyplanner.servlet;

import com.smartstudyplanner.exception.InvalidStudyDataException;
import com.smartstudyplanner.model.Subject;
import com.smartstudyplanner.model.Topic;
import com.smartstudyplanner.model.User;
import com.smartstudyplanner.service.SubjectService;
import com.smartstudyplanner.service.TopicService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Controller servlet for Topic CRUD and AI priority refresh (Requirement 3 & 5).
 */
@WebServlet(urlPatterns = {"/topics", "/topics/create", "/topics/update", "/topics/delete", "/topics/prioritize"})
public class TopicServlet extends HttpServlet {

    private TopicService topicService;
    private SubjectService subjectService;

    @Override
    public void init() throws ServletException {
        this.topicService = new TopicService();
        this.subjectService = new SubjectService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute("currentUser");
        String subjectIdParam = req.getParameter("subjectId");

        List<Subject> subjects = subjectService.getSubjectsWithTopics(user.getId());
        List<Topic> topics;

        if (subjectIdParam != null && !subjectIdParam.isBlank()) {
            Long subjectId = Long.parseLong(subjectIdParam);
            topics = topicService.getTopicsBySubject(subjectId);
            req.setAttribute("selectedSubjectId", subjectId);
        } else {
            topics = topicService.getTopicsByUser(user.getId());
        }

        req.setAttribute("subjects", subjects);
        req.setAttribute("topics", topics);
        req.getRequestDispatcher("/WEB-INF/views/topics.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();
        User user = (User) req.getSession().getAttribute("currentUser");
        HttpSession session = req.getSession();

        try {
            if ("/topics/create".equals(path)) {
                Long subjectId = Long.parseLong(req.getParameter("subjectId"));
                String title = req.getParameter("title");
                String description = req.getParameter("description");
                int difficulty = Integer.parseInt(req.getParameter("difficulty"));
                int confidence = Integer.parseInt(req.getParameter("confidenceLevel"));
                int estimatedMinutes = Integer.parseInt(req.getParameter("estimatedMinutes"));

                topicService.createTopic(subjectId, title, description, difficulty, confidence, estimatedMinutes);
                session.setAttribute("successMessage", "Topic '" + title + "' created with AI priority calculated!");

            } else if ("/topics/update".equals(path)) {
                Long id = Long.parseLong(req.getParameter("id"));
                String title = req.getParameter("title");
                String description = req.getParameter("description");
                int difficulty = Integer.parseInt(req.getParameter("difficulty"));
                int confidence = Integer.parseInt(req.getParameter("confidenceLevel"));
                int estimatedMinutes = Integer.parseInt(req.getParameter("estimatedMinutes"));
                int completedMinutes = Integer.parseInt(req.getParameter("completedMinutes"));
                String status = req.getParameter("status");

                topicService.updateTopic(id, title, description, difficulty, confidence,
                        estimatedMinutes, completedMinutes, status);
                session.setAttribute("successMessage", "Topic '" + title + "' updated successfully!");

            } else if ("/topics/delete".equals(path)) {
                Long id = Long.parseLong(req.getParameter("id"));
                topicService.deleteTopic(id);
                session.setAttribute("successMessage", "Topic deleted successfully.");

            } else if ("/topics/prioritize".equals(path)) {
                topicService.refreshAllPrioritiesForUser(user.getId());
                session.setAttribute("successMessage", "AI Recommendation Engine re-evaluated all topic priorities!");
            }
        } catch (InvalidStudyDataException e) {
            session.setAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            session.setAttribute("errorMessage", "Error: " + e.getMessage());
        }

        String redirect = req.getParameter("redirectSubjectId");
        if (redirect != null && !redirect.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/topics?subjectId=" + redirect);
        } else {
            resp.sendRedirect(req.getContextPath() + "/topics");
        }
    }
}

