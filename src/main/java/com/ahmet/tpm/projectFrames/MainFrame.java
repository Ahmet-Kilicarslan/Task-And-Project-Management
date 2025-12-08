package com.ahmet.tpm.projectFrames;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;
import com.ahmet.tpm.utils.UIHelper;
import com.ahmet.tpm.projectFrames.projects.ProjectsModulePanel;
import com.ahmet.tpm.projectFrames.profile.ProfilePanel;



import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    // Components
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton btnDashboard, btnProjects, btnTasks, btnProfile;

    // User info
    private String currentUsername;
    private int currentUserId;

    public MainFrame(String username, int userId) {
        this.currentUsername = username;
        this.currentUserId = userId;

        initializeFrame();
        createNavBar();
        createContentArea();
        showDashboard();
    }

    private void initializeFrame() {
        setTitle("Project & Task Management System");
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

        // Left - Logo
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        leftPanel.setBackground(StyleUtil.SURFACE);
        leftPanel.add(new JLabel("📋"));
        JLabel appName = new JLabel("TaskFlow");
        appName.setFont(StyleUtil.FONT_TITLE);
        appName.setForeground(StyleUtil.PRIMARY);
        leftPanel.add(appName);

        // Center - Nav buttons
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 15));
        centerPanel.setBackground(StyleUtil.SURFACE);

        btnDashboard = ComponentFactory.createNavButton("📊 Dashboard", true);
        btnProjects = ComponentFactory.createNavButton("📁 Projects", false);
        btnTasks = ComponentFactory.createNavButton("✅ Tasks", false);
        btnProfile = ComponentFactory.createNavButton("👤 Profile", false);

        // Add click handlers
        btnDashboard.addActionListener(e -> showDashboard());
        btnProjects.addActionListener(e -> showProjects());
        btnTasks.addActionListener(e -> showTasks());
        btnProfile.addActionListener(e -> showProfile());

        // Add hover effects
        UIHelper.addNavButtonHover(btnDashboard, btnDashboard, btnProjects, btnTasks, btnProfile);
        UIHelper.addNavButtonHover(btnProjects, btnDashboard, btnProjects, btnTasks, btnProfile);
        UIHelper.addNavButtonHover(btnTasks, btnDashboard, btnProjects, btnTasks, btnProfile);
        UIHelper.addNavButtonHover(btnProfile, btnDashboard, btnProjects, btnTasks, btnProfile);

        centerPanel.add(btnDashboard);
        centerPanel.add(btnProjects);
        centerPanel.add(btnTasks);
        centerPanel.add(btnProfile);

        // Right - User info
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        rightPanel.setBackground(StyleUtil.SURFACE);

        JLabel userLabel = ComponentFactory.createBodyLabel("👋 " + currentUsername);
        userLabel.setForeground(StyleUtil.TEXT_PRIMARY);
        JButton btnLogout = ComponentFactory.createDangerButton("🚪 Logout");
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

        // Create actual module panels
        ProjectsModulePanel projectsModule = new ProjectsModulePanel(this);

        // Add to CardLayout with identifier "PROJECTS"
        contentPanel.add(projectsModule, "PROJECTS");

        // Placeholders
        contentPanel.add(createPlaceholder("📊 Dashboard", "Welcome back!"), "DASHBOARD");
        //contentPanel.add(createPlaceholder("📁 Projects", "Manage projects"), "PROJECTS");
        contentPanel.add(createPlaceholder("✅ Tasks", "View tasks"), "TASKS");
        contentPanel.add(createPlaceholder("👤 Profile", "Your profile"), "PROFILE");

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createPlaceholder(String title, String subtitle) {
        JPanel panel = ComponentFactory.createContentPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel titleLabel = ComponentFactory.createTitleLabel(title);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = ComponentFactory.createBodyLabel(subtitle);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtitleLabel);

        return panel;
    }

    // Navigation methods
    private void showDashboard() {
        cardLayout.show(contentPanel, "DASHBOARD");
        UIHelper.updateNavButtonStates(btnDashboard, btnDashboard, btnProjects, btnTasks, btnProfile);
    }

    private void showProjects() {
        cardLayout.show(contentPanel, "PROJECTS");
        UIHelper.updateNavButtonStates(btnProjects, btnDashboard, btnProjects, btnTasks, btnProfile);
    }

    private void showTasks() {
        cardLayout.show(contentPanel, "TASKS");
        UIHelper.updateNavButtonStates(btnTasks, btnDashboard, btnProjects, btnTasks, btnProfile);
    }

    private void showProfile() {
        cardLayout.show(contentPanel, "PROFILE");
        UIHelper.updateNavButtonStates(btnProfile, btnDashboard, btnProjects, btnTasks, btnProfile);
    }

    private void logout() {
        if (UIHelper.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout")) {
            dispose();
            System.out.println("User logged out: " + currentUsername);
            System.exit(0);
        }
    }

    // Getters
    public String getCurrentUsername() { return currentUsername; }
    public int getCurrentUserId() { return currentUserId; }

    // Replace panels later
    public void setDashboardPanel(JPanel panel) {
        contentPanel.remove(0);
        contentPanel.add(panel, "DASHBOARD", 0);
    }

    public static void main(String[] args) {
        UIHelper.setSystemLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame("Ahmet", 1);
            frame.setVisible(true);
        });
    }
}