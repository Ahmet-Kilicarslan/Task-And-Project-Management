package com.ahmet.tpm.dao;

import com.ahmet.tpm.config.DatabaseConfig;
import com.ahmet.tpm.models.ProjectStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectStatusDao {

    // ==================== HELPER METHOD ====================
    private ProjectStatus extractProjectStatusFromResultSet(ResultSet rs) throws SQLException {
        return new ProjectStatus(
                rs.getInt("status_id"),
                rs.getString("status_name")
        );
    }

    // ==================== CREATE (Admin Only - usually pre-populated) ====================
    public void insert(ProjectStatus status) {
        String sql = """
            INSERT INTO ProjectStatus (status_name)
            VALUES (?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.getStatusName());
            stmt.executeUpdate();
            System.out.println("✓ Project status inserted successfully!");

        } catch (SQLException e) {
            System.err.println("✗ Error inserting project status: " + e.getMessage());
        }
    }

    // ==================== READ ====================
    public ProjectStatus findById(int id) {
        String sql = """
            SELECT status_id, status_name
            FROM ProjectStatus
            WHERE status_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractProjectStatusFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding project status: " + e.getMessage());
        }
        return null;
    }

    public List<ProjectStatus> findAll() {
        String sql = """
            SELECT status_id, status_name
            FROM ProjectStatus
            ORDER BY status_id ASC
            """;

        List<ProjectStatus> statuses = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                statuses.add(extractProjectStatusFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding all project statuses: " + e.getMessage());
        }

        return statuses;
    }

    public ProjectStatus findByName(String name) {
        String sql = """
            SELECT status_id, status_name
            FROM ProjectStatus
            WHERE status_name = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractProjectStatusFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding project status by name: " + e.getMessage());
        }
        return null;
    }

    // ==================== UPDATE (Admin Only) ====================
    public void update(ProjectStatus status) {
        String sql = """
            UPDATE ProjectStatus
            SET status_name = ?
            WHERE status_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.getStatusName());
            stmt.setInt(2, status.getStatusId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✓ Project status updated successfully!");
            } else {
                System.out.println("⚠ No project status found with ID: " + status.getStatusId());
            }

        } catch (SQLException e) {
            System.err.println("✗ Error updating project status: " + e.getMessage());
        }
    }

    // ==================== DELETE (Dangerous - Admin Only) ====================
    public void delete(int id) {
        String sql = """
            DELETE FROM ProjectStatus
            WHERE status_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("✓ Project status deleted successfully!");
            } else {
                System.out.println("⚠ No project status found with ID: " + id);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error deleting project status: " + e.getMessage());
            System.err.println("  Note: Cannot delete status if projects use it");
        }
    }

    // ==================== UTILITY METHODS ====================
    public int count() {
        String sql = "SELECT COUNT(*) FROM ProjectStatus";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting project statuses: " + e.getMessage());
        }
        return 0;
    }

    public boolean exists(int id) {
        String sql = "SELECT 1 FROM ProjectStatus WHERE status_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("✗ Error checking project status existence: " + e.getMessage());
        }
        return false;
    }
}