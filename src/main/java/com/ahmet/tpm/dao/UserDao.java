package com.ahmet.tpm.dao;

import com.ahmet.tpm.config.DatabaseConfig;
import com.ahmet.tpm.models.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public int insert(User user) throws SQLException {
        String sql = "INSERT INTO Users (username, password, email, full_name, department_id) " +
                "VALUES (?, ?, ?, ?, ?)";


        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getFullName());
            stmt.setInt(5, user.getDepartmentId());

            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }

            // Get the generated ID
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Insert failed, no ID obtained.");
                }
            }
        }

    }

    public User findById(int userId) throws SQLException {
        String sql = "SELECT * FROM Users WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;  // User not found
    }

    // READ - Get user by username
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM Users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // READ - Get all users
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM Users ORDER BY user_id";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        }
        return users;
    }

    // UPDATE - Update user information
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE Users SET username = ?, password = ?, email = ?, " +
                "full_name = ?, department_id = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getFullName());

            if (user.getDepartmentId() != null) {
                stmt.setInt(5, user.getDepartmentId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setInt(6, user.getUserId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // DELETE - Delete user by ID
    public boolean delete(int userId) throws SQLException {
        String sql = "DELETE FROM Users WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Helper method - Convert ResultSet to User object
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));

        // Handle NULL for department_id
        int deptId = rs.getInt("department_id");
        if (rs.wasNull()) {
            user.setDepartmentId(null);
        } else {
            user.setDepartmentId(deptId);
        }

        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            user.setCreatedAt(timestamp.toLocalDateTime());
        }

        return user;
    }

}
