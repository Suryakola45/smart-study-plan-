package com.smartstudyplanner.dao;

import com.smartstudyplanner.exception.DatabaseException;
import com.smartstudyplanner.model.Availability;
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
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for student Availability records.
 */
public class AvailabilityDAO implements GenericDAO<Availability, Long> {

    @Override
    public Availability save(Availability availability) {
        String sql = "INSERT INTO availability (user_id, available_date, start_time, end_time, " +
                     "slot_duration_minutes, break_duration_minutes) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, availability.getUserId());
            ps.setDate(2, Date.valueOf(availability.getAvailableDate()));
            ps.setTime(3, Time.valueOf(availability.getStartTime()));
            ps.setTime(4, Time.valueOf(availability.getEndTime()));
            ps.setInt(5, availability.getSlotDurationMinutes());
            ps.setInt(6, availability.getBreakDurationMinutes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        availability.setId(rs.getLong(1));
                    }
                }
            }
            return availability;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save availability: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Availability> findById(Long id) {
        String sql = "SELECT * FROM availability WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAvailability(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find availability by id: " + e.getMessage(), e);
        }
    }

    public List<Availability> findByUserId(Long userId) {
        String sql = "SELECT * FROM availability WHERE user_id = ? ORDER BY available_date ASC, start_time ASC";
        List<Availability> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAvailability(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find availabilities by user: " + e.getMessage(), e);
        }
    }

    public List<Availability> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM availability WHERE user_id = ? AND available_date >= ? AND available_date <= ? " +
                     "ORDER BY available_date ASC, start_time ASC";
        List<Availability> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAvailability(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find availabilities in date range: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Availability> findAll() {
        String sql = "SELECT * FROM availability ORDER BY available_date ASC";
        List<Availability> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToAvailability(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve all availabilities: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Availability availability) {
        String sql = "UPDATE availability SET available_date = ?, start_time = ?, end_time = ?, " +
                     "slot_duration_minutes = ?, break_duration_minutes = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(availability.getAvailableDate()));
            ps.setTime(2, Time.valueOf(availability.getStartTime()));
            ps.setTime(3, Time.valueOf(availability.getEndTime()));
            ps.setInt(4, availability.getSlotDurationMinutes());
            ps.setInt(5, availability.getBreakDurationMinutes());
            ps.setLong(6, availability.getId());
            ps.setLong(7, availability.getUserId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update availability: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM availability WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete availability: " + e.getMessage(), e);
        }
    }

    public boolean deleteByIdAndUserId(Long id, Long userId) {
        String sql = "DELETE FROM availability WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete availability by id and user: " + e.getMessage(), e);
        }
    }

    private Availability mapResultSetToAvailability(ResultSet rs) throws SQLException {
        Availability a = new Availability();
        a.setId(rs.getLong("id"));
        a.setUserId(rs.getLong("user_id"));

        Date date = rs.getDate("available_date");
        if (date != null) a.setAvailableDate(date.toLocalDate());

        Time start = rs.getTime("start_time");
        if (start != null) a.setStartTime(start.toLocalTime());

        Time end = rs.getTime("end_time");
        if (end != null) a.setEndTime(end.toLocalTime());

        a.setSlotDurationMinutes(rs.getInt("slot_duration_minutes"));
        a.setBreakDurationMinutes(rs.getInt("break_duration_minutes"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) a.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) a.setUpdatedAt(updated.toLocalDateTime());

        return a;
    }
}

