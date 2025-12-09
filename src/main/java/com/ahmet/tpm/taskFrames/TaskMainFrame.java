package com.ahmet.tpm.taskFrames;

import com.ahmet.tpm.taskFrames.tasks.TasksModulePanel;
import com.ahmet.tpm.taskFrames.dashboard.TaskDashboard;
import com.ahmet.tpm.taskFrames.profile.TaskProfilePanel;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;
import com.ahmet.tpm.utils.UIHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Main Frame for Task Management System
 * Standalone - can run independently from Project Management System
 */
public class TaskMainFrame extends JFrame {

    // Components
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton btnDashboard, btnTasks, btnProfile;

    // User info
    private String currentUsername;
    private int currentUserId;

    public TaskMainFrame(String username, int userId) {
        this.currentUsername = username;
        this.currentUserId = userId;

        initializeFrame();
        createNavBar();
        createContentArea();
        showDashboard();
    }

    private void initializeFrame() {
        setTitle("Task Management System");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        UIHelper.centerFrame(this);
        setLayout(new BorderLayout());
        getContentPane().setBackground(StyleUtil.BACKGROUND);
    }

    private void createNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(StyleUtil.SURFACE);
        navBar.setPreferredSize(new Dimension(getWidth(), StyleUtil.NAVBAR_HEIGHT));
        navBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, StyleUtil.BORDER));

        // Left - Logo (Green theme for Task system)
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        leftPanel.setBackground(StyleUtil.SURFACE);

        JLabel iconLabel = new JLabel("✓");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        iconLabel.setForeground(StyleUtil.SUCCESS);  // Green

        JLabel appName = new JLabel("TaskFlow");
        appName.setFont(StyleUtil.FONT_TITLE);
        appName.setForeground(StyleUtil.SUCCESS);  // Green

        leftPanel.add(iconLabel);
        leftPanel.add(appName);

        // Center - Nav buttons (Only 3: Dashboard, Tasks, Profile)
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 15));
        centerPanel.setBackground(StyleUtil.SURFACE);

        btnDashboard = ComponentFactory.createNavButton("Dashboard", true);
        btnTasks = ComponentFactory.createNavButton("Tasks", false);
        btnProfile = ComponentFactory.createNavButton("Profile", false);

        // Click handlers
        btnDashboard.addActionListener(e -> showDashboard());
        btnTasks.addActionListener(e -> showTasks());
        btnProfile.addActionListener(e -> showProfile());

        // Hover effects
        UIHelper.addNavButtonHover(btnDashboard, btnDashboard, btnTasks, btnProfile);
        UIHelper.addNavButtonHover(btnTasks, btnDashboard, btnTasks, btnProfile);
        UIHelper.addNavButtonHover(btnProfile, btnDashboard, btnTasks, btnProfile);

        centerPanel.add(btnDashboard);
        centerPanel.add(btnTasks);
        centerPanel.add(btnProfile);

        // Right - User info
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        rightPanel.setBackground(StyleUtil.SURFACE);

        JLabel userLabel = ComponentFactory.createBodyLabel(currentUsername);
        userLabel.setForeground(StyleUtil.TEXT_PRIMARY);

        JButton btnLogout = ComponentFactory.createDangerButton("Logout");
        btnLogout.addActionListener(e -> logout());

        rightPanel.add(userLabel);
        rightPanel.add(btnLogout);

        // Assemble
        navBar.add(leftPanel, BorderLayout.WEST);
        navBar.add(centerPanel, BorderLayout.CENTER);
        navBar.add(rightPanel, BorderLayout.EAST);

        add(navBar, BorderLayout.NORTH);
    }

    private void createContentArea() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(StyleUtil.BACKGROUND);
        contentPanel.setBorder(StyleUtil.createPaddingBorder(
                StyleUtil.CONTENT_PADDING,
                StyleUtil.CONTENT_PADDING,
                StyleUtil.CONTENT_PADDING,
                StyleUtil.CONTENT_PADDING
        ));

        // Create panels
        TaskDashboard dashboardPanel = new TaskDashboard(this);
        TasksModulePanel tasksModule = new TasksModulePanel(this);
        TaskProfilePanel profilePanel = new TaskProfilePanel(this);

        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(tasksModule, "TASKS");
        contentPanel.add(profilePanel, "PROFILE");

        add(contentPanel, BorderLayout.CENTER);
    }

    // Navigation methods
    private void showDashboard() {
        cardLayout.show(contentPanel, "DASHBOARD");
        UIHelper.updateNavButtonStates(btnDashboard, btnDashboard, btnTasks, btnProfile);
    }

    private void showTasks() {
        cardLayout.show(contentPanel, "TASKS");
        UIHelper.updateNavButtonStates(btnTasks, btnDashboard, btnTasks, btnProfile);
    }

    private void showProfile() {
        cardLayout.show(contentPanel, "PROFILE");
        UIHelper.updateNavButtonStates(btnProfile, btnDashboard, btnTasks, btnProfile);
    }

    private void logout() {
        if (UIHelper.showConfirmDialog(this,
                "Are you sure you want to logout from Task System?",
                "Confirm Logout")) {
            dispose();
            System.out.println("Task System - User logged out: " + currentUsername);

            // Open login frame again
            SwingUtilities.invokeLater(() -> {
                com.ahmet.tpm.taskFrames.auth.TaskLoginFrame loginFrame =
                        new com.ahmet.tpm.taskFrames.auth.TaskLoginFrame();
                loginFrame.setVisible(true);
            });
        }
    }

    // Getters
    public String getCurrentUsername() { return currentUsername; }
    public int getCurrentUserId() { return currentUserId; }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            // For testing - normally opened from TaskLoginFrame
            TaskMainFrame frame = new TaskMainFrame("TestUser", 1);
            frame.setVisible(true);
        });
    }
}