package com.ahmet.tpm.dao;

import com.ahmet.tpm.config.DatabaseConfig;
import com.ahmet.tpm.models.TaskDependency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDependencyDao{

    // ==================== HELPER METHOD ====================
    private TaskDependency extractTaskDependencyFromResultSet(ResultSet rs) throws SQLException {
        return new TaskDependency(
                rs.getInt("dependency_id"),
                rs.getInt("task_id"),
                rs.getInt("depends_on_task_id")
        );
    }

    // ==================== CREATE ====================
    /**
     * Add a dependency: taskId depends on dependsOnTaskId
     */
    public void insert(TaskDependency dependency) {
        String sql = """
            INSERT INTO TaskDependencies (task_id, depends_on_task_id)
            VALUES (?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dependency.getTaskId());
            stmt.setInt(2, dependency.getDependsOnTaskId());
            stmt.executeUpdate();
            System.out.println("✓ Task dependency created successfully!");

        } catch (SQLException e) {
            System.err.println("✗ Error creating task dependency: " + e.getMessage());
            if (e.getMessage().contains("CHECK")) {
                System.err.println("  Note: A task cannot depend on itself");
            }
        }
    }

    /**
     * Add dependency (convenience method)
     */
    public void addDependency(int taskId, int dependsOnTaskId) {
        insert(new TaskDependency(taskId, dependsOnTaskId));
    }

    // ==================== READ ====================
    /**
     * Get all tasks that a specific task depends on
     * (What must be completed before this task can start?)
     */
    public List<TaskDependency> findDependenciesForTask(int taskId) {
        String sql = """
            SELECT dependency_id, task_id, depends_on_task_id
            FROM TaskDependencies
            WHERE task_id = ?
            """;

        List<TaskDependency> dependencies = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                dependencies.add(extractTaskDependencyFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding dependencies for task: " + e.getMessage());
        }

        return dependencies;
    }

    /**
     * Get all tasks that depend on a specific task
     * (What tasks are blocked by this task?)
     */
    public List<TaskDependency> findDependentTasks(int taskId) {
        String sql = """
            SELECT dependency_id, task_id, depends_on_task_id
            FROM TaskDependencies
            WHERE depends_on_task_id = ?
            """;

        List<TaskDependency> dependents = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                dependents.add(extractTaskDependencyFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding dependent tasks: " + e.getMessage());
        }

        return dependents;
    }

    /**
     * Get task IDs that must be completed before this task
     */
    public List<Integer> getDependencyIdsForTask(int taskId) {
        String sql = """
            SELECT depends_on_task_id
            FROM TaskDependencies
            WHERE task_id = ?
            """;

        List<Integer> dependencyIds = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                dependencyIds.add(rs.getInt("depends_on_task_id"));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error getting dependency IDs: " + e.getMessage());
        }

        return dependencyIds;
    }

    /**
     * Get task IDs that are blocked by this task
     */
    public List<Integer> getDependentTaskIds(int taskId) {
        String sql = """
            SELECT task_id
            FROM TaskDependencies
            WHERE depends_on_task_id = ?
            """;

        List<Integer> dependentIds = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                dependentIds.add(rs.getInt("task_id"));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error getting dependent task IDs: " + e.getMessage());
        }

        return dependentIds;
    }

    /**
     * Check if task A depends on task B
     */
    public boolean hasDependency(int taskId, int dependsOnTaskId) {
        String sql = """
            SELECT 1
            FROM TaskDependencies
            WHERE task_id = ? AND depends_on_task_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            stmt.setInt(2, dependsOnTaskId);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("✗ Error checking dependency: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if task has any dependencies (is it blocked?)
     */
    public boolean taskHasDependencies(int taskId) {
        String sql = "SELECT TOP 1 1 FROM TaskDependencies WHERE task_id = ? ";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("✗ Error checking if task has dependencies: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if task is blocking other tasks
     */
    public boolean taskIsBlocking(int taskId) {
        String sql = "SELECT TOP 1 1 FROM TaskDependencies WHERE depends_on_task_id = ? ";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("✗ Error checking if task is blocking: " + e.getMessage());
        }
        return false;
    }

    // ==================== DELETE ====================
    /**
     * Remove a specific dependency
     */
    public void delete(int taskId, int dependsOnTaskId) {
        String sql = """
            DELETE FROM TaskDependencies
            WHERE task_id = ? AND depends_on_task_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            stmt.setInt(2, dependsOnTaskId);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("✓ Dependency removed successfully!");
            } else {
                System.out.println("⚠ Dependency not found");
            }

        } catch (SQLException e) {
            System.err.println("✗ Error removing dependency: " + e.getMessage());
        }
    }

    /**
     * Remove all dependencies for a task (when deleting a task)
     */
    public void deleteAllDependenciesForTask(int taskId) {
        String sql = """
            DELETE FROM TaskDependencies
            WHERE task_id = ? OR depends_on_task_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            stmt.setInt(2, taskId);
            int rowsDeleted = stmt.executeUpdate();
            System.out.println("✓ Removed " + rowsDeleted + " dependencies related to task");

        } catch (SQLException e) {
            System.err.println("✗ Error removing all dependencies for task: " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================
    /**
     * Count how many dependencies a task has
     */
    public int countDependenciesForTask(int taskId) {
        String sql = "SELECT COUNT(*) FROM TaskDependencies WHERE task_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting dependencies: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Count how many tasks depend on this task
     */
    public int countDependentTasks(int taskId) {
        String sql = "SELECT COUNT(*) FROM TaskDependencies WHERE depends_on_task_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting dependent tasks: " + e.getMessage());
        }
        return 0;
    }
}