package com.ahmet.tpm.dao;

import com.ahmet.tpm.config.DatabaseConfig;
import com.ahmet.tpm.models.TaskComment;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskCommentDao {

    // ==================== HELPER METHOD ====================
    private TaskComment extractTaskCommentFromResultSet(ResultSet rs) throws SQLException {
        return new TaskComment(
                rs.getInt("comment_id"),
                rs.getInt("task_id"),
                rs.getInt("user_id"),
                rs.getString("comment_text"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    // ==================== CREATE ====================
    /**
     * Add a comment to a task
     */
    public void insert(TaskComment comment) {
        String sql = """
            INSERT INTO TaskComments (task_id, user_id, comment_text)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, comment.getTaskId());
            stmt.setInt(2, comment.getUserId());
            stmt.setString(3, comment.getCommentText());
            stmt.executeUpdate();
            System.out.println("✓ Comment added successfully!");

        } catch (SQLException e) {
            System.err.println("✗ Error adding comment: " + e.getMessage());
        }
    }

    public Integer insertAndGetId(TaskComment comment) {
        String sql = """
            INSERT INTO TaskComments (task_id, user_id, comment_text)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, comment.getTaskId());
            stmt.setInt(2, comment.getUserId());
            stmt.setString(3, comment.getCommentText());
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    Integer generatedId = generatedKeys.getInt(1);
                    comment.setCommentId(generatedId);
                    System.out.println("✓ Comment added with ID: " + generatedId);
                    return generatedId;
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ Error adding comment: " + e.getMessage());
        }
        return null;
    }

    /**
     * Add comment (convenience method)
     */
    public void addComment(int taskId, int userId, String commentText) {
        insert(new TaskComment(taskId, userId, commentText));
    }

    // ==================== READ ====================
    /**
     * Find comment by ID
     */
    public TaskComment findById(int id) {
        String sql = """
            SELECT comment_id, task_id, user_id, comment_text, created_at
            FROM TaskComments
            WHERE comment_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractTaskCommentFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding comment: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all comments for a specific task (newest first)
     */
    public List<TaskComment> findByTask(int taskId) {
        String sql = """
            SELECT comment_id, task_id, user_id, comment_text, created_at
            FROM TaskComments
            WHERE task_id = ?
            ORDER BY created_at DESC
            """;

        List<TaskComment> comments = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                comments.add(extractTaskCommentFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding comments for task: " + e.getMessage());
        }

        return comments;
    }

    /**
     * Get all comments by a specific user
     */
    public List<TaskComment> findByUser(int userId) {
        String sql = """
            SELECT comment_id, task_id, user_id, comment_text, created_at
            FROM TaskComments
            WHERE user_id = ?
            ORDER BY created_at DESC
            """;

        List<TaskComment> comments = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                comments.add(extractTaskCommentFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding comments by user: " + e.getMessage());
        }

        return comments;
    }

    /**
     * Get recent comments for a task (limit results)
     */
    public List<TaskComment> findRecentByTask(int taskId, int limit) {
        String sql = """
            SELECT TOP (?) comment_id, task_id, user_id, comment_text, created_at
            FROM TaskComments
            WHERE task_id = ?
            ORDER BY created_at DESC
            """;

        List<TaskComment> comments = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, taskId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                comments.add(extractTaskCommentFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding recent comments: " + e.getMessage());
        }

        return comments;
    }

    /**
     * Search comments by text
     */
    public List<TaskComment> searchByText(String keyword) {
        String sql = """
            SELECT comment_id, task_id, user_id, comment_text, created_at
            FROM TaskComments
            WHERE comment_text LIKE ?
            ORDER BY created_at DESC
            """;

        List<TaskComment> comments = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                comments.add(extractTaskCommentFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error searching comments: " + e.getMessage());
        }

        return comments;
    }

    /**
     * Get comments within a date range
     */
    public List<TaskComment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT comment_id, task_id, user_id, comment_text, created_at
            FROM TaskComments
            WHERE created_at BETWEEN ? AND ?
            ORDER BY created_at DESC
            """;

        List<TaskComment> comments = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                comments.add(extractTaskCommentFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding comments by date range: " + e.getMessage());
        }

        return comments;
    }

    // ==================== UPDATE ====================
    /**
     * Update comment text (edit comment)
     */
    public void update(TaskComment comment) {
        String sql = """
            UPDATE TaskComments
            SET comment_text = ?
            WHERE comment_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, comment.getCommentText());
            stmt.setInt(2, comment.getCommentId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✓ Comment updated successfully!");
            } else {
                System.out.println("⚠ No comment found with ID: " + comment.getCommentId());
            }

        } catch (SQLException e) {
            System.err.println("✗ Error updating comment: " + e.getMessage());
        }
    }

    // ==================== DELETE ====================
    /**
     * Delete a specific comment
     */
    public void delete(int id) {
        String sql = """
            DELETE FROM TaskComments
            WHERE comment_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("✓ Comment deleted successfully!");
            } else {
                System.out.println("⚠ No comment found with ID: " + id);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error deleting comment: " + e.getMessage());
        }
    }

    /**
     * Delete all comments for a task
     */
    public void deleteAllCommentsForTask(int taskId) {
        String sql = """
            DELETE FROM TaskComments
            WHERE task_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            int rowsDeleted = stmt.executeUpdate();
            System.out.println("✓ Deleted " + rowsDeleted + " comments for task");

        } catch (SQLException e) {
            System.err.println("✗ Error deleting comments for task: " + e.getMessage());
        }
    }

    /**
     * Delete all comments by a user
     */
    public void deleteAllCommentsByUser(int userId) {
        String sql = """
            DELETE FROM TaskComments
            WHERE user_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int rowsDeleted = stmt.executeUpdate();
            System.out.println("✓ Deleted " + rowsDeleted + " comments by user");

        } catch (SQLException e) {
            System.err.println("✗ Error deleting comments by user: " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================
    /**
     * Count comments for a task
     */
    public int countCommentsForTask(int taskId) {
        String sql = "SELECT COUNT(*) FROM TaskComments WHERE task_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting comments for task: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Count comments by a user
     */
    public int countCommentsByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM TaskComments WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting comments by user: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get the most recent comment for a task
     */
    public TaskComment findLatestCommentForTask(int taskId) {
        String sql = """
            SELECT TOP 1 comment_id, task_id, user_id, comment_text, created_at
            FROM TaskComments
            WHERE task_id = ?
            ORDER BY created_at DESC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractTaskCommentFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding latest comment: " + e.getMessage());
        }
        return null;
    }

    /**
     * Check if task has any comments
     */
    public boolean taskHasComments(int taskId) {
        String sql = "SELECT 1 FROM TaskComments WHERE task_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("✗ Error checking if task has comments: " + e.getMessage());
        }
        return false;
    }
}
