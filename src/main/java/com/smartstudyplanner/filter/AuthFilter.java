package com.smartstudyplanner.filter;

import com.smartstudyplanner.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Authentication filter enforcing session security across protected routes.
 * Redirects unauthenticated access attempts to the login page.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    private static final List<String> PUBLIC_PREFIXES = Arrays.asList(
            "/login",
            "/register",
            "/logout",
            "/health",
            "/assets/",
            "/css/",
            "/js/",
            "/favicon.ico"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Force UTF-8 character encoding
        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");

        String contextPath = req.getContextPath();
        String path = req.getRequestURI().substring(contextPath.length());

        // Allow public pages and static assets
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Check active session
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            // Unauthenticated: store target URL and redirect to login
            if (session != null && !path.equals("/") && !path.isEmpty()) {
                session.setAttribute("targetUri", path);
            }
            res.sendRedirect(contextPath + "/login");
            return;
        }

        // Authenticated: proceed down chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private boolean isPublicPath(String path) {
        if (path == null || path.isEmpty() || path.equals("/") || path.equals("/index.jsp")) {
            return true;
        }
        for (String prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}

