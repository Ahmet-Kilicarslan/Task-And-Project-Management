package com.ahmet.tpm.dao;

import com.ahmet.tpm.config.DatabaseConfig;
import com.ahmet.tpm.models.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRoleDao {
    // ==================== HELPER METHOD ====================
    private UserRole extractUserRoleFromResultSet(ResultSet rs) throws SQLException {
        return new UserRole(
                rs.getInt("user_role_id"),
                rs.getInt("user_id"),
                rs.getInt("role_id")
        );
    }

    // ==================== CREATE ====================
    /**
     * Assign a role to a user
     */
    public void insert(UserRole userRole) {
        String sql = """
            INSERT INTO UserRoles (user_id, role_id)
            VALUES (?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userRole.getUserId());
            stmt.setInt(2, userRole.getRoleId());
            stmt.executeUpdate();
            System.out.println("✓ Role assigned to user successfully!");

        } catch (SQLException e) {
            System.err.println("✗ Error assigning role to user: " + e.getMessage());
        }
    }

    /**
     * Assign a role to a user (convenience method)
     */
    public void assignRoleToUser(int userId, int roleId) {
        insert(new UserRole(userId, roleId));
    }

    // ==================== READ ====================
    /**
     * Get all roles for a specific user
     */
    public List<UserRole> findByUser(int userId) {
        String sql = """
            SELECT user_role_id, user_id, role_id
            FROM UserRoles
            WHERE user_id = ?
            """;

        List<UserRole> userRoles = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                userRoles.add(extractUserRoleFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding roles for user: " + e.getMessage());
        }

        return userRoles;
    }

    /**
     * Get all users with a specific role
     */
    public List<UserRole> findByRole(int roleId) {
        String sql = """
            SELECT user_role_id, user_id, role_id
            FROM UserRoles
            WHERE role_id = ?
            """;

        List<UserRole> userRoles = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roleId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                userRoles.add(extractUserRoleFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding users with role: " + e.getMessage());
        }

        return userRoles;
    }

    /**
     * Get role IDs for a user (useful for permission checks)
     */
    public List<Integer> getRoleIdsForUser(int userId) {
        String sql = """
            SELECT role_id
            FROM UserRoles
            WHERE user_id = ?
            """;

        List<Integer> roleIds = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                roleIds.add(rs.getInt("role_id"));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error getting role IDs for user: " + e.getMessage());
        }

        return roleIds;
    }

    /**
     * Check if user has a specific role
     */
    public boolean userHasRole(int userId, int roleId) {
        String sql = """
            SELECT 1
            FROM UserRoles
            WHERE user_id = ? AND role_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, roleId);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("✗ Error checking if user has role: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if user has role by name (convenience method)
     */
    public boolean userHasRole(int userId, String roleName) {
        String sql = """
            SELECT 1
            FROM UserRoles ur
            JOIN Roles r ON ur.role_id = r.role_id
            WHERE ur.user_id = ? AND r.role_name = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, roleName);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("✗ Error checking if user has role by name: " + e.getMessage());
        }
        return false;
    }

    // ==================== DELETE ====================
    /**
     * Remove a specific role from a user
     */
    public void delete(int userId, int roleId) {
        String sql = """
            DELETE FROM UserRoles
            WHERE user_id = ? AND role_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, roleId);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("✓ Role removed from user successfully!");
            } else {
                System.out.println("⚠ User-Role assignment not found");
            }

        } catch (SQLException e) {
            System.err.println("✗ Error removing role from user: " + e.getMessage());
        }
    }

    /**
     * Remove all roles from a user
     */
    public void deleteAllRolesForUser(int userId) {
        String sql = """
            DELETE FROM UserRoles
            WHERE user_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int rowsDeleted = stmt.executeUpdate();
            System.out.println("✓ Removed " + rowsDeleted + " roles from user");

        } catch (SQLException e) {
            System.err.println("✗ Error removing all roles from user: " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================
    /**
     * Count roles for a user
     */
    public int countRolesForUser(int userId) {
        String sql = "SELECT COUNT(*) FROM UserRoles WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting roles for user: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Count users with a specific role
     */
    public int countUsersWithRole(int roleId) {
        String sql = "SELECT COUNT(*) FROM UserRoles WHERE role_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting users with role: " + e.getMessage());
        }
        return 0;
    }


}
