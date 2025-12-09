package com.ahmet.tpm.service;

import com.ahmet.tpm.dao.NotificationDao;
import com.ahmet.tpm.dao.ProjectMemberDao;
import com.ahmet.tpm.dao.TaskMemberDao;
import com.ahmet.tpm.models.Notification;

import java.util.List;

/**
 * Service layer for notification operations
 * Handles business logic for creating and managing notifications
 */
public class NotificationService {

    private NotificationDao notificationDao;
    private TaskMemberDao taskMemberDao;
    private ProjectMemberDao projectMemberDao;

    public NotificationService() {
        this.notificationDao = new NotificationDao();
        this.taskMemberDao = new TaskMemberDao();
        this.projectMemberDao = new ProjectMemberDao();
    }

    // ==================== TASK NOTIFICATIONS ====================

    /**
     * Notify when a user is assigned to a task
     */
    public void notifyTaskAssignment(int taskId, int assignedUserId, String taskName, String assignerName) {
        Notification notification = new Notification(
                assignedUserId,
                "TASK_ASSIGNED",
                "New Task Assignment",
                assignerName + " assigned you to task: " + taskName
        );
        notification.setTaskId(taskId);
        notification.setPriority("NORMAL");
        notificationDao.insert(notification);
    }

    /**
     * Notify all task members when task is updated
     */
    public void notifyTaskUpdate(int taskId, String taskName, String updaterName, String updateType) {
        List<Integer> memberIds = taskMemberDao.getUserIdsForTask(taskId);

        for (int userId : memberIds) {
            Notification notification = new Notification(
                    userId,
                    "TASK_UPDATED",
                    "Task Updated",
                    updaterName + " updated " + updateType + " in task: " + taskName
            );
            notification.setTaskId(taskId);
            notification.setPriority("LOW");
            notificationDao.insert(notification);
        }
    }

    /**
     * Notify when task status changes
     */
    public void notifyTaskStatusChange(int taskId, String taskName, String newStatus, String changerName) {
        List<Integer> memberIds = taskMemberDao.getUserIdsForTask(taskId);

        for (int userId : memberIds) {
            Notification notification = new Notification(
                    userId,
                    "TASK_STATUS_CHANGED",
                    "Task Status Changed",
                    changerName + " changed status of '" + taskName + "' to " + newStatus
            );
            notification.setTaskId(taskId);
            notification.setPriority("NORMAL");
            notificationDao.insert(notification);
        }
    }

    /**
     * Notify when task is overdue
     */
    public void notifyTaskOverdue(int taskId, String taskName, List<Integer> assigneeIds) {
        for (int userId : assigneeIds) {
            Notification notification = new Notification(
                    userId,
                    "TASK_OVERDUE",
                    "Task Overdue",
                    "Task '" + taskName + "' is now overdue!"
            );
            notification.setTaskId(taskId);
            notification.setPriority("URGENT");
            notificationDao.insert(notification);
        }
    }

    /**
     * Notify when task is completed
     */
    public void notifyTaskCompletion(int taskId, String taskName, String completerName) {
        List<Integer> memberIds = taskMemberDao.getUserIdsForTask(taskId);

        for (int userId : memberIds) {
            Notification notification = new Notification(
                    userId,
                    "TASK_COMPLETED",
                    "Task Completed",
                    completerName + " marked task '" + taskName + "' as complete"
            );
            notification.setTaskId(taskId);
            notification.setPriority("LOW");
            notificationDao.insert(notification);
        }
    }

    /**
     * Notify when comment is added to task
     */
    public void notifyTaskComment(int taskId, String taskName, String commenterName, int commentOwnerId) {
        List<Integer> memberIds = taskMemberDao.getUserIdsForTask(taskId);

        for (int userId : memberIds) {
            if (userId != commentOwnerId) { // Don't notify the commenter
                Notification notification = new Notification(
                        userId,
                        "TASK_COMMENT",
                        "New Comment",
                        commenterName + " commented on task: " + taskName
                );
                notification.setTaskId(taskId);
                notification.setPriority("LOW");
                notificationDao.insert(notification);
            }
        }
    }

    // ==================== PROJECT NOTIFICATIONS ====================

    /**
     * Notify when user is added to a project
     */
    public void notifyProjectMemberAdded(int projectId, int userId, String projectName, String adderName) {
        Notification notification = new Notification(
                userId,
                "PROJECT_MEMBER_ADDED",
                "Added to Project",
                adderName + " added you to project: " + projectName
        );
        notification.setProjectId(projectId);
        notification.setPriority("NORMAL");
        notificationDao.insert(notification);
    }

    /**
     * Notify all project members when project is updated
     */
    public void notifyProjectUpdate(int projectId, String projectName, String updaterName, String updateType) {
        List<Integer> memberIds = projectMemberDao.getUserIdsForProject(projectId);

        for (int userId : memberIds) {
            Notification notification = new Notification(
                    userId,
                    "PROJECT_UPDATED",
                    "Project Updated",
                    updaterName + " updated " + updateType + " in project: " + projectName
            );
            notification.setProjectId(projectId);
            notification.setPriority("LOW");
            notificationDao.insert(notification);
        }
    }

    /**
     * Notify when project status changes
     */
    public void notifyProjectStatusChange(int projectId, String projectName, String newStatus, String changerName) {
        List<Integer> memberIds = projectMemberDao.getUserIdsForProject(projectId);

        for (int userId : memberIds) {
            Notification notification = new Notification(
                    userId,
                    "PROJECT_STATUS_CHANGED",
                    "Project Status Changed",
                    changerName + " changed status of '" + projectName + "' to " + newStatus
            );
            notification.setProjectId(projectId);
            notification.setPriority("NORMAL");
            notificationDao.insert(notification);
        }
    }

    /**
     * Notify when user is removed from project
     */
    public void notifyProjectMemberRemoved(int projectId, int userId, String projectName, String removerName) {
        Notification notification = new Notification(
                userId,
                "PROJECT_MEMBER_REMOVED",
                "Removed from Project",
                removerName + " removed you from project: " + projectName
        );
        notification.setProjectId(projectId);
        notification.setPriority("NORMAL");
        notificationDao.insert(notification);
    }

    // ==================== GENERAL OPERATIONS ====================

    /**
     * Get all notifications for user
     */
    public List<Notification> getUserNotifications(int userId) {
        return notificationDao.findByUserId(userId);
    }

    /**
     * Get unread notifications for user
     */
    public List<Notification> getUnreadNotifications(int userId) {
        return notificationDao.findUnreadByUserId(userId);
    }

    /**
     * Get unread count for user
     */
    public int getUnreadCount(int userId) {
        return notificationDao.getUnreadCount(userId);
    }

    /**
     * Mark notification as read
     */
    public boolean markAsRead(int notificationId) {
        return notificationDao.markAsRead(notificationId);
    }

    /**
     * Mark all notifications as read for user
     */
    public boolean markAllAsRead(int userId) {
        return notificationDao.markAllAsRead(userId);
    }

    /**
     * Delete notification
     */
    public boolean deleteNotification(int notificationId) {
        return notificationDao.delete(notificationId);
    }
}