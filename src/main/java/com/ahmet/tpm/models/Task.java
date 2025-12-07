package com.ahmet.tpm.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task {

    private int taskId;
    private Integer projectId;
    private String taskName;
    private String description;
    private Integer statusId;
    private Integer priorityId;
    private double estimatedHours;
    private LocalDate dueDate;
    private Integer parentTaskId;
    private Integer createdBy;
    private LocalDateTime createdAt;

    public Task(){}

    public Task(int taskId, Integer projectId, String description, String taskName, Integer statusId, Integer priorityId, double estimatedHours,LocalDate dueDate, Integer parentTaskId, Integer createdBy, LocalDateTime createdAt) {
        this.taskId = taskId;
        this.projectId = projectId;
        this.description = description;
        this.taskName = taskName;
        this.statusId = statusId;
        this.priorityId = priorityId;
        this.estimatedHours = estimatedHours;
        this.dueDate = dueDate;
        this.parentTaskId = parentTaskId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Task(Integer projectId, String taskName, String description, Integer statusId, Integer priorityId, double estimatedHours,LocalDate dueDate, Integer parentTaskId, Integer createdBy) {
        this.projectId = projectId;
        this.taskName = taskName;
        this.description = description;
        this.statusId = statusId;
        this.priorityId = priorityId;
        this.estimatedHours = estimatedHours;
        this.dueDate = dueDate;
        this.parentTaskId = parentTaskId;
        this.createdBy = createdBy;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPriorityId() {
        return priorityId;
    }

    public void setPriorityId(Integer priorityId) {
        this.priorityId = priorityId;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Integer getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(Integer parentTaskId) {
        this.parentTaskId = parentTaskId;
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
