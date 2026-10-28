package com.smartstudyplanner.dao;

import com.smartstudyplanner.exception.DatabaseException;
import com.smartstudyplanner.model.Subject;
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
 * Data Access Object for Subject entities.
 */
public class SubjectDAO implements GenericDAO<Subject, Long> {

    @Override
    public Subject save(Subject subject) {
        String sql = "INSERT INTO subjects (user_id, name, code, exam_date, color_hex, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, subject.getUserId());
            ps.setString(2, subject.getName());
            ps.setString(3, subject.getCode());
            ps.setDate(4, Date.valueOf(subject.getExamDate()));
            ps.setString(5, subject.getColorHex());
            ps.setString(6, subject.getDescription());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        subject.setId(rs.getLong(1));
                    }
                }
            }
            return subject;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save subject: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Subject> findById(Long id) {
        String sql = "SELECT * FROM subjects WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSubject(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find subject by id: " + e.getMessage(), e);
        }
    }

    public List<Subject> findByUserId(Long userId) {
        String sql = "SELECT * FROM subjects WHERE user_id = ? ORDER BY exam_date ASC, name ASC";
        List<Subject> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSubject(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find subjects by user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Subject> findAll() {
        String sql = "SELECT * FROM subjects ORDER BY exam_date ASC";
        List<Subject> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToSubject(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve all subjects: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Subject subject) {
        String sql = "UPDATE subjects SET name = ?, code = ?, exam_date = ?, color_hex = ?, description = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, subject.getName());
            ps.setString(2, subject.getCode());
            ps.setDate(3, Date.valueOf(subject.getExamDate()));
            ps.setString(4, subject.getColorHex());
            ps.setString(5, subject.getDescription());
            ps.setLong(6, subject.getId());
            ps.setLong(7, subject.getUserId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update subject: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM subjects WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete subject: " + e.getMessage(), e);
        }
    }

    public boolean deleteByIdAndUserId(Long id, Long userId) {
        String sql = "DELETE FROM subjects WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete subject by id and user: " + e.getMessage(), e);
        }
    }

    private Subject mapResultSetToSubject(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setId(rs.getLong("id"));
        s.setUserId(rs.getLong("user_id"));
        s.setName(rs.getString("name"));
        s.setCode(rs.getString("code"));

        Date exam = rs.getDate("exam_date");
        if (exam != null) s.setExamDate(exam.toLocalDate());

        s.setColorHex(rs.getString("color_hex"));
        s.setDescription(rs.getString("description"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) s.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) s.setUpdatedAt(updated.toLocalDateTime());

        return s;
    }
}

