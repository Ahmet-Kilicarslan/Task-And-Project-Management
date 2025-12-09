package com.ahmet.tpm.dao;

import com.ahmet.tpm.config.DatabaseConfig;
import com.ahmet.tpm.models.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DepartmentDao {
    // ==================== HELPER METHOD ====================
    private Department extractDepartmentFromResultSet(ResultSet rs) throws SQLException {
        return new Department(
                rs.getInt("department_id"),
                rs.getString("department_name")
        );
    }

    // ==================== CREATE ====================
    public void insert(Department department) {
        String sql = """
            INSERT INTO Departments (department_name)
            VALUES (?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, department.getDepartmentName());
            stmt.executeUpdate();
            System.out.println("âœ“ Department inserted successfully!");

        } catch (SQLException e) {
            System.err.println(" Error inserting department: " + e.getMessage());
        }
    }

    public Integer insertAndGetId(Department department) {
        String sql = """
            INSERT INTO Departments (department_name)
            VALUES (?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, department.getDepartmentName());
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    Integer generatedId = generatedKeys.getInt(1);
                    department.setDepartmentId(generatedId);
                    System.out.println("âœ“ Department inserted with ID: " + generatedId);
                    return generatedId;
                }
            }

        } catch (SQLException e) {
            System.err.println(" Error inserting department: " + e.getMessage());
        }
        return null;
    }

    // ==================== READ ====================
    public Department findById(int id) {
        String sql = """
            SELECT department_id, department_name
            FROM Departments
            WHERE department_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractDepartmentFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println(" Error finding department: " + e.getMessage());
        }
        return null;
    }

    public List<Department> findAll() {
        String sql = """
            SELECT department_id, department_name
            FROM Departments
            ORDER BY department_name ASC
            """;

        List<Department> departments = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                departments.add(extractDepartmentFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println(" Error finding all departments: " + e.getMessage());
        }

        return departments;
    }

    public Department findByName(String name) {
        String sql = """
            SELECT department_id, department_name
            FROM Departments
            WHERE department_name = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractDepartmentFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println(" Error finding department by name: " + e.getMessage());
        }
        return null;
    }

    // ==================== UPDATE ====================
    public void update(Department department) {
        String sql = """
            UPDATE Departments
            SET department_name = ?
            WHERE department_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, department.getDepartmentName());
            stmt.setInt(2, department.getDepartmentId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("âœ“ Department updated successfully!");
            } else {
                System.out.println("âš  No department found with ID: " + department.getDepartmentId());
            }

        } catch (SQLException e) {
            System.err.println(" Error updating department: " + e.getMessage());
        }
    }

    // ==================== DELETE ====================
    public void delete(int id) {
        String sql = """
            DELETE FROM Departments
            WHERE department_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("âœ“ Department deleted successfully!");
            } else {
                System.out.println("âš  No department found with ID: " + id);
            }

        } catch (SQLException e) {
            System.err.println(" Error deleting department: " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================
    public int count() {
        String sql = "SELECT COUNT(*) FROM Departments";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println(" Error counting departments: " + e.getMessage());
        }
        return 0;
    }

    public boolean exists(int id) {
        String sql = "SELECT 1 FROM Departments WHERE department_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println(" Error checking department existence: " + e.getMessage());
        }
        return false;
    }



}