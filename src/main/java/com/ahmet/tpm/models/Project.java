package com.ahmet.tpm.models;

import java.time.LocalDateTime;

public class Project {

    private int projectId;
    private String projectName;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime deadline;
    private Integer statusId;
    private Integer departmentId;
    private Integer createdBy;
    private LocalDateTime createdAt;


    public Project() {
    }

    public Project(int projectId, String projectName, String description, LocalDateTime startDate, LocalDateTime deadline, Integer statusId, Integer createdBy, Integer departmentId, LocalDateTime createdAt) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.description = description;
        this.startDate = startDate;
        this.deadline = deadline;
        this.statusId = statusId;
        this.createdBy = createdBy;
        this.departmentId = departmentId;
        this.createdAt = createdAt;
    }

    public Project(String projectName,String description,LocalDateTime startDate, LocalDateTime deadline, Integer statusId, Integer departmentId, Integer createdBy) {
        this.projectName = projectName;
        this.description = description;
        this.startDate = startDate;
        this.deadline = deadline;
        this.statusId = 1;// default 1 = planning
        this.departmentId = departmentId;
        this.createdBy = createdBy;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
