package com.ahmet.tpm.models;

import java.util.Date;

public class TimeTracking {

    private int timeTrackingId;
    private int taskId;
    private int userId;
    private Date workDate;
    private double hoursWorked;


    public TimeTracking() {
    }


    public TimeTracking(int timeTrackingId, int taskId, Date workDate, int userId, double hoursWorked) {
        this.timeTrackingId = timeTrackingId;
        this.taskId = taskId;
        this.workDate = workDate;
        this.userId = userId;
        this.hoursWorked = hoursWorked;
    }

    public TimeTracking(int taskId, int userId, Date workDate, double hoursWorked) {
        this.taskId = taskId;
        this.userId = userId;
        this.workDate = workDate;
        this.hoursWorked = hoursWorked;
    }

    public int getTimeTrackingId() {
        return timeTrackingId;
    }

    public void setTimeTrackingId(int timeTrackingId) {
        this.timeTrackingId = timeTrackingId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(double hoursWorked) {
        this.hoursWorked = hoursWorked;
    }
}
