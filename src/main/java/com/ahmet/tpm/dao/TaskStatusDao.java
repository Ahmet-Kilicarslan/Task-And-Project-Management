package com.ahmet.tpm.dao;

import com.ahmet.tpm.config.DatabaseConfig;
import com.ahmet.tpm.models.TaskStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskStatusDao {

    // ==================== HELPER METHOD ====================
    private TaskStatus extractTaskStatusFromResultSet(ResultSet rs) throws SQLException {
        return new TaskStatus(
                rs.getInt("status_id"),
                rs.getString("status_name")
        );
    }

    // ==================== CREATE (Admin Only - usually pre-populated) ====================
    public void insert(TaskStatus status) {
        String sql = """
            INSERT INTO TaskStatus (status_name)
            VALUES (?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.getStatusName());
            stmt.executeUpdate();
            System.out.println("✓ Task status inserted successfully!");

        } catch (SQLException e) {
            System.err.println("✗ Error inserting task status: " + e.getMessage());
        }
    }

    // ==================== READ ====================
    public TaskStatus findById(int id) {
        String sql = """
            SELECT status_id, status_name
            FROM TaskStatus
            WHERE status_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractTaskStatusFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding task status: " + e.getMessage());
        }
        return null;
    }

    public List<TaskStatus> findAll() {
        String sql = """
            SELECT status_id, status_name
            FROM TaskStatus
            ORDER BY status_id ASC
            """;

        List<TaskStatus> statuses = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                statuses.add(extractTaskStatusFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding all task statuses: " + e.getMessage());
        }

        return statuses;
    }

    public TaskStatus findByName(String name) {
        String sql = """
            SELECT status_id, status_name
            FROM TaskStatus
            WHERE status_name = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractTaskStatusFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding task status by name: " + e.getMessage());
        }
        return null;
    }

    // ==================== UPDATE (Admin Only) ====================
    public void update(TaskStatus status) {
        String sql = """
            UPDATE TaskStatus
            SET status_name = ?
            WHERE status_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.getStatusName());
            stmt.setInt(2, status.getStatusId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✓ Task status updated successfully!");
            } else {
                System.out.println("⚠ No task status found with ID: " + status.getStatusId());
            }

        } catch (SQLException e) {
            System.err.println("✗ Error updating task status: " + e.getMessage());
        }
    }

    // ==================== DELETE (Dangerous - Admin Only) ====================
    public void delete(int id) {
        String sql = """
            DELETE FROM TaskStatus
            WHERE status_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("✓ Task status deleted successfully!");
            } else {
                System.out.println("⚠ No task status found with ID: " + id);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error deleting task status: " + e.getMessage());
            System.err.println("  Note: Cannot delete status if tasks use it");
        }
    }

    // ==================== UTILITY METHODS ====================
    public int count() {
        String sql = "SELECT COUNT(*) FROM TaskStatus";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting task statuses: " + e.getMessage());
        }
        return 0;
    }

    public boolean exists(int id) {
        String sql = "SELECT 1 FROM TaskStatus WHERE status_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("✗ Error checking task status existence: " + e.getMessage());
        }
        return false;
    }
}
