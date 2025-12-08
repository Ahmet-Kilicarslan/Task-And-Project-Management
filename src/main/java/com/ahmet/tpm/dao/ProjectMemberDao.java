package com.ahmet.tpm.dao;

import com.ahmet.tpm.config.DatabaseConfig;
import com.ahmet.tpm.models.ProjectMember;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectMemberDao {

    // ==================== HELPER METHOD ====================
    private ProjectMember extractProjectMemberFromResultSet(ResultSet rs) throws SQLException {
        return new ProjectMember(
                rs.getInt("project_member_id"),
                rs.getInt("project_id"),
                rs.getInt("user_id"),
                rs.getString("role_in_project")
        );
    }

    // ==================== CREATE ====================
    /**
     * Add a member to a project
     */
    public void insert(ProjectMember member) {
        String sql = """
            INSERT INTO ProjectMembers (project_id, user_id, role_in_project)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, member.getProjectId());
            stmt.setInt(2, member.getUserId());
            stmt.setString(3, member.getRoleInProject());
            stmt.executeUpdate();
            System.out.println("✓ Member added to project successfully!");

        } catch (SQLException e) {
            System.err.println("✗ Error adding member to project: " + e.getMessage());
        }
    }

    /**
     * Add member to project (convenience method)
     */
    public void addMemberToProject(int projectId, int userId, String roleInProject) {
        insert(new ProjectMember(projectId, userId, roleInProject));
    }

    // ==================== READ ====================
    /**
     * Get all members of a specific project
     */
    public List<ProjectMember> findByProject(int projectId) {
        String sql = """
            SELECT project_member_id, project_id, user_id, role_in_project
            FROM ProjectMembers
            WHERE project_id = ?
            ORDER BY role_in_project ASC
            """;

        List<ProjectMember> members = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                members.add(extractProjectMemberFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding members for project: " + e.getMessage());
        }

        return members;
    }

    /**
     * Get all projects a user is a member of
     */
    public List<ProjectMember> findByUser(int userId) {
        String sql = """
            SELECT project_member_id, project_id, user_id, role_in_project
            FROM ProjectMembers
            WHERE user_id = ?
            ORDER BY project_id ASC
            """;

        List<ProjectMember> memberships = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                memberships.add(extractProjectMemberFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding projects for user: " + e.getMessage());
        }

        return memberships;
    }

    /**
     * Get specific member record
     */
    public ProjectMember findByProjectAndUser(int projectId, int userId) {
        String sql = """
            SELECT project_member_id, project_id, user_id, role_in_project
            FROM ProjectMembers
            WHERE project_id = ? AND user_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractProjectMemberFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding project member: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get user IDs for a project (useful for quick lookups)
     */
    public List<Integer> getUserIdsForProject(int projectId) {
        String sql = """
            SELECT user_id
            FROM ProjectMembers
            WHERE project_id = ?
            """;

        List<Integer> userIds = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                userIds.add(rs.getInt("user_id"));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error getting user IDs for project: " + e.getMessage());
        }

        return userIds;
    }

    /**
     * Get project IDs for a user (useful for quick lookups)
     */
    public List<Integer> getProjectIdsForUser(int userId) {
        String sql = """
            SELECT project_id
            FROM ProjectMembers
            WHERE user_id = ?
            """;

        List<Integer> projectIds = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                projectIds.add(rs.getInt("project_id"));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error getting project IDs for user: " + e.getMessage());
        }

        return projectIds;
    }

    /**
     * Check if user is a member of project
     */
    public boolean isMemberOfProject(int userId, int projectId) {
        String sql = """
            SELECT 1
            FROM ProjectMembers
            WHERE user_id = ? AND project_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, projectId);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("✗ Error checking project membership: " + e.getMessage());
        }
        return false;
    }

    // ==================== UPDATE ====================
    /**
     * Update member's role in project
     */
    public void updateRole(int projectId, int userId, String newRole) {
        String sql = """
            UPDATE ProjectMembers
            SET role_in_project = ?
            WHERE project_id = ? AND user_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newRole);
            stmt.setInt(2, projectId);
            stmt.setInt(3, userId);

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✓ Member role updated successfully!");
            } else {
                System.out.println("⚠ Member not found in project");
            }

        } catch (SQLException e) {
            System.err.println("✗ Error updating member role: " + e.getMessage());
        }
    }

    // ==================== DELETE ====================
    /**
     * Remove a member from a project
     */
    public void delete(int projectId, int userId) {
        String sql = """
            DELETE FROM ProjectMembers
            WHERE project_id = ? AND user_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            stmt.setInt(2, userId);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("✓ Member removed from project successfully!");
            } else {
                System.out.println("⚠ Member not found in project");
            }

        } catch (SQLException e) {
            System.err.println("✗ Error removing member from project: " + e.getMessage());
        }
    }

    /**
     * Remove all members from a project (when deleting project)
     */
    public void deleteAllMembersFromProject(int projectId) {
        String sql = """
            DELETE FROM ProjectMembers
            WHERE project_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            int rowsDeleted = stmt.executeUpdate();
            System.out.println("✓ Removed " + rowsDeleted + " members from project");

        } catch (SQLException e) {
            System.err.println("✗ Error removing all members from project: " + e.getMessage());
        }
    }

    /**
     * Remove user from all projects
     */
    public void deleteUserFromAllProjects(int userId) {
        String sql = """
            DELETE FROM ProjectMembers
            WHERE user_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int rowsDeleted = stmt.executeUpdate();
            System.out.println("✓ Removed user from " + rowsDeleted + " projects");

        } catch (SQLException e) {
            System.err.println("✗ Error removing user from all projects: " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================
    /**
     * Count members in a project
     */
    public int countMembersInProject(int projectId) {
        String sql = "SELECT COUNT(*) FROM ProjectMembers WHERE project_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting project members: " + e.getMessage());
        }
        return 0;
    }
    public int countProjectsForUser(int userId) {
        String sql = "SELECT COUNT(*) FROM ProjectMembers WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting projects for user: " + e.getMessage());
        }
        return 0;
    }
    public int countTotalMembers() {
        String sql = "SELECT COUNT(DISTINCT user_id) FROM ProjectMembers";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("âœ— Error counting total members: " + e.getMessage());
        }
        return 0;
    }






}
