package com.ahmet.tpm.models;

import java.time.LocalDateTime;

public class TaskMember {

    private int taskMemberId;
    private int taskId;
    private int userId;
    private LocalDateTime assignedAt;

    public TaskMember(){}

    public TaskMember(int taskMemberId, int taskId, int userId, LocalDateTime assignedAt) {
        this.taskMemberId = taskMemberId;
        this.taskId = taskId;
        this.userId = userId;
        this.assignedAt = assignedAt;
    }

    public TaskMember(int taskId, int userId) {
        this.taskId = taskId;
        this.userId = userId;
    }

    public int getTaskMemberId() {
        return taskMemberId;
    }

    public void setTaskMemberId(int taskMemberId) {
        this.taskMemberId = taskMemberId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
