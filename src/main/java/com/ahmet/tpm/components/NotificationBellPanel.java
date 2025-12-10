package com.ahmet.tpm.components;

import com.ahmet.tpm.dao.NotificationDao;
import com.ahmet.tpm.models.Notification;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Notification Bell Panel for Navbar
 * Shows notification icon with unread count badge
 * Opens popup menu on click
 */
public class NotificationBellPanel extends JPanel {

    private int userId;
    private NotificationDao notificationDao;
    private JFrame parentFrame;
    private Runnable onViewAllClicked;

    private JLabel lblBell;
    private JLabel lblBadge;
    private JPopupMenu popupMenu;
    private Timer refreshTimer;

    private int unreadCount = 0;

    public NotificationBellPanel(JFrame parentFrame, int userId) {
        this.parentFrame = parentFrame;
        this.userId = userId;
        this.notificationDao = new NotificationDao();

        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        setBackground(StyleUtil.SURFACE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        initializeUI();
        refreshUnreadCount();
        startAutoRefresh();

        // Click handler
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showNotificationPopup();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(StyleUtil.PRIMARY_LIGHT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(StyleUtil.SURFACE);
            }
        });

        setPreferredSize(new Dimension(40, 40));
        setMinimumSize(new Dimension(40, 40));
        setMaximumSize(new Dimension(40, 40));
        setVisible(true);
       // add(new JLabel("!"));
    }

    private void initializeUI() {
        // Bell icon
        lblBell = new JLabel("!");
        lblBell.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        add(lblBell);

        // Badge (unread count)
        lblBadge = new JLabel("0");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setForeground(Color.WHITE);
        lblBadge.setBackground(StyleUtil.DANGER);
        lblBadge.setOpaque(true);
        lblBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        lblBadge.setVisible(false);
        add(lblBadge);
    }

    private void showNotificationPopup() {
        if (popupMenu != null && popupMenu.isVisible()) {
            popupMenu.setVisible(false);
            return;
        }

        popupMenu = createNotificationPopup();
        popupMenu.show(this, 0, getHeight());
    }

    private JPopupMenu createNotificationPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.setPreferredSize(new Dimension(400, 450));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(StyleUtil.SURFACE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JLabel titleLabel = new JLabel("Bildirimler");
        titleLabel.setFont(StyleUtil.FONT_SUBHEADING);
        titleLabel.setForeground(StyleUtil.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton btnMarkAllRead = new JButton("Tümünü Okundu İşaretle");
        btnMarkAllRead.setFont(StyleUtil.FONT_SMALL);
        btnMarkAllRead.setForeground(StyleUtil.PRIMARY);
        btnMarkAllRead.setBorderPainted(false);
        btnMarkAllRead.setContentAreaFilled(false);
        btnMarkAllRead.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMarkAllRead.addActionListener(e -> {
            markAllAsRead();
            popup.setVisible(false);
        });
        headerPanel.add(btnMarkAllRead, BorderLayout.EAST);

        popup.add(headerPanel);
        popup.addSeparator();

        // Notifications list
        JPanel notificationsPanel = new JPanel();
        notificationsPanel.setLayout(new BoxLayout(notificationsPanel, BoxLayout.Y_AXIS));
        notificationsPanel.setBackground(Color.WHITE);

        List<Notification> notifications = notificationDao.getRecentNotifications(userId, 5);

        if (notifications.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

            JLabel emptyLabel = new JLabel("Bildirim yok");
            emptyLabel.setFont(StyleUtil.FONT_BODY);
            emptyLabel.setForeground(StyleUtil.TEXT_SECONDARY);
            emptyPanel.add(emptyLabel);

            notificationsPanel.add(emptyPanel);
        } else {
            for (Notification notification : notifications) {
                JPanel notificationItem = createNotificationItem(notification, popup);
                notificationsPanel.add(notificationItem);
                notificationsPanel.add(Box.createVerticalStrut(1));
            }
        }

        JScrollPane scrollPane = new JScrollPane(notificationsPanel);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        popup.add(scrollPane);

        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(StyleUtil.SURFACE);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, StyleUtil.BORDER),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JButton btnViewAll = ComponentFactory.createSecondaryButton("Tümünü Gör");
        btnViewAll.addActionListener(e -> {
            popup.setVisible(false);
            openNotificationsDialog();
        });
        footerPanel.add(btnViewAll);

        popup.add(footerPanel);

        return popup;
    }

    private JPanel createNotificationItem(Notification notification, JPopupMenu popup) {
        JPanel item = new JPanel(new BorderLayout(10, 5));
        item.setBackground(notification.isRead() ? Color.WHITE : new Color(240, 247, 255));
        item.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icon based on type (colored circle)
        JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.Y_AXIS));
        iconPanel.setBackground(item.getBackground());

        JLabel iconLabel = new JLabel("●");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        // Parse hex color from database
        try {
            if (notification.getIconColor() != null) {
                iconLabel.setForeground(Color.decode(notification.getIconColor()));
            } else {
                iconLabel.setForeground(StyleUtil.PRIMARY);
            }
        } catch (NumberFormatException e) {
            iconLabel.setForeground(StyleUtil.PRIMARY);
        }

        iconPanel.add(iconLabel);
        item.add(iconPanel, BorderLayout.WEST);

        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(item.getBackground());

        JLabel titleLabel = new JLabel(notification.getTitle());
        titleLabel.setFont(notification.isRead() ? StyleUtil.FONT_BODY : StyleUtil.FONT_BUTTON);
        titleLabel.setForeground(StyleUtil.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel messageLabel = new JLabel("<html>" + notification.getMessage() + "</html>");
        messageLabel.setFont(StyleUtil.FONT_SMALL);
        messageLabel.setForeground(StyleUtil.TEXT_SECONDARY);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel timeLabel = new JLabel(notification.getTimeAgo());
        timeLabel.setFont(StyleUtil.FONT_SMALL);
        timeLabel.setForeground(StyleUtil.TEXT_SECONDARY);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(3));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(3));
        contentPanel.add(timeLabel);

        item.add(contentPanel, BorderLayout.CENTER);

        // Hover effect
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(StyleUtil.PRIMARY_LIGHT);
                iconPanel.setBackground(StyleUtil.PRIMARY_LIGHT);
                contentPanel.setBackground(StyleUtil.PRIMARY_LIGHT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Color bg = notification.isRead() ? Color.WHITE : new Color(240, 247, 255);
                item.setBackground(bg);
                iconPanel.setBackground(bg);
                contentPanel.setBackground(bg);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleNotificationClick(notification);
                popup.setVisible(false);
            }
        });

        return item;
    }

    private void handleNotificationClick(Notification notification) {
        // Mark as read
        if (!notification.isRead()) {
            notificationDao.markAsRead(notification.getNotificationId());
            refreshUnreadCount();
        }

        // Navigate to related task/project (implement based on your navigation)
        if (notification.getRelatedTaskId() != null) {
            System.out.println("Navigate to Task ID: " + notification.getRelatedTaskId());
            // TODO: Add navigation logic
            // parentFrame.navigateToTask(notification.getRelatedTaskId());
        } else if (notification.getRelatedProjectId() != null) {
            System.out.println("Navigate to Project ID: " + notification.getRelatedProjectId());
            // TODO: Add navigation logic
            // parentFrame.navigateToProject(notification.getRelatedProjectId());
        }
    }

    private void markAllAsRead() {
        notificationDao.markAllAsRead(userId);
        refreshUnreadCount();
        JOptionPane.showMessageDialog(this,
                "Tüm bildirimler okundu olarak işaretlendi",
                "Başarılı",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void openNotificationsDialog() {
        if (onViewAllClicked != null) {
            onViewAllClicked.run();
        } else {
            // Fallback to default dialog
            com.ahmet.tpm.components.NotificationsDialog dialog =
                    new com.ahmet.tpm.components.NotificationsDialog(parentFrame, userId);
            dialog.setVisible(true);
            refreshUnreadCount();
        }
    }

    public void setOnViewAllClicked(Runnable callback) {
        this.onViewAllClicked = callback;
    }

    public void refreshUnreadCount() {
        unreadCount = notificationDao.getUnreadCount(userId);
        updateBadge();
    }

    public void refreshBadge() {
        refreshUnreadCount();
    }

    private void updateBadge() {
        if (unreadCount > 0) {
            lblBadge.setText(String.valueOf(unreadCount));
            lblBadge.setVisible(true);
        } else {
            lblBadge.setVisible(false);
        }
    }

    private void startAutoRefresh() {
        // Refresh every 30 seconds
        refreshTimer = new Timer(30000, e -> refreshUnreadCount());
        refreshTimer.start();
    }

    public void stopAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }
}