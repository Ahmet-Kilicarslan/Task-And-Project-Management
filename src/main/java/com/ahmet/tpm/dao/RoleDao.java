package com.ahmet.tpm.dao;
import com.ahmet.tpm.config.DatabaseConfig;
import com.ahmet.tpm.models.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class RoleDao {

    // ==================== HELPER METHOD ====================
    private Role extractRoleFromResultSet(ResultSet rs) throws SQLException {
        return new Role(
                rs.getInt("role_id"),
                rs.getString("role_name")
        );
    }

    // ==================== CREATE (Admin Only) ====================
    public void insert(Role role) {
        String sql = """
            INSERT INTO Roles (role_name)
            VALUES (?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role.getRoleName());
            stmt.executeUpdate();
            System.out.println("✓ Role inserted successfully!");

        } catch (SQLException e) {
            System.err.println("✗ Error inserting role: " + e.getMessage());
        }
    }

    // ==================== READ ====================
    public Role findById(int id) {
        String sql = """
            SELECT role_id, role_name
            FROM Roles
            WHERE role_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractRoleFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding role: " + e.getMessage());
        }
        return null;
    }

    public List<Role> findAll() {
        String sql = """
            SELECT role_id, role_name
            FROM Roles
            ORDER BY role_name ASC
            """;

        List<Role> roles = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                roles.add(extractRoleFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding all roles: " + e.getMessage());
        }

        return roles;
    }

    public Role findByName(String name) {
        String sql = """
            SELECT role_id, role_name
            FROM Roles
            WHERE role_name = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractRoleFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding role by name: " + e.getMessage());
        }
        return null;
    }

    // ==================== UPDATE (Admin Only) ====================
    public void update(Role role) {
        String sql = """
            UPDATE Roles
            SET role_name = ?
            WHERE role_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role.getRoleName());
            stmt.setInt(2, role.getRoleId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✓ Role updated successfully!");
            } else {
                System.out.println("⚠ No role found with ID: " + role.getRoleId());
            }

        } catch (SQLException e) {
            System.err.println("✗ Error updating role: " + e.getMessage());
        }
    }

    // ==================== DELETE (Dangerous - Admin Only) ====================
    public void delete(int id) {
        String sql = """
            DELETE FROM Roles
            WHERE role_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("✓ Role deleted successfully!");
            } else {
                System.out.println("⚠ No role found with ID: " + id);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error deleting role: " + e.getMessage());
            System.err.println("  Note: Cannot delete role if users are assigned to it");
        }
    }

    // ==================== UTILITY METHODS ====================
    public int count() {
        String sql = "SELECT COUNT(*) FROM Roles";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting roles: " + e.getMessage());
        }
        return 0;
    }

    public boolean exists(int id) {
        String sql = "SELECT 1 FROM Roles WHERE role_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("✗ Error checking role existence: " + e.getMessage());
        }
        return false;
    }

}
