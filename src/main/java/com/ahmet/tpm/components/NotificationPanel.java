package com.ahmet.tpm.components;

import com.ahmet.tpm.dao.NotificationDao;
import com.ahmet.tpm.models.Notification;
import com.ahmet.tpm.service.NotificationService;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel to display notifications in a dropdown/popup style
 */
public class NotificationPanel extends JPanel {

    private int userId;
    private NotificationService notificationService;
    private JPanel notificationsContainer;
    private JScrollPane scrollPane;
    private JLabel lblNoNotifications;

    public NotificationPanel(int userId) {
        this.userId = userId;
        this.notificationService = new NotificationService();

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(350, 400));
        setBackground(StyleUtil.SURFACE);
        setBorder(BorderFactory.createLineBorder(StyleUtil.BORDER, 1));

        initializeUI();
        loadNotifications();
    }

    private void initializeUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(StyleUtil.SURFACE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Notifications");
        lblTitle.setFont(StyleUtil.FONT_HEADING);
        lblTitle.setForeground(StyleUtil.TEXT_PRIMARY);

        JButton btnMarkAllRead = new JButton("Mark all as read");
        btnMarkAllRead.setFont(StyleUtil.FONT_SMALL);
        btnMarkAllRead.setForeground(StyleUtil.PRIMARY);
        btnMarkAllRead.setBackground(StyleUtil.SURFACE);
        btnMarkAllRead.setBorderPainted(false);
        btnMarkAllRead.setFocusPainted(false);
        btnMarkAllRead.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMarkAllRead.addActionListener(e -> markAllAsRead());

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnMarkAllRead, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Notifications container
        notificationsContainer = new JPanel();
        notificationsContainer.setLayout(new BoxLayout(notificationsContainer, BoxLayout.Y_AXIS));
        notificationsContainer.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(notificationsContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // No notifications label
        lblNoNotifications = new JLabel("No notifications");
        lblNoNotifications.setFont(StyleUtil.FONT_BODY);
        lblNoNotifications.setForeground(StyleUtil.TEXT_SECONDARY);
        lblNoNotifications.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblNoNotifications.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
    }

    public void loadNotifications() {
        notificationsContainer.removeAll();

        List<Notification> notifications = notificationService.getUserNotifications(userId);

        if (notifications.isEmpty()) {
            notificationsContainer.add(lblNoNotifications);
        } else {
            for (Notification notification : notifications) {
                notificationsContainer.add(createNotificationItem(notification));
            }
        }

        notificationsContainer.revalidate();
        notificationsContainer.repaint();
    }

    private JPanel createNotificationItem(Notification notification) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BorderLayout(10, 5));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, StyleUtil.BORDER),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        // Background color based on read status
        if (notification.isRead()) {
            itemPanel.setBackground(Color.WHITE);
        } else {
            itemPanel.setBackground(new Color(240, 248, 255)); // Light blue for unread
        }

        // Left side - Icon based on type
        JLabel iconLabel = new JLabel(getIconForType(notification.getNotificationType()));
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        iconLabel.setForeground(getColorForType(notification.getNotificationType()));

        // Center - Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(notification.getTitle());
        lblTitle.setFont(notification.isRead() ? StyleUtil.FONT_BODY : StyleUtil.FONT_SUBHEADING);
        lblTitle.setForeground(StyleUtil.TEXT_PRIMARY);

        JLabel lblMessage = new JLabel("<html>" + notification.getMessage() + "</html>");
        lblMessage.setFont(StyleUtil.FONT_SMALL);
        lblMessage.setForeground(StyleUtil.TEXT_SECONDARY);

        JLabel lblTime = new JLabel(formatTime(notification.getCreatedAt()));
        lblTime.setFont(StyleUtil.FONT_SMALL);
        lblTime.setForeground(StyleUtil.TEXT_SECONDARY);

        contentPanel.add(lblTitle);
        contentPanel.add(Box.createVerticalStrut(3));
        contentPanel.add(lblMessage);
        contentPanel.add(Box.createVerticalStrut(3));
        contentPanel.add(lblTime);

        // Right side - Actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionsPanel.setOpaque(false);

        if (!notification.isRead()) {
            JButton btnMarkRead = new JButton("Mark Read");
            btnMarkRead.setFont(StyleUtil.FONT_SMALL);
            btnMarkRead.setForeground(StyleUtil.PRIMARY);
            btnMarkRead.setBackground(Color.WHITE);
            btnMarkRead.setBorder(BorderFactory.createLineBorder(StyleUtil.PRIMARY));
            btnMarkRead.setFocusPainted(false);
            btnMarkRead.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnMarkRead.addActionListener(e -> {
                notificationService.markAsRead(notification.getNotificationId());
                loadNotifications();
            });
            actionsPanel.add(btnMarkRead);
        }

        JButton btnDelete = new JButton("X");
        btnDelete.setFont(StyleUtil.FONT_SMALL);
        btnDelete.setForeground(StyleUtil.DANGER);
        btnDelete.setBackground(Color.WHITE);
        btnDelete.setBorderPainted(false);
        btnDelete.setFocusPainted(false);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> {
            notificationService.deleteNotification(notification.getNotificationId());
            loadNotifications();
        });
        actionsPanel.add(btnDelete);

        // Assemble
        itemPanel.add(iconLabel, BorderLayout.WEST);
        itemPanel.add(contentPanel, BorderLayout.CENTER);
        itemPanel.add(actionsPanel, BorderLayout.EAST);

        // Make clickable to mark as read
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        itemPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!notification.isRead()) {
                    notificationService.markAsRead(notification.getNotificationId());
                    loadNotifications();
                }
            }
        });

        return itemPanel;
    }

    private void markAllAsRead() {
        notificationService.markAllAsRead(userId);
        loadNotifications();
    }

    private String getIconForType(String type) {
        switch (type) {
            case "TASK_ASSIGNED": return "📋";
            case "TASK_UPDATED": return "✏️";
            case "TASK_STATUS_CHANGED": return "🔄";
            case "TASK_OVERDUE": return "⚠️";
            case "TASK_COMPLETED": return "✅";
            case "TASK_COMMENT": return "💬";
            case "PROJECT_MEMBER_ADDED": return "👥";
            case "PROJECT_UPDATED": return "📁";
            case "PROJECT_STATUS_CHANGED": return "🔄";
            case "PROJECT_MEMBER_REMOVED": return "👋";
            default: return "🔔";
        }
    }

    private Color getColorForType(String type) {
        switch (type) {
            case "TASK_OVERDUE": return StyleUtil.DANGER;
            case "TASK_COMPLETED": return StyleUtil.SUCCESS;
            case "TASK_ASSIGNED":
            case "PROJECT_MEMBER_ADDED": return StyleUtil.PRIMARY;
            case "TASK_STATUS_CHANGED":
            case "PROJECT_STATUS_CHANGED": return StyleUtil.WARNING;
            default: return StyleUtil.INFO;
        }
    }

    private String formatTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";

        java.time.Duration duration = java.time.Duration.between(dateTime, java.time.LocalDateTime.now());
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (hours < 24) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        if (days < 7) return days + " day" + (days > 1 ? "s" : "") + " ago";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        return dateTime.format(formatter);
    }
}