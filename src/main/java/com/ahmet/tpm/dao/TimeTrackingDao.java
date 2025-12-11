package com.ahmet.tpm.dao;

import com.ahmet.tpm.config.DatabaseConfig;
import com.ahmet.tpm.models.TimeTracking;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TimeTrackingDao {

    // ==================== HELPER METHOD ====================
    private TimeTracking extractTimeTrackingFromResultSet(ResultSet rs) throws SQLException {
        return new TimeTracking(
                rs.getInt("time_entry_id"),
                rs.getInt("task_id"),
                rs.getInt("user_id"),
                rs.getDate("work_date").toLocalDate(),
                rs.getDouble("hours_worked")
        );
    }

    // ==================== CREATE ====================
    /**
     * Log time worked on a task
     */
    public void insert(TimeTracking timeEntry) {
        String sql = """
            INSERT INTO TimeTracking (task_id, user_id, work_date, hours_worked)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, timeEntry.getTaskId());
            stmt.setInt(2, timeEntry.getUserId());
            stmt.setDate(3, Date.valueOf(timeEntry.getWorkDate()));
            stmt.setDouble(4, timeEntry.getHoursWorked());
            stmt.executeUpdate();
            System.out.println(" Time entry logged successfully!");

        } catch (SQLException e) {
            System.err.println(" Error logging time entry: " + e.getMessage());
        }
    }

    public Integer insertAndGetId(TimeTracking timeEntry) {
        String sql = """
            INSERT INTO TimeTracking (task_id, user_id, work_date, hours_worked)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, timeEntry.getTaskId());
            stmt.setInt(2, timeEntry.getUserId());
            stmt.setDate(3, Date.valueOf(timeEntry.getWorkDate()));
            stmt.setDouble(4, timeEntry.getHoursWorked());
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    Integer generatedId = generatedKeys.getInt(1);
                    timeEntry.setTimeEntryId(generatedId);
                    System.out.println(" Time entry logged with ID: " + generatedId);
                    return generatedId;
                }
            }

        } catch (SQLException e) {
            System.err.println(" Error logging time entry: " + e.getMessage());
        }
        return null;
    }

    /**
     * Log time (convenience method)
     */
    public void logTime(int taskId, int userId, LocalDate workDate, double hoursWorked) {
        insert(new TimeTracking(taskId, userId, workDate, hoursWorked));
    }

    // ==================== READ ====================
    /**
     * Find time entry by ID
     */
    public TimeTracking findById(int id) {
        String sql = """
            SELECT time_entry_id, task_id, user_id, work_date, hours_worked
            FROM TimeTracking
            WHERE time_entry_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractTimeTrackingFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println(" Error finding time entry: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all time entries for a task
     */
    public List<TimeTracking> findByTask(int taskId) {
        String sql = """
            SELECT time_entry_id, task_id, user_id, work_date, hours_worked
            FROM TimeTracking
            WHERE task_id = ?
            ORDER BY work_date DESC
            """;

        List<TimeTracking> entries = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                entries.add(extractTimeTrackingFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println(" Error finding time entries for task: " + e.getMessage());
        }

        return entries;
    }

    /**
     * Get all time entries by a user
     */
    public List<TimeTracking> findByUser(int userId) {
        String sql = """
            SELECT time_entry_id, task_id, user_id, work_date, hours_worked
            FROM TimeTracking
            WHERE user_id = ?
            ORDER BY work_date DESC
            """;

        List<TimeTracking> entries = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                entries.add(extractTimeTrackingFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println(" Error finding time entries for user: " + e.getMessage());
        }

        return entries;
    }

    /**
     * Get time entries for a user on a specific date
     */
    public List<TimeTracking> findByUserAndDate(int userId, LocalDate date) {
        String sql = """
            SELECT time_entry_id, task_id, user_id, work_date, hours_worked
            FROM TimeTracking
            WHERE user_id = ? AND work_date = ?
            ORDER BY time_entry_id ASC
            """;

        List<TimeTracking> entries = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                entries.add(extractTimeTrackingFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println(" Error finding time entries by user and date: " + e.getMessage());
        }

        return entries;
    }

    /**
     * Get time entries within a date range
     */
    public List<TimeTracking> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT time_entry_id, task_id, user_id, work_date, hours_worked
            FROM TimeTracking
            WHERE work_date BETWEEN ? AND ?
            ORDER BY work_date DESC, user_id ASC
            """;

        List<TimeTracking> entries = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                entries.add(extractTimeTrackingFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println(" Error finding time entries by date range: " + e.getMessage());
        }

        return entries;
    }

    /**
     * Get time entries for a user within a date range
     */
    public List<TimeTracking> findByUserAndDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT time_entry_id, task_id, user_id, work_date, hours_worked
            FROM TimeTracking
            WHERE user_id = ? AND work_date BETWEEN ? AND ?
            ORDER BY work_date DESC
            """;

        List<TimeTracking> entries = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                entries.add(extractTimeTrackingFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println(" Error finding time entries by user and date range: " + e.getMessage());
        }

        return entries;
    }

    // ==================== UPDATE ====================
    /**
     * Update a time entry
     */
    public void update(TimeTracking timeEntry) {
        String sql = """
            UPDATE TimeTracking
            SET task_id = ?,
                user_id = ?,
                work_date = ?,
                hours_worked = ?
            WHERE time_entry_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, timeEntry.getTaskId());
            stmt.setInt(2, timeEntry.getUserId());
            stmt.setDate(3, Date.valueOf(timeEntry.getWorkDate()));
            stmt.setDouble(4, timeEntry.getHoursWorked());
            stmt.setInt(5, timeEntry.getTimeEntryId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println(" Time entry updated successfully!");
            } else {
                System.out.println(" No time entry found with ID: " + timeEntry.getTimeEntryId());
            }

        } catch (SQLException e) {
            System.err.println(" Error updating time entry: " + e.getMessage());
        }
    }

    /**
     * Update only hours worked
     */
    public void updateHours(int timeEntryId, double newHours) {
        String sql = """
            UPDATE TimeTracking
            SET hours_worked = ?
            WHERE time_entry_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newHours);
            stmt.setInt(2, timeEntryId);

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println(" Hours updated successfully!");
            } else {
                System.out.println(" No time entry found with ID: " + timeEntryId);
            }

        } catch (SQLException e) {
            System.err.println(" Error updating hours: " + e.getMessage());
        }
    }

    // ==================== DELETE ====================
    /**
     * Delete a time entry
     */
    public void delete(int id) {
        String sql = """
            DELETE FROM TimeTracking
            WHERE time_entry_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println(" Time entry deleted successfully!");
            } else {
                System.out.println(" No time entry found with ID: " + id);
            }

        } catch (SQLException e) {
            System.err.println(" Error deleting time entry: " + e.getMessage());
        }
    }

    /**
     * Delete all time entries for a task
     */
    public void deleteAllEntriesForTask(int taskId) {
        String sql = """
            DELETE FROM TimeTracking
            WHERE task_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            int rowsDeleted = stmt.executeUpdate();
            System.out.println(" Deleted " + rowsDeleted + " time entries for task");

        } catch (SQLException e) {
            System.err.println(" Error deleting time entries for task: " + e.getMessage());
        }
    }

    /**
     * Delete all time entries by a user
     */
    public void deleteAllEntriesByUser(int userId) {
        String sql = """
            DELETE FROM TimeTracking
            WHERE user_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int rowsDeleted = stmt.executeUpdate();
            System.out.println(" Deleted " + rowsDeleted + " time entries by user");

        } catch (SQLException e) {
            System.err.println(" Error deleting time entries by user: " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODS (ANALYTICS) ====================
    /**
     * Get total hours worked on a task
     */
    public double getTotalHoursForTask(int taskId) {
        String sql = """
            SELECT SUM(hours_worked) as total_hours
            FROM TimeTracking
            WHERE task_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total_hours");
            }

        } catch (SQLException e) {
            System.err.println(" Error getting total hours for task: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get total hours worked by a user
     */
    public double getTotalHoursForUser(int userId) {
        String sql = """
            SELECT SUM(hours_worked) as total_hours
            FROM TimeTracking
            WHERE user_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total_hours");
            }

        } catch (SQLException e) {
            System.err.println(" Error getting total hours for user: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get total hours for a user on a task
     */
    public double getTotalHoursForUserOnTask(int userId, int taskId) {
        String sql = """
            SELECT SUM(hours_worked) as total_hours
            FROM TimeTracking
            WHERE user_id = ? AND task_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, taskId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total_hours");
            }

        } catch (SQLException e) {
            System.err.println(" Error getting total hours for user on task: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get total hours within a date range
     */
    public double getTotalHoursByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT SUM(hours_worked) as total_hours
            FROM TimeTracking
            WHERE work_date BETWEEN ? AND ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total_hours");
            }

        } catch (SQLException e) {
            System.err.println(" Error getting total hours by date range: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get total hours for a user within a date range
     */
    public double getTotalHoursForUserByDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT SUM(hours_worked) as total_hours
            FROM TimeTracking
            WHERE user_id = ? AND work_date BETWEEN ? AND ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total_hours");
            }

        } catch (SQLException e) {
            System.err.println(" Error getting total hours for user by date range: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Count time entries for a task
     */
    public int countEntriesForTask(int taskId) {
        String sql = "SELECT COUNT(*) FROM TimeTracking WHERE task_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println(" Error counting time entries for task: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Count time entries by a user
     */
    public int countEntriesForUser(int userId) {
        String sql = "SELECT COUNT(*) FROM TimeTracking WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println(" Error counting time entries for user: " + e.getMessage());
        }
        return 0;
    }
}