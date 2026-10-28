package com.smartstudyplanner.servlet;

import com.smartstudyplanner.exception.InvalidStudyDataException;
import com.smartstudyplanner.exception.DatabaseException;
import com.smartstudyplanner.filter.CsrfFilter;
import com.smartstudyplanner.model.User;
import com.smartstudyplanner.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller servlet handling user registration, login, and session invalidation.
 */
@WebServlet(urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AuthServlet.class);
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();

        if ("/logout".equals(path)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        if ("/register".equals(path)) {
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
        } else {
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();

        if ("/register".equals(path)) {
            handleRegister(req, resp);
        } else if ("/login".equals(path)) {
            handleLogin(req, resp);
        } else if ("/logout".equals(path)) {
            handleLogout(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/login");
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String identifier = req.getParameter("identifier");
        String password = req.getParameter("password");

        Optional<User> userOpt;
        try {
            userOpt = authService.authenticate(identifier, password);
        } catch (DatabaseException e) {
            logger.error("Login is unavailable because the database request failed.", e);
            req.setAttribute("errorMessage", "Sign-in is temporarily unavailable. Please try again shortly.");
            req.setAttribute("identifier", identifier);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            HttpSession session = req.getSession(true);
            req.changeSessionId();
            CsrfFilter.regenerateToken(session);
            session.setAttribute("currentUser", user);

            String targetUri = (String) session.getAttribute("targetUri");
            session.removeAttribute("targetUri");

            if (targetUri != null && !targetUri.isBlank()) {
                resp.sendRedirect(req.getContextPath() + targetUri);
            } else {
                resp.sendRedirect(req.getContextPath() + "/dashboard");
            }
        } else {
            req.setAttribute("errorMessage", "Invalid username/email or password.");
            req.setAttribute("identifier", identifier);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String fullName = req.getParameter("fullName");

        try {
            User user = authService.register(username, email, password, fullName);
            HttpSession session = req.getSession(true);
            req.changeSessionId();
            CsrfFilter.regenerateToken(session);
            session.setAttribute("currentUser", user);
            session.setAttribute("successMessage", "Registration successful! Welcome to AI Smart Study Planner.");
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } catch (InvalidStudyDataException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("username", username);
            req.setAttribute("email", email);
            req.setAttribute("fullName", fullName);
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
        } catch (DatabaseException e) {
            logger.error("Registration is unavailable because the database request failed.", e);
            req.setAttribute("errorMessage", "Registration is temporarily unavailable. Please try again shortly.");
            req.setAttribute("username", username);
            req.setAttribute("email", email);
            req.setAttribute("fullName", fullName);
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/login?loggedOut=true");
    }
}

