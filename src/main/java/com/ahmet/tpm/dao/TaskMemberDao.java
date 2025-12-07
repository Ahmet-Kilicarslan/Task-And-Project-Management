package com.ahmet.tpm.dao;

import com.ahmet.tpm.config.DatabaseConfig;
import com.ahmet.tpm.models.TaskMember;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskMemberDao{

    // ==================== HELPER METHOD ====================
    private TaskMember extractTaskMemberFromResultSet(ResultSet rs) throws SQLException {
        return new TaskMember(
                rs.getInt("task_member_id"),
                rs.getInt("task_id"),
                rs.getInt("user_id"),
                rs.getTimestamp("assigned_at").toLocalDateTime()
        );
    }

    // ==================== CREATE ====================
    /**
     * Assign a user to a task
     */
    public void insert(TaskMember member) {
        String sql = """
            INSERT INTO TaskMembers (task_id, user_id)
            VALUES (?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, member.getTaskId());
            stmt.setInt(2, member.getUserId());
            stmt.executeUpdate();
            System.out.println("✓ User assigned to task successfully!");

        } catch (SQLException e) {
            System.err.println("✗ Error assigning user to task: " + e.getMessage());
        }
    }

    /**
     * Assign user to task (convenience method)
     */
    public void assignUserToTask(int taskId, int userId) {
        insert(new TaskMember(taskId, userId));
    }

    // ==================== READ ====================
    /**
     * Get all users assigned to a specific task
     */
    public List<TaskMember> findByTask(int taskId) {
        String sql = """
            SELECT task_member_id, task_id, user_id, assigned_at
            FROM TaskMembers
            WHERE task_id = ?
            ORDER BY assigned_at ASC
            """;

        List<TaskMember> members = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                members.add(extractTaskMemberFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding members for task: " + e.getMessage());
        }

        return members;
    }

    /**
     * Get all tasks assigned to a specific user
     */
    public List<TaskMember> findByUser(int userId) {
        String sql = """
            SELECT task_member_id, task_id, user_id, assigned_at
            FROM TaskMembers
            WHERE user_id = ?
            ORDER BY assigned_at DESC
            """;

        List<TaskMember> assignments = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                assignments.add(extractTaskMemberFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding tasks for user: " + e.getMessage());
        }

        return assignments;
    }

    /**
     * Get specific assignment record
     */
    public TaskMember findByTaskAndUser(int taskId, int userId) {
        String sql = """
            SELECT task_member_id, task_id, user_id, assigned_at
            FROM TaskMembers
            WHERE task_id = ? AND user_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractTaskMemberFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding task assignment: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get user IDs assigned to a task (useful for quick lookups)
     */
    public List<Integer> getUserIdsForTask(int taskId) {
        String sql = """
            SELECT user_id
            FROM TaskMembers
            WHERE task_id = ?
            """;

        List<Integer> userIds = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                userIds.add(rs.getInt("user_id"));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error getting user IDs for task: " + e.getMessage());
        }

        return userIds;
    }

    /**
     * Get task IDs assigned to a user (useful for quick lookups)
     */
    public List<Integer> getTaskIdsForUser(int userId) {
        String sql = """
            SELECT task_id
            FROM TaskMembers
            WHERE user_id = ?
            """;

        List<Integer> taskIds = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                taskIds.add(rs.getInt("task_id"));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error getting task IDs for user: " + e.getMessage());
        }

        return taskIds;
    }

    /**
     * Check if user is assigned to task
     */
    public boolean isAssignedToTask(int userId, int taskId) {
        String sql = """
            SELECT 1
            FROM TaskMembers
            WHERE user_id = ? AND task_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, taskId);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("✗ Error checking task assignment: " + e.getMessage());
        }
        return false;
    }

    // ==================== DELETE ====================
    /**
     * Unassign a user from a task
     */
    public void delete(int taskId, int userId) {
        String sql = """
            DELETE FROM TaskMembers
            WHERE task_id = ? AND user_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            stmt.setInt(2, userId);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("✓ User unassigned from task successfully!");
            } else {
                System.out.println("⚠ Assignment not found");
            }

        } catch (SQLException e) {
            System.err.println("✗ Error unassigning user from task: " + e.getMessage());
        }
    }

    /**
     * Unassign all users from a task
     */
    public void deleteAllMembersFromTask(int taskId) {
        String sql = """
            DELETE FROM TaskMembers
            WHERE task_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            int rowsDeleted = stmt.executeUpdate();
            System.out.println("✓ Removed " + rowsDeleted + " users from task");

        } catch (SQLException e) {
            System.err.println("✗ Error removing all users from task: " + e.getMessage());
        }
    }

    /**
     * Unassign user from all tasks
     */
    public void deleteUserFromAllTasks(int userId) {
        String sql = """
            DELETE FROM TaskMembers
            WHERE user_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int rowsDeleted = stmt.executeUpdate();
            System.out.println("✓ Removed user from " + rowsDeleted + " tasks");

        } catch (SQLException e) {
            System.err.println("✗ Error removing user from all tasks: " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================
    /**
     * Count users assigned to a task
     */
    public int countMembersForTask(int taskId) {
        String sql = "SELECT COUNT(*) FROM TaskMembers WHERE task_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting task members: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Count tasks assigned to a user
     */
    public int countTasksForUser(int userId) {
        String sql = "SELECT COUNT(*) FROM TaskMembers WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting tasks for user: " + e.getMessage());
        }
        return 0;
    }
}
