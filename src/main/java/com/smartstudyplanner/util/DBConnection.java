package com.smartstudyplanner.util;

import com.smartstudyplanner.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Properties;

/**
 * Database connection manager utilizing HikariCP high-performance connection pool.
 * Implements Singleton pattern and handles auto-configuration, classpath property loading,
 * and automatic database schema bootstrapping.
 */
public class DBConnection {

    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);
    private static volatile HikariDataSource dataSource;
    private static final Object lock = new Object();

    private DBConnection() {
        // Private constructor for singleton
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            synchronized (lock) {
                if (dataSource == null || dataSource.isClosed()) {
                    initDataSource();
                }
            }
        }
        return dataSource.getConnection();
    }

    private static void initDataSource() {
        HikariDataSource candidate = null;
        try {
            Properties props = new Properties();
            try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (is != null) {
                    props.load(is);
                }
            }

            String jdbcUrl = System.getenv("DB_URL");
            if (jdbcUrl == null || jdbcUrl.isBlank()) {
                jdbcUrl = props.getProperty("db.url", "jdbc:mysql://localhost:3306/smart_study_planner?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
            }

            String user = System.getenv("DB_USER");
            if (user == null || user.isBlank()) {
                user = props.getProperty("db.user", "root");
            }

            String password = System.getenv("DB_PASSWORD");
            if (password == null || password.isBlank()) {
                password = props.getProperty("db.password", "root");
            }

            String driver = System.getenv("DB_DRIVER");
            if (driver == null || driver.isBlank()) {
                driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            }

            boolean postgresDatabase = jdbcUrl.startsWith("jdbc:postgresql:")
                    || driver.startsWith("org.postgresql.");

            try {
                HikariConfig config = new HikariConfig();
                config.setDriverClassName(driver);
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(user);
                config.setPassword(password);

                config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "10")));
                config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimumIdle", "2")));
                config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "300000")));
                config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "4000")));
                config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "1200000")));
                config.setPoolName("SmartStudyPlannerPool");

                candidate = new HikariDataSource(config);
                try (Connection testConn = candidate.getConnection()) {
                    logger.info("Successfully connected to primary database at {}", jdbcUrl);
                }
            } catch (Exception primaryEx) {
                if (candidate != null && !candidate.isClosed()) {
                    candidate.close();
                }
                if (postgresDatabase) {
                    throw new DatabaseException(
                            "Could not connect to PostgreSQL. Verify DB_URL, DB_USER, and DB_PASSWORD.", primaryEx);
                }
                logger.warn("Primary database unavailable ({}: {}). Falling back to embedded MySQL-compatible database...",
                        primaryEx.getClass().getSimpleName(), primaryEx.getMessage());

                String userHome = System.getProperty("user.home", ".").replace("\\", "/");
                String fallbackUrl = "jdbc:h2:file:" + userHome + "/smart_study_planner_db;AUTO_SERVER=TRUE;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1";

                HikariConfig fallbackConfig = new HikariConfig();
                fallbackConfig.setDriverClassName("org.h2.Driver");
                fallbackConfig.setJdbcUrl(fallbackUrl);
                fallbackConfig.setUsername("sa");
                fallbackConfig.setPassword("");
                fallbackConfig.setMaximumPoolSize(5);
                fallbackConfig.setPoolName("SmartStudyPlannerFallbackPool");

                candidate = new HikariDataSource(fallbackConfig);
                try (Connection testConn = candidate.getConnection()) {
                    logger.info("Embedded database connected successfully at {}", fallbackUrl);
                }
            }

            bootstrapSchema(candidate);
            seedIfEmpty(candidate);
            dataSource = candidate;
        } catch (Exception e) {
            if (candidate != null && !candidate.isClosed()) {
                candidate.close();
            }
            dataSource = null;
            logger.error("Failed to initialize database connection pool: {}", e.getMessage(), e);
            throw new DatabaseException("Could not initialize database connection pool: " + e.getMessage(), e);
        }
    }

    private static void seedIfEmpty(HikariDataSource source) {
        try (Connection conn = source.getConnection();
             Statement stmt = conn.createStatement()) {
            if (conn.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("postgresql")) {
                logger.info("Skipping automatic sample-data seeding for PostgreSQL.");
                return;
            }

            boolean hasUsers = false;
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasUsers = true;
                }
            } catch (SQLException ignored) {
                // Table might not exist yet
            }

            if (!hasUsers) {
                logger.info("Empty database detected. Applying seed.sql with demo account...");
                try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("seed.sql")) {
                    if (is != null) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                            StringBuilder sqlBuilder = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                line = line.trim();
                                if (line.isEmpty() || line.startsWith("--") || line.startsWith("//")) {
                                    continue;
                                }
                                sqlBuilder.append(" ").append(line);
                                if (line.endsWith(";")) {
                                    String query = sqlBuilder.toString().replace(";", "").trim();
                                    if (!query.isEmpty()) {
                                        try {
                                            stmt.execute(query);
                                        } catch (SQLException ex) {
                                            logger.debug("Seed query warning: {}", ex.getMessage());
                                        }
                                    }
                                    sqlBuilder.setLength(0);
                                }
                            }
                        }
                        logger.info("Demo seed data loaded successfully.");
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not seed initial data: {}", e.getMessage());
        }
    }

    /**
     * Executes schema.sql if tables do not exist.
     */
    public static void bootstrapSchema() {
        HikariDataSource source = dataSource;
        if (source == null || source.isClosed()) {
            throw new DatabaseException("Database connection pool is not initialized.");
        }
        bootstrapSchema(source);
    }

    private static void bootstrapSchema(HikariDataSource source) {
        try (Connection conn = source.getConnection();
             InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("schema.sql")) {

            if (is == null) {
                throw new IllegalStateException("schema.sql was not found on the application classpath.");
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                 Statement stmt = conn.createStatement()) {

                StringBuilder sqlBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("--") || line.startsWith("//")) {
                        continue;
                    }
                    sqlBuilder.append(" ").append(line);
                    if (line.endsWith(";")) {
                        String query = sqlBuilder.toString().replace(";", "").trim();
                        String normalizedQuery = query.toUpperCase(Locale.ROOT);
                        if (!query.isEmpty() && !normalizedQuery.startsWith("USE ")
                                && !normalizedQuery.startsWith("CREATE DATABASE")) {
                            stmt.execute(query);
                        }
                        sqlBuilder.setLength(0);
                    }
                }
                if (!sqlBuilder.toString().isBlank()) {
                    throw new IllegalStateException("schema.sql ends with an incomplete SQL statement.");
                }
            }
            logger.info("Database schema is ready.");
        } catch (Exception e) {
            throw new DatabaseException("Could not initialize the database schema: " + e.getMessage(), e);
        }
    }

    public static boolean isDatabaseAvailable() {
        try (Connection conn = getConnection()) {
            return conn.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }

    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP database pool closed.");
        }
    }

    /**
     * Helper to set test DataSource (e.g. H2 in-memory).
     */
    public static void setDataSource(HikariDataSource customDataSource) {
        synchronized (lock) {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
            dataSource = customDataSource;
        }
    }
}
