package com.smartstudyplanner.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application lifecycle listener ensuring database pool cleanup on shutdown.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        boolean production = "production".equalsIgnoreCase(System.getenv("APP_ENV"));
        sce.getServletContext().getSessionCookieConfig().setHttpOnly(true);
        sce.getServletContext().getSessionCookieConfig().setSecure(production);
        logger.info("AI Smart Study Planner web application initialized.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("AI Smart Study Planner shutting down. Releasing database connections...");
        DBConnection.closePool();
    }
}

