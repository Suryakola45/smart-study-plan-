package com.smartstudyplanner.filter;

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
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Filter providing CSRF (Cross-Site Request Forgery) protection for state-modifying requests.
 */
@WebFilter("/*")
public class CsrfFilter implements Filter {

    public static final String CSRF_SESSION_ATTR = "CSRF_TOKEN";
    public static final String CSRF_PARAM_NAME = "csrfToken";
    private static final SecureRandom secureRandom = new SecureRandom();

    public static void regenerateToken(HttpSession session) {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        session.setAttribute(CSRF_SESSION_ATTR, Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(true);
        String sessionToken = (String) session.getAttribute(CSRF_SESSION_ATTR);

        if (sessionToken == null) {
            regenerateToken(session);
            sessionToken = (String) session.getAttribute(CSRF_SESSION_ATTR);
        }

        // Expose token to request attributes for JSPs
        req.setAttribute(CSRF_PARAM_NAME, sessionToken);

        // Validate state-changing methods (POST, PUT, DELETE)
        String method = req.getMethod();
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            String requestToken = req.getParameter(CSRF_PARAM_NAME);
            if (requestToken == null || !sessionToken.equals(requestToken)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}

