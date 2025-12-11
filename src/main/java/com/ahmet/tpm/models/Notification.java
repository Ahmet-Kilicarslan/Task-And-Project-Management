package com.ahmet.tpm.models;

import java.time.LocalDateTime;

/**
 * Notification model class
 * Represents a notification in the system
 */
public class Notification {

    private int notificationId;
    private int userId;
    private String notificationType;  // notification_type in DB
    private String title;
    private String message;
    private Integer taskId;  // task_id in DB
    private Integer projectId;  // project_id in DB
    private String actionUrl;
    private String priority;
    private boolean isRead;
    private LocalDateTime createdAt;

    // Constructors
    public Notification() {}

    public Notification(int userId, String notificationType, String title, String message) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.isRead = false;
        this.priority = "NORMAL";
    }

    // Full constructor (for database retrieval)
    public Notification(int notificationId, int userId, String notificationType,
                        String title, String message, Integer taskId, Integer projectId,
                        String actionUrl, String priority, boolean isRead, LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.taskId = taskId;
        this.projectId = projectId;
        this.actionUrl = actionUrl;
        this.priority = priority;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Backward compatibility methods (deprecated - use new names)
    @Deprecated
    public Integer getRelatedTaskId() {
        return taskId;
    }

    @Deprecated
    public void setRelatedTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    @Deprecated
    public Integer getRelatedProjectId() {
        return projectId;
    }

    @Deprecated
    public void setRelatedProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    // Utility methods
    public String getTimeAgo() {
        if (createdAt == null) return "";

        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(createdAt, now).getSeconds();

        // Debug logging (remove after testing)
        // System.out.println("DEBUG Time - Created: " + createdAt + " | Now: " + now + " | Seconds: " + seconds);

        if (seconds < 10) return "Just now";
        if (seconds < 60) return seconds + "s ago";

        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m ago";

        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";

        long days = hours / 24;
        if (days < 7) return days + "d ago";

        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    // Helper method to get icon color based on notification type
    public String getIconColor() {
        return switch (notificationType) {
            case "TASK_ASSIGNED" -> "#2196F3";
            case "TASK_COMPLETED" -> "#4CAF50";
            case "TASK_OVERDUE" -> "#F44336";
            case "TASK_STATUS_CHANGED" -> "#FF9800";
            case "TASK_COMMENT" -> "#9C27B0";
            case "PROJECT_MEMBER_ADDED" -> "#00BCD4";
            case "PROJECT_STATUS_CHANGED" -> "#FFC107";
            default -> "#607D8B";
        };
    }

    @Override
    public String toString() {
        return "Notification{" +
                "notificationId=" + notificationId +
                ", userId=" + userId +
                ", type='" + notificationType + '\'' +
                ", title='" + title + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}