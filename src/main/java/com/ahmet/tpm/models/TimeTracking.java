package com.ahmet.tpm.models;

import java.time.LocalDate;

public class TimeTracking {

    private Integer timeEntryId;
    private Integer taskId;
    private Integer userId;
    private LocalDate workDate;
    private Double hoursWorked;

    // Constructor 1: No-arg
    public TimeTracking() {
    }

    // Constructor 2: Without ID (for INSERT)
    public TimeTracking(Integer taskId, Integer userId, LocalDate workDate, Double hoursWorked) {
        this.taskId = taskId;
        this.userId = userId;
        this.workDate = workDate;
        this.hoursWorked = hoursWorked;
    }

    // Constructor 3: Full with ID (for SELECT)
    public TimeTracking(Integer timeEntryId, Integer taskId, Integer userId,
                        LocalDate workDate, Double hoursWorked) {
        this.timeEntryId = timeEntryId;
        this.taskId = taskId;
        this.userId = userId;
        this.workDate = workDate;
        this.hoursWorked = hoursWorked;
    }

    // Getters and Setters
    public Integer getTimeEntryId() {
        return timeEntryId;
    }

    public void setTimeEntryId(Integer timeEntryId) {
        this.timeEntryId = timeEntryId;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public Double getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(Double hoursWorked) {
        this.hoursWorked = hoursWorked;
    }
}