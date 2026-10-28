package com.smartstudyplanner.servlet;

import com.smartstudyplanner.util.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Readiness endpoint for deployment platform health checks. */
@WebServlet("/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (DBConnection.isDatabaseAvailable()) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().write("ok");
        } else {
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Database unavailable");
        }
    }
}
