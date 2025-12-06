package com.ahmet.tpm.models;

import java.time.LocalDateTime;

public class User {

    private int userId; //int cannot be null . Default is 0
    private String username;
    private String password;
    private String email;
    private String fullName;
    private Integer departmentId;
    private LocalDateTime createdAt; // Integer default is null

    // Constructor 1: Empty (for creating new users)
    public User(){

    }

    // Constructor 2: With all fields (for reading from database)
    public User(int userId,
                String username,
                String password,
                String email,
                String fullName,
                Integer departmentId,
                LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.departmentId = departmentId;
        this.createdAt = createdAt;
    }

    //Constructor 3: Without Id and createdAt (for inserting new users - DB will generate Id and date)
public User(String username,
            String password,
            String email,
            String fullName,
            Integer departmentId) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.departmentId = departmentId;

}

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
