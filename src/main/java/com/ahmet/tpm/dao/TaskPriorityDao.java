package com.ahmet.tpm.dao;

import com.ahmet.tpm.config.DatabaseConfig;
import com.ahmet.tpm.models.TaskPriority;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskPriorityDao {

    // ==================== HELPER METHOD ====================
    private TaskPriority extractTaskPriorityFromResultSet(ResultSet rs) throws SQLException {
        return new TaskPriority(
                rs.getInt("priority_id"),
                rs.getString("priority_name")
        );
    }

    // ==================== CREATE (Admin Only - usually pre-populated) ====================
    public void insert(TaskPriority priority) {
        String sql = """
            INSERT INTO TaskPriority (priority_name)
            VALUES (?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, priority.getPriorityName());
            stmt.executeUpdate();
            System.out.println("✓ Task priority inserted successfully!");

        } catch (SQLException e) {
            System.err.println("✗ Error inserting task priority: " + e.getMessage());
        }
    }

    // ==================== READ ====================
    public TaskPriority findById(int id) {
        String sql = """
            SELECT priority_id, priority_name
            FROM TaskPriority
            WHERE priority_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractTaskPriorityFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding task priority: " + e.getMessage());
        }
        return null;
    }

    public List<TaskPriority> findAll() {
        String sql = """
            SELECT priority_id, priority_name
            FROM TaskPriority
            ORDER BY priority_id ASC
            """;

        List<TaskPriority> priorities = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                priorities.add(extractTaskPriorityFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding all task priorities: " + e.getMessage());
        }

        return priorities;
    }

    public TaskPriority findByName(String name) {
        String sql = """
            SELECT priority_id, priority_name
            FROM TaskPriority
            WHERE priority_name = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractTaskPriorityFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding task priority by name: " + e.getMessage());
        }
        return null;
    }

    // ==================== UPDATE (Admin Only) ====================
    public void update(TaskPriority priority) {
        String sql = """
            UPDATE TaskPriority
            SET priority_name = ?
            WHERE priority_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, priority.getPriorityName());
            stmt.setInt(2, priority.getPriorityId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✓ Task priority updated successfully!");
            } else {
                System.out.println("⚠ No task priority found with ID: " + priority.getPriorityId());
            }

        } catch (SQLException e) {
            System.err.println("✗ Error updating task priority: " + e.getMessage());
        }
    }

    // ==================== DELETE (Dangerous - Admin Only) ====================
    public void delete(int id) {
        String sql = """
            DELETE FROM TaskPriority
            WHERE priority_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("✓ Task priority deleted successfully!");
            } else {
                System.out.println("⚠ No task priority found with ID: " + id);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error deleting task priority: " + e.getMessage());
            System.err.println("  Note: Cannot delete priority if tasks use it");
        }
    }

    // ==================== UTILITY METHODS ====================
    public int count() {
        String sql = "SELECT COUNT(*) FROM TaskPriority";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting task priorities: " + e.getMessage());
        }
        return 0;
    }

    public boolean exists(int id) {
        String sql = "SELECT 1 FROM TaskPriority WHERE priority_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("✗ Error checking task priority existence: " + e.getMessage());
        }
        return false;
    }
}
