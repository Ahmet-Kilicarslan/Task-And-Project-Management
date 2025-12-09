package com.ahmet.tpm.models;

import java.time.LocalDateTime;

/**
 * DTO class for Project with Department and Status details
 * Used when fetching projects with JOIN queries
 */
public class ProjectWithDetails {
    private int projectId;
    private String projectName;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime deadline;
    private int statusId;
    private String statusName;
    private int departmentId;
    private String departmentName;
    private int createdBy;
    private String createdByUsername;
    private LocalDateTime createdAt;

    // Constructor
    public ProjectWithDetails(int projectId, String projectName, String description,
                              LocalDateTime startDate, LocalDateTime deadline,
                              int statusId, String statusName,
                              int departmentId, String departmentName,
                              int createdBy, String createdByUsername,
                              LocalDateTime createdAt) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.description = description;
        this.startDate = startDate;
        this.deadline = deadline;
        this.statusId = statusId;
        this.statusName = statusName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.createdBy = createdBy;
        this.createdByUsername = createdByUsername;
        this.createdAt = createdAt;
    }

    // Getters
    public int getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public int getStatusId() {
        return statusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ProjectWithDetails{" +
                "projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", statusName='" + statusName + '\'' +
                ", departmentName='" + departmentName + '\'' +
                ", createdByUsername='" + createdByUsername + '\'' +
                '}';
    }
}