package com.ahmet.tpm.models;

import java.time.LocalDateTime;

public class TaskComment {

    private int commentId;
    private int taskId;
    private int userId;
    private String commentText;
    private LocalDateTime createdAt;

    public TaskComment() {
    }

    public TaskComment(int commentId, int taskId, int userId, String commentText, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.taskId = taskId;
        this.userId = userId;
        this.commentText = commentText;
        this.createdAt = createdAt;
    }

    public TaskComment(int taskId, int userId, String commentText) {
        this.taskId = taskId;
        this.userId = userId;
        this.commentText = commentText;
    }

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
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

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
