package com.ahmet.tpm.dao;

import com.ahmet.tpm.config.DatabaseConfig;
import com.ahmet.tpm.models.Project;


import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class ProjectDao {

    private Project extractProjectFromResultSet(ResultSet rs) throws SQLException {
        return new Project(
                rs.getInt("project_id"),
                rs.getString("project_name"),
                rs.getString("description"),
                rs.getTimestamp("start_date").toLocalDateTime(),
                rs.getTimestamp("deadline").toLocalDateTime(),
                rs.getInt("status_id"),
                rs.getInt("department_id"),
                rs.getInt("created_by"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }



    public int insert(Project project) throws SQLException {

        String sql = """
                INSERT INTO Projects (
                project_name,
                description,
                start_date,
                deadline,
                status_id,
                department_id,
                created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set parameters
            stmt.setString(1, project.getProjectName());
            stmt.setString(2, project.getDescription());
            stmt.setTimestamp(3, project.getStartDate() != null ?
                    Timestamp.valueOf(project.getStartDate()) : null);
            stmt.setTimestamp(4, project.getDeadline() != null ?
                    Timestamp.valueOf(project.getDeadline()) : null);
            stmt.setInt(5, project.getStatusId());

            if (project.getDepartmentId() != null) {
                stmt.setInt(6, project.getDepartmentId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.setInt(7, project.getCreatedBy());

            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Insert failed, no ID obtained.");
                }
            }


        }

    }

    public Project findById(int id) {
        String sql = """
            SELECT project_id, project_name, description, 
                   start_date, deadline, status_id, 
                   department_id, created_by, created_at
            FROM Projects
            WHERE project_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractProjectFromResultSet(rs);  // Use helper method
            }

        } catch (SQLException e) {
            System.err.println("  SQL State: " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("Error finding project: " + e.getMessage());
        }
        return null;
    }

    /**
     * Find all projects
     */
    public List<Project> findAll() {
        String sql = """
            SELECT project_id, project_name, description, 
                   start_date, deadline, status_id, 
                   department_id, created_by, created_at
            FROM Projects
            ORDER BY created_at DESC
            """;

        List<Project> projects = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                projects.add(extractProjectFromResultSet(rs));  // Use helper method
            }

        } catch (SQLException e) {
            System.err.println("  SQL State: " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());

            System.err.println("Error finding all projects: " + e.getMessage());
        }

        return projects;
    }

    /**
     * Find projects by status
     */
    public List<Project> findByStatus(int statusId) {
        String sql = """
            SELECT project_id, project_name, description, 
                   start_date, deadline, status_id, 
                   department_id, created_by, created_at
            FROM Projects
            WHERE status_id = ?
            ORDER BY deadline ASC
            """;

        List<Project> projects = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, statusId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                projects.add(extractProjectFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("  SQL State: " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("Error finding projects by status: " + e.getMessage());
        }

        return projects;
    }

    /**
     * Find projects by department
     */
    public List<Project> findByDepartment(int departmentId) {
        String sql = """
            SELECT project_id, project_name, description, 
                   start_date, deadline, status_id, 
                   department_id, created_by, created_at
            FROM Projects
            WHERE department_id = ?
            ORDER BY created_at DESC
            """;

        List<Project> projects = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                projects.add(extractProjectFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("  SQL State: " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("Error finding projects by department: " + e.getMessage());
        }

        return projects;
    }

    /**
     * Find projects created by user
     */
    public List<Project> findByCreator(int userId) {
        String sql = """
            SELECT project_id, project_name, description, 
                   start_date, deadline, status_id, 
                   department_id, created_by, created_at
            FROM Projects
            WHERE created_by = ?
            ORDER BY created_at DESC
            """;

        List<Project> projects = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                projects.add(extractProjectFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("  SQL State: " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("Error finding projects by creator: " + e.getMessage());
        }

        return projects;
    }

    public void updateStatus(int projectId, int newStatusId) {
        String sql = """
            UPDATE Projects
            SET status_id = ?
            WHERE project_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newStatusId);
            stmt.setInt(2, projectId);

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Project status updated successfully!");
            }

        } catch (SQLException e) {

            System.err.println("  SQL State: " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("Error updating project status: " + e.getMessage());
        }
    }


    public void delete(int projectId) {
        String sql = """
            DELETE FROM Projects
            WHERE project_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("✓ Project deleted successfully!");
            } else {
                System.out.println("⚠ No project found with ID: " + projectId);
            }

        } catch (SQLException e) {
            System.err.println("  SQL State: " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("✗ Error deleting project: " + e.getMessage());
            System.err.println("  Note: Cannot delete project if it has dependencies");
        }
    }
    public void update(Project project) {
        String sql = """
            UPDATE Projects
            SET project_name = ?,
                description = ?,
                start_date = ?,
                deadline = ?,
                status_id = ?,
                department_id = ?
            WHERE project_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, project.getProjectName());
            stmt.setString(2, project.getDescription());
            stmt.setTimestamp(3, project.getStartDate() != null ?
                    Timestamp.valueOf(project.getStartDate()) : null);
            stmt.setTimestamp(4, project.getDeadline() != null ?
                    Timestamp.valueOf(project.getDeadline()) : null);
            stmt.setInt(5, project.getStatusId());

            if (project.getDepartmentId() != null) {
                stmt.setInt(6, project.getDepartmentId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.setInt(7, project.getProjectId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✓ Project updated successfully!");
            } else {
                System.out.println("⚠ No project found with ID: " + project.getProjectId());
            }

        } catch (SQLException e) {
            System.err.println("  SQL State: " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("✗ Error updating project: " + e.getMessage());
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM Projects";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("âœ— Error counting projects: " + e.getMessage());
        }
        return 0;
    }

    public int countByStatus(int statusId) {
        String sql = "SELECT COUNT(*) FROM Projects WHERE status_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, statusId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("âœ— Error counting projects by status: " + e.getMessage());
        }
        return 0;
    }

    public boolean exists(int id) {
        String sql = "SELECT 1 FROM Projects WHERE project_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("âœ— Error checking project existence: " + e.getMessage());
        }
        return false;
    }











}


