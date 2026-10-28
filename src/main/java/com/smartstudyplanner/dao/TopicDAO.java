package com.smartstudyplanner.dao;

import com.smartstudyplanner.exception.DatabaseException;
import com.smartstudyplanner.model.Topic;
import com.smartstudyplanner.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Topic entities.
 * Includes join queries to eagerly populate subject information for UI and priority calculations.
 */
public class TopicDAO implements GenericDAO<Topic, Long> {

    @Override
    public Topic save(Topic topic) {
        String sql = "INSERT INTO topics (subject_id, title, description, difficulty, confidence_level, " +
                "estimated_minutes, completed_minutes, status, calculated_priority) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, topic.getSubjectId());
            ps.setString(2, topic.getTitle());
            ps.setString(3, topic.getDescription());
            ps.setInt(4, topic.getDifficulty());
            ps.setInt(5, topic.getConfidenceLevel());
            ps.setInt(6, topic.getEstimatedMinutes());
            ps.setInt(7, topic.getCompletedMinutes());
            ps.setString(8, topic.getStatus());
            ps.setDouble(9, topic.getCalculatedPriority());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        topic.setId(rs.getLong(1));
                    }
                }
            }
            return topic;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save topic: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Topic> findById(Long id) {
        String sql = "SELECT t.*, s.name AS subject_name, s.code AS subject_code, s.exam_date, s.color_hex " +
                     "FROM topics t " +
                     "JOIN subjects s ON t.subject_id = s.id " +
                     "WHERE t.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTopic(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find topic by id: " + e.getMessage(), e);
        }
    }

    public List<Topic> findBySubjectId(Long subjectId) {
        String sql = "SELECT t.*, s.name AS subject_name, s.code AS subject_code, s.exam_date, s.color_hex " +
                     "FROM topics t " +
                     "JOIN subjects s ON t.subject_id = s.id " +
                     "WHERE t.subject_id = ? " +
                     "ORDER BY t.calculated_priority DESC, t.id ASC";
        List<Topic> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTopic(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find topics by subject: " + e.getMessage(), e);
        }
    }

    public List<Topic> findByUserId(Long userId) {
        String sql = "SELECT t.*, s.name AS subject_name, s.code AS subject_code, s.exam_date, s.color_hex " +
                     "FROM topics t " +
                     "JOIN subjects s ON t.subject_id = s.id " +
                     "WHERE s.user_id = ? " +
                     "ORDER BY t.calculated_priority DESC, s.exam_date ASC";
        List<Topic> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTopic(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find topics by user: " + e.getMessage(), e);
        }
    }

    public List<Topic> findPendingByUserId(Long userId) {
        String sql = "SELECT t.*, s.name AS subject_name, s.code AS subject_code, s.exam_date, s.color_hex " +
                     "FROM topics t " +
                     "JOIN subjects s ON t.subject_id = s.id " +
                     "WHERE s.user_id = ? AND t.status != 'COMPLETED' " +
                     "ORDER BY t.calculated_priority DESC, s.exam_date ASC";
        List<Topic> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTopic(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find pending topics by user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Topic> findAll() {
        String sql = "SELECT t.*, s.name AS subject_name, s.code AS subject_code, s.exam_date, s.color_hex " +
                     "FROM topics t " +
                     "JOIN subjects s ON t.subject_id = s.id " +
                     "ORDER BY t.id ASC";
        List<Topic> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToTopic(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve all topics: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Topic topic) {
        String sql = "UPDATE topics SET title = ?, description = ?, difficulty = ?, confidence_level = ?, " +
                "estimated_minutes = ?, completed_minutes = ?, status = ?, calculated_priority = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, topic.getTitle());
            ps.setString(2, topic.getDescription());
            ps.setInt(3, topic.getDifficulty());
            ps.setInt(4, topic.getConfidenceLevel());
            ps.setInt(5, topic.getEstimatedMinutes());
            ps.setInt(6, topic.getCompletedMinutes());
            ps.setString(7, topic.getStatus());
            ps.setDouble(8, topic.getCalculatedPriority());
            ps.setLong(9, topic.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update topic: " + e.getMessage(), e);
        }
    }

    public boolean updateProgress(Long topicId, int additionalMinutes) {
        String fetchSql = "SELECT estimated_minutes, completed_minutes FROM topics WHERE id = ?";
        String updateSql = "UPDATE topics SET completed_minutes = ?, status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement fetchPs = conn.prepareStatement(fetchSql)) {
                fetchPs.setLong(1, topicId);
                try (ResultSet rs = fetchPs.executeQuery()) {
                    if (rs.next()) {
                        int est = rs.getInt("estimated_minutes");
                        int comp = rs.getInt("completed_minutes") + additionalMinutes;
                        String status = comp >= est ? "COMPLETED" : (comp > 0 ? "IN_PROGRESS" : "PENDING");

                        try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                            updatePs.setInt(1, comp);
                            updatePs.setString(2, status);
                            updatePs.setLong(3, topicId);
                            updatePs.executeUpdate();
                        }
                    }
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update topic progress transactionally: " + e.getMessage(), e);
        }
    }

    public boolean updateCalculatedPriority(Long topicId, double priority) {
        String sql = "UPDATE topics SET calculated_priority = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, priority);
            ps.setLong(2, topicId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update topic priority: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM topics WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete topic: " + e.getMessage(), e);
        }
    }

    private Topic mapResultSetToTopic(ResultSet rs) throws SQLException {
        Topic t = new Topic();
        t.setId(rs.getLong("id"));
        t.setSubjectId(rs.getLong("subject_id"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setDifficulty(rs.getInt("difficulty"));
        t.setConfidenceLevel(rs.getInt("confidence_level"));
        t.setEstimatedMinutes(rs.getInt("estimated_minutes"));
        t.setCompletedMinutes(rs.getInt("completed_minutes"));
        t.setStatus(rs.getString("status"));
        t.setCalculatedPriority(rs.getDouble("calculated_priority"));

        // Joined subject attributes if present in result set
        try {
            t.setSubjectName(rs.getString("subject_name"));
            t.setSubjectCode(rs.getString("subject_code"));
            Date exam = rs.getDate("exam_date");
            if (exam != null) t.setExamDate(exam.toLocalDate());
            t.setSubjectColorHex(rs.getString("color_hex"));
        } catch (SQLException ignored) {
            // Joined columns not queried in simple queries
        }

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) t.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) t.setUpdatedAt(updated.toLocalDateTime());

        return t;
    }
}

