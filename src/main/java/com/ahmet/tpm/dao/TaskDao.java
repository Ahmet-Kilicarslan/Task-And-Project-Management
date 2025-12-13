package com.ahmet.tpm.dao;


import com.ahmet.tpm.config.DatabaseConfig;
import com.ahmet.tpm.models.Task;
import com.ahmet.tpm.models.TaskStatus;

import com.ahmet.tpm.dao.TaskStatusDao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskDao {

    TaskStatusDao taskStatusDao = new TaskStatusDao();


    public boolean areAllDependenciesCompleted(int taskId) {
        String sql = "SELECT COUNT(*) as incomplete_count " +
                "FROM TaskDependencies td " +
                "JOIN Tasks t ON td.depends_on_task_id = t.task_id " +
                "JOIN TaskStatus ts ON t.status_id = ts.status_id " +
                "WHERE td.task_id = ? " +
                "AND ts.status_name != 'DONE'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int incompleteCount = rs.getInt("incomplete_count");
                return incompleteCount == 0;  // true if no incomplete dependencies
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error checking dependencies: " + e.getMessage());
            e.printStackTrace();
        }

        return false;  // If error occurs, assume not ready
    }

// ═══════════════════════════════════════════════════════════════════════════
// METHOD 2: Get list of incomplete dependency names (for error messages)
// ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get names of dependencies that are not yet completed
     *
     * @param taskId The task to check
     * @return List of incomplete dependency task names
     */
    public List<String> getIncompleteDependencyNames(int taskId) {
        List<String> incompleteNames = new ArrayList<>();

        String sql = "SELECT t.task_name, ts.status_name " +
                "FROM TaskDependencies td " +
                "JOIN Tasks t ON td.depends_on_task_id = t.task_id " +
                "JOIN TaskStatus ts ON t.status_id = ts.status_id " +
                "WHERE td.task_id = ? " +
                "AND ts.status_name != 'DONE' " +
                "ORDER BY t.task_name";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String taskName = rs.getString("task_name");
                String statusName = rs.getString("status_name");
                incompleteNames.add(taskName + " [" + statusName + "]");
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error getting incomplete dependencies: " + e.getMessage());
            e.printStackTrace();
        }

        return incompleteNames;
    }

// ═══════════════════════════════════════════════════════════════════════════
// METHOD 3: Modified update() method with validation
// ═══════════════════════════════════════════════════════════════════════════

    /**
     * UPDATE YOUR EXISTING update() METHOD TO INCLUDE THIS VALIDATION
     *
     * Add this code at the BEGINNING of your update() method, before the SQL UPDATE
     */




   // ==================== HELPER METHOD ====================

    private Task extractTaskFromResultSet(ResultSet rs) throws SQLException {
        return new Task(
                rs.getInt("task_id"),
                rs.getInt("project_id"),
                rs.getString("task_name"),
                rs.getString("description"),
                rs.getInt("status_id"),
                rs.getInt("priority_id"),
                rs.getObject("estimated_hours", Double.class),
                rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate() : null,
                rs.getObject("parent_task_id", Integer.class),
                rs.getInt("created_by"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    // ==================== CREATE ====================
    public void insert(Task task) {
        String sql = """
            INSERT INTO Tasks 
                (project_id, task_name, description, status_id, priority_id, 
                 estimated_hours, due_date, parent_task_id, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, task.getProjectId());
            stmt.setString(2, task.getTaskName());
            stmt.setString(3, task.getDescription());
            stmt.setInt(4, task.getStatusId());
            stmt.setInt(5, task.getPriorityId());
            stmt.setObject(6, task.getEstimatedHours());  // Clean! Handles null
            stmt.setObject(7, task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : null);
            stmt.setObject(8, task.getParentTaskId());  // Clean! Handles null
            stmt.setInt(9, task.getCreatedBy());

            stmt.executeUpdate();
            System.out.println("✓ Task inserted successfully!");

        } catch (SQLException e) {
            System.err.println("✗ Error inserting task: " + e.getMessage());
            System.err.println("  SQL State: " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
        }
    }

    public Integer insertAndGetId(Task task) {
        String sql = """
            INSERT INTO Tasks 
                (project_id, task_name, description, status_id, priority_id, 
                 estimated_hours, due_date, parent_task_id, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, task.getProjectId());
            stmt.setString(2, task.getTaskName());
            stmt.setString(3, task.getDescription());
            stmt.setInt(4, task.getStatusId());
            stmt.setInt(5, task.getPriorityId());
            stmt.setObject(6, task.getEstimatedHours());
            stmt.setObject(7, task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : null);
            stmt.setObject(8, task.getParentTaskId());
            stmt.setInt(9, task.getCreatedBy());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    Integer generatedId = generatedKeys.getInt(1);
                    task.setTaskId(generatedId);
                    System.out.println("✓ Task inserted with ID: " + generatedId);
                    return generatedId;
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ Error inserting task: " + e.getMessage());
        }
        return null;
    }

    // ==================== READ ====================
    public Task findById(int id) {
        String sql = """
            SELECT task_id, project_id, task_name, description, status_id,
                   priority_id, estimated_hours, due_date, parent_task_id,
                   created_by, created_at
            FROM Tasks
            WHERE task_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractTaskFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding task by ID: " + e.getMessage());
        }
        return null;
    }

    public List<Task> findAll() {
        String sql = """
            SELECT task_id, project_id, task_name, description, status_id,
                   priority_id, estimated_hours, due_date, parent_task_id,
                   created_by, created_at
            FROM Tasks
            ORDER BY created_at DESC
            """;

        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tasks.add(extractTaskFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding all tasks: " + e.getMessage());
        }

        return tasks;
    }

    public List<Task> findByProject(int projectId) {
        String sql = """
            SELECT task_id, project_id, task_name, description, status_id,
                   priority_id, estimated_hours, due_date, parent_task_id,
                   created_by, created_at
            FROM Tasks
            WHERE project_id = ?
            ORDER BY priority_id DESC, due_date ASC
            """;

        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(extractTaskFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding tasks by project: " + e.getMessage());
        }

        return tasks;
    }

    public List<Task> findByStatus(int statusId) {
        String sql = """
            SELECT task_id, project_id, task_name, description, status_id,
                   priority_id, estimated_hours, due_date, parent_task_id,
                   created_by, created_at
            FROM Tasks
            WHERE status_id = ?
            ORDER BY priority_id DESC, due_date ASC
            """;

        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, statusId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(extractTaskFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding tasks by status: " + e.getMessage());
        }

        return tasks;
    }

    public List<Task> findByPriority(int priorityId) {
        String sql = """
            SELECT task_id, project_id, task_name, description, status_id,
                   priority_id, estimated_hours, due_date, parent_task_id,
                   created_by, created_at
            FROM Tasks
            WHERE priority_id = ?
            ORDER BY due_date ASC
            """;

        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, priorityId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(extractTaskFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding tasks by priority: " + e.getMessage());
        }

        return tasks;
    }

    public List<Task> findSubtasks(int parentTaskId) {
        String sql = """
            SELECT task_id, project_id, task_name, description, status_id,
                   priority_id, estimated_hours, due_date, parent_task_id,
                   created_by, created_at
            FROM Tasks
            WHERE parent_task_id = ?
            ORDER BY created_at ASC
            """;

        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, parentTaskId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(extractTaskFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding subtasks: " + e.getMessage());
        }

        return tasks;
    }

    public List<Task> findOverdueTasks() {
        String sql = """
            SELECT task_id, project_id, task_name, description, status_id,
                   priority_id, estimated_hours, due_date, parent_task_id,
                   created_by, created_at
            FROM Tasks
            WHERE due_date < CAST(GETDATE() AS DATE)
              AND status_id != (SELECT status_id FROM TaskStatus WHERE status_name = 'Done')
            ORDER BY due_date ASC
            """;

        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tasks.add(extractTaskFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding overdue tasks: " + e.getMessage());
        }

        return tasks;
    }

    public List<Task> findTasksDueToday() {
        String sql = """
            SELECT task_id, project_id, task_name, description, status_id,
                   priority_id, estimated_hours, due_date, parent_task_id,
                   created_by, created_at
            FROM Tasks
            WHERE due_date = CAST(GETDATE() AS DATE)
            ORDER BY priority_id DESC
            """;

        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tasks.add(extractTaskFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error finding tasks due today: " + e.getMessage());
        }

        return tasks;
    }

    public List<Task> searchByName(String keyword) {
        String sql = """
            SELECT task_id, project_id, task_name, description, status_id,
                   priority_id, estimated_hours, due_date, parent_task_id,
                   created_by, created_at
            FROM Tasks
            WHERE task_name LIKE ?
            ORDER BY task_name ASC
            """;

        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(extractTaskFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error searching tasks: " + e.getMessage());
        }

        return tasks;
    }

    // ==================== UPDATE ====================
    /*public void update(Task task) throws SQLException {


        TaskStatus newStatus = taskStatusDao.findById(task.getStatusId());

        if (newStatus != null && "DONE".equals(newStatus.getStatusName())) {
            // Check if all dependencies are completed
            if (!areAllDependenciesCompleted(task.getTaskId())) {
                // Get incomplete dependency names for error message
                List<String> incompleteNames = getIncompleteDependencyNames(task.getTaskId());

                String errorMessage = "Cannot mark task as DONE. The following dependencies must be completed first:\n" +
                        String.join("\n", incompleteNames);

                System.err.println("❌ VALIDATION FAILED: " + errorMessage);
                throw new SQLException(errorMessage);
            }

        String sql = """
            UPDATE Tasks
            SET project_id = ?,
                task_name = ?,
                description = ?,
                status_id = ?,
                priority_id = ?,
                estimated_hours = ?,
                due_date = ?,
                parent_task_id = ?
            WHERE task_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, task.getProjectId());
            stmt.setString(2, task.getTaskName());
            stmt.setString(3, task.getDescription());
            stmt.setInt(4, task.getStatusId());
            stmt.setInt(5, task.getPriorityId());
            stmt.setObject(6, task.getEstimatedHours());
            stmt.setObject(7, task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : null);
            stmt.setObject(8, task.getParentTaskId());
            stmt.setInt(9, task.getTaskId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✓ Task updated successfully!");
            } else {
                System.out.println("⚠ No task found with ID: " + task.getTaskId());
            }

        } catch (SQLException e) {
            System.err.println("✗ Error updating task: " + e.getMessage());
        }
    }}*/

    public void update(Task task) {
        String sql = """
        UPDATE Tasks
        SET project_id = ?,
            task_name = ?,
            description = ?,
            status_id = ?,
            priority_id = ?,
            estimated_hours = ?,
            due_date = ?,
            parent_task_id = ?
        WHERE task_id = ?
        """;

        Connection conn = null;  // ⭐ Declare outside try-with-resources
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);  // ⭐ DISABLE auto-commit

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, task.getProjectId());
            stmt.setString(2, task.getTaskName());
            stmt.setString(3, task.getDescription());
            stmt.setInt(4, task.getStatusId());
            stmt.setInt(5, task.getPriorityId());
            stmt.setObject(6, task.getEstimatedHours());
            stmt.setObject(7, task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : null);
            stmt.setObject(8, task.getParentTaskId());
            stmt.setInt(9, task.getTaskId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                conn.commit();  // ⭐ COMMIT the transaction
                System.out.println("✓ Task updated successfully!");
            } else {
                conn.rollback();  // ⭐ ROLLBACK if nothing updated
                System.out.println("⚠ No task found with ID: " + task.getTaskId());
            }

        } catch (SQLException e) {
            System.err.println("✗ Error updating task: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();  // ⭐ ROLLBACK on error
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);  // ⭐ Reset auto-commit
                    conn.close();  // ⭐ Close connection
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateStatus(int taskId, int newStatusId) {
        String sql = """
            UPDATE Tasks
            SET status_id = ?
            WHERE task_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newStatusId);
            stmt.setInt(2, taskId);

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✓ Task status updated successfully!");
            } else {
                System.out.println("⚠ No task found with ID: " + taskId);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error updating task status: " + e.getMessage());
        }
    }

    // ==================== DELETE ====================
    public void delete(int id) {
        String sql = """
            DELETE FROM Tasks
            WHERE task_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("✓ Task deleted successfully!");
            } else {
                System.out.println("⚠ No task found with ID: " + id);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error deleting task: " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================
    public int count() {
        String sql = "SELECT COUNT(*) FROM Tasks";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting tasks: " + e.getMessage());
        }
        return 0;
    }

    public int countByProject(int projectId) {
        String sql = "SELECT COUNT(*) FROM Tasks WHERE project_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting tasks by project: " + e.getMessage());
        }
        return 0;
    }

    public int countByStatus(int statusId) {
        String sql = "SELECT COUNT(*) FROM Tasks WHERE status_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, statusId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting tasks by status: " + e.getMessage());
        }
        return 0;
    }

    public int countByProjectAndStatus(int projectId, int statusId) {
        String sql = "SELECT COUNT(*) FROM Tasks WHERE project_id = ? AND status_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            stmt.setInt(2, statusId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error counting tasks by project and status: " + e.getMessage());
        }
        return 0;
    }

    public boolean exists(int id) {
        String sql = "SELECT 1 FROM Tasks WHERE task_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("✗ Error checking task existence: " + e.getMessage());
        }
        return false;
    }



}
