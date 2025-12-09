package com.ahmet.tpm.components;

import com.ahmet.tpm.service.NotificationService;
import com.ahmet.tpm.utils.StyleUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Notification button with badge count for MainFrame header
 */
public class NotificationButton extends JButton {

    private int userId;
    private NotificationService notificationService;
    private JLabel badgeLabel;
    private JPopupMenu notificationPopup;
    private NotificationPanel notificationPanel;
    private Timer refreshTimer;

    public NotificationButton(int userId) {
        this.userId = userId;
        this.notificationService = new NotificationService();

        initializeButton();
        initializePopup();
        startAutoRefresh();
        updateBadge();
    }

    private void initializeButton() {
        setText("Notifications");
        setFont(StyleUtil.FONT_BODY);
        setForeground(StyleUtil.TEXT_PRIMARY);
        setBackground(StyleUtil.SURFACE);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(140, 40));
        setLayout(null); // For absolute positioning of badge

        // Badge label for unread count
        badgeLabel = new JLabel("0");
        badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badgeLabel.setForeground(Color.WHITE);
        badgeLabel.setBackground(StyleUtil.DANGER);
        badgeLabel.setOpaque(true);
        badgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        badgeLabel.setBounds(100, 5, 22, 22);
        badgeLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        badgeLabel.setVisible(false);
        add(badgeLabel);

        // Hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(StyleUtil.BACKGROUND);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(StyleUtil.SURFACE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                toggleNotificationPopup();
            }
        });
    }

    private void initializePopup() {
        notificationPopup = new JPopupMenu();
        notificationPopup.setBorder(BorderFactory.createLineBorder(StyleUtil.BORDER, 1));

        notificationPanel = new NotificationPanel(userId);
        notificationPopup.add(notificationPanel);
    }

    private void toggleNotificationPopup() {
        if (notificationPopup.isVisible()) {
            notificationPopup.setVisible(false);
        } else {
            notificationPanel.loadNotifications(); // Refresh before showing
            notificationPopup.show(this, -210, getHeight() + 5); // Position below button
            updateBadge();
        }
    }

    public void updateBadge() {
        int unreadCount = notificationService.getUnreadCount(userId);

        if (unreadCount > 0) {
            badgeLabel.setText(String.valueOf(Math.min(unreadCount, 99))); // Max 99
            badgeLabel.setVisible(true);
        } else {
            badgeLabel.setVisible(false);
        }

        revalidate();
        repaint();
    }

    /**
     * Start auto-refresh timer to update badge every 30 seconds
     */
    private void startAutoRefresh() {
        refreshTimer = new Timer(30000, e -> updateBadge()); // 30 seconds
        refreshTimer.start();
    }

    /**
     * Stop auto-refresh timer
     */
    public void stopAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

    /**
     * Force refresh notifications
     */
    public void refresh() {
        updateBadge();
        if (notificationPanel != null) {
            notificationPanel.loadNotifications();
        }
    }
}