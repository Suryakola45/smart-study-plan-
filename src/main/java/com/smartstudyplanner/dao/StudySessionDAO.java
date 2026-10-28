package com.smartstudyplanner.dao;

import com.smartstudyplanner.exception.DatabaseException;
import com.smartstudyplanner.model.SessionStatus;
import com.smartstudyplanner.model.StudySession;
import com.smartstudyplanner.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object for StudySession entities.
 * Implements transaction management, batch inserts, and historical progress aggregation.
 */
public class StudySessionDAO implements GenericDAO<StudySession, Long> {

    @Override
    public StudySession save(StudySession session) {
        String sql = "INSERT INTO study_sessions (user_id, topic_id, session_date, start_time, end_time, " +
                "duration_minutes, actual_minutes_studied, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindSessionParameters(ps, session);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        session.setId(rs.getLong(1));
                    }
                }
            }
            return session;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save study session: " + e.getMessage(), e);
        }
    }

    /**
     * Batch persists a list of study sessions inside a single ACID database transaction.
     */
    public boolean saveAll(List<StudySession> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return true;
        }

        String sql = "INSERT INTO study_sessions (user_id, topic_id, session_date, start_time, end_time, " +
                "duration_minutes, actual_minutes_studied, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (StudySession s : sessions) {
                    bindSessionParameters(ps, s);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit(); // Commit transaction
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on failure
                } catch (SQLException ex) {
                    // ignore rollback exception
                }
            }
            throw new DatabaseException("Failed to batch save study sessions: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    @Override
    public Optional<StudySession> findById(Long id) {
        String sql = "SELECT s.*, t.title AS topic_title, sub.name AS subject_name, sub.code AS subject_code, " +
                     "sub.color_hex, sub.exam_date " +
                     "FROM study_sessions s " +
                     "JOIN topics t ON s.topic_id = t.id " +
                     "JOIN subjects sub ON t.subject_id = sub.id " +
                     "WHERE s.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStudySession(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find study session by id: " + e.getMessage(), e);
        }
    }

    public List<StudySession> findByUserId(Long userId) {
        String sql = "SELECT s.*, t.title AS topic_title, sub.name AS subject_name, sub.code AS subject_code, " +
                     "sub.color_hex, sub.exam_date " +
                     "FROM study_sessions s " +
                     "JOIN topics t ON s.topic_id = t.id " +
                     "JOIN subjects sub ON t.subject_id = sub.id " +
                     "WHERE s.user_id = ? " +
                     "ORDER BY s.session_date ASC, s.start_time ASC";
        List<StudySession> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStudySession(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find sessions by user: " + e.getMessage(), e);
        }
    }

    public List<StudySession> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT s.*, t.title AS topic_title, sub.name AS subject_name, sub.code AS subject_code, " +
                     "sub.color_hex, sub.exam_date " +
                     "FROM study_sessions s " +
                     "JOIN topics t ON s.topic_id = t.id " +
                     "JOIN subjects sub ON t.subject_id = sub.id " +
                     "WHERE s.user_id = ? AND s.session_date >= ? AND s.session_date <= ? " +
                     "ORDER BY s.session_date ASC, s.start_time ASC";
        List<StudySession> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStudySession(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find sessions in date range: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes only future SCHEDULED sessions starting from a specific date.
     * Preserves all COMPLETED, SKIPPED, and PARTIALLY_COMPLETED historical records!
     */
    public int deleteFutureScheduledSessions(Long userId, LocalDate fromDate) {
        String sql = "DELETE FROM study_sessions WHERE user_id = ? AND session_date >= ? AND status = 'SCHEDULED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(fromDate));
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete future scheduled sessions: " + e.getMessage(), e);
        }
    }

    @Override
    public List<StudySession> findAll() {
        String sql = "SELECT s.*, t.title AS topic_title, sub.name AS subject_name, sub.code AS subject_code, " +
                     "sub.color_hex, sub.exam_date " +
                     "FROM study_sessions s " +
                     "JOIN topics t ON s.topic_id = t.id " +
                     "JOIN subjects sub ON t.subject_id = sub.id " +
                     "ORDER BY s.session_date DESC";
        List<StudySession> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToStudySession(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve all sessions: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(StudySession session) {
        String sql = "UPDATE study_sessions SET topic_id = ?, session_date = ?, start_time = ?, end_time = ?, " +
                "duration_minutes = ?, actual_minutes_studied = ?, status = ?, notes = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, session.getTopicId());
            ps.setDate(2, Date.valueOf(session.getSessionDate()));
            ps.setTime(3, Time.valueOf(session.getStartTime()));
            ps.setTime(4, Time.valueOf(session.getEndTime()));
            ps.setInt(5, session.getDurationMinutes());
            ps.setInt(6, session.getActualMinutesStudied());
            ps.setString(7, session.getStatus());
            ps.setString(8, session.getNotes());
            ps.setLong(9, session.getId());
            ps.setLong(10, session.getUserId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update study session: " + e.getMessage(), e);
        }
    }

    public boolean updateStatusAndActualTime(Long sessionId, String status, int actualMinutes, String notes) {
        String sql = "UPDATE study_sessions SET status = ?, actual_minutes_studied = ?, notes = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, actualMinutes);
            ps.setString(3, notes);
            ps.setLong(4, sessionId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update session status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM study_sessions WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete session: " + e.getMessage(), e);
        }
    }

    /**
     * Aggregates daily study minutes for the last 7 days for progress charts.
     */
    public Map<LocalDate, Integer> getWeeklyStudyMinutes(Long userId, LocalDate start, LocalDate end) {
        String sql = "SELECT session_date, SUM(actual_minutes_studied) AS total_minutes " +
                     "FROM study_sessions " +
                     "WHERE user_id = ? AND session_date >= ? AND session_date <= ? AND status IN ('COMPLETED', 'PARTIALLY_COMPLETED') " +
                     "GROUP BY session_date ORDER BY session_date ASC";
        Map<LocalDate, Integer> map = new HashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getDate("session_date").toLocalDate(), rs.getInt("total_minutes"));
                }
            }
            return map;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to calculate weekly study minutes: " + e.getMessage(), e);
        }
    }

    private void bindSessionParameters(PreparedStatement ps, StudySession session) throws SQLException {
        ps.setLong(1, session.getUserId());
        ps.setLong(2, session.getTopicId());
        ps.setDate(3, Date.valueOf(session.getSessionDate()));
        ps.setTime(4, Time.valueOf(session.getStartTime()));
        ps.setTime(5, Time.valueOf(session.getEndTime()));
        ps.setInt(6, session.getDurationMinutes());
        ps.setInt(7, session.getActualMinutesStudied());
        ps.setString(8, session.getStatus());
        ps.setString(9, session.getNotes());
    }

    private StudySession mapResultSetToStudySession(ResultSet rs) throws SQLException {
        StudySession s = new StudySession();
        s.setId(rs.getLong("id"));
        s.setUserId(rs.getLong("user_id"));
        s.setTopicId(rs.getLong("topic_id"));

        Date date = rs.getDate("session_date");
        if (date != null) s.setSessionDate(date.toLocalDate());

        Time start = rs.getTime("start_time");
        if (start != null) s.setStartTime(start.toLocalTime());

        Time end = rs.getTime("end_time");
        if (end != null) s.setEndTime(end.toLocalTime());

        s.setDurationMinutes(rs.getInt("duration_minutes"));
        s.setActualMinutesStudied(rs.getInt("actual_minutes_studied"));
        s.setStatus(rs.getString("status"));
        s.setNotes(rs.getString("notes"));

        // Joined fields
        try {
            s.setTopicTitle(rs.getString("topic_title"));
            s.setSubjectName(rs.getString("subject_name"));
            s.setSubjectCode(rs.getString("subject_code"));
            s.setSubjectColorHex(rs.getString("color_hex"));
            Date exam = rs.getDate("exam_date");
            if (exam != null) s.setExamDate(exam.toLocalDate());
        } catch (SQLException ignored) {
        }

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) s.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) s.setUpdatedAt(updated.toLocalDateTime());

        return s;
    }
}

