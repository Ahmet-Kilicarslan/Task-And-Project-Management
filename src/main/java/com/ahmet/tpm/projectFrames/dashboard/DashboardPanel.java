package com.ahmet.tpm.projectFrames.dashboard;

import com.ahmet.tpm.dao.ProjectDao;
import com.ahmet.tpm.dao.ProjectMemberDao;
import com.ahmet.tpm.dao.ProjectStatusDao;
import com.ahmet.tpm.models.Project;
import com.ahmet.tpm.models.ProjectStatus;
import com.ahmet.tpm.projectFrames.MainFrame;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dashboard Panel for Project Management System
 *
 * Features:
 * A. Summary Cards - Quick statistics overview
 * B. Recent Projects - Latest projects table
 * C. Project Status Distribution - Visual status breakdown
 */
public class DashboardPanel extends JPanel {

    private MainFrame mainFrame;

    // DAOs
    private ProjectDao projectDao;
    private ProjectStatusDao statusDao;
    private ProjectMemberDao memberDao;

    // UI Components for Summary Cards (A)
    private JLabel lblTotalProjects;
    private JLabel lblActiveProjects;
    private JLabel lblCompletedProjects;
    private JLabel lblTotalMembers;

    // UI Components for Recent Projects (B)
    private JTable recentProjectsTable;
    private DefaultTableModel tableModel;

    // UI Components for Status Distribution (C)
    private JPanel statusDistributionPanel;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.projectDao = new ProjectDao();
        this.statusDao = new ProjectStatusDao();
        this.memberDao = new ProjectMemberDao();

        setLayout(new BorderLayout());
        setBackground(StyleUtil.BACKGROUND);

        initializeUI();
        loadDashboardData();
    }

    /**
     * Initialize all UI components
     */
    private void initializeUI() {
        // Main content panel with padding
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(StyleUtil.BACKGROUND);
        contentPanel.setBorder(StyleUtil.createPaddingBorder(30, 40, 30, 40));

        // Title
        JLabel titleLabel = ComponentFactory.createTitleLabel("📊 DASHBOARD");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(30));

        // A. SUMMARY CARDS
        JPanel summaryPanel = createSummaryCardsPanel();
        summaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(summaryPanel);
        contentPanel.add(Box.createVerticalStrut(30));

        // B. RECENT PROJECTS TABLE
        JPanel recentProjectsPanel = createRecentProjectsPanel();
        recentProjectsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(recentProjectsPanel);
        contentPanel.add(Box.createVerticalStrut(30));

        // C. PROJECT STATUS DISTRIBUTION
        JPanel statusPanel = createStatusDistributionPanel();
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(statusPanel);

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ==================== A. SUMMARY CARDS ====================

    /**
     * Creates the summary cards panel (A)
     * Shows: Total Projects, Active, Completed, Total Members
     */
    private JPanel createSummaryCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setBackground(StyleUtil.BACKGROUND);
        panel.setMaximumSize(new Dimension(1100, 140));

        // Card 1: Total Projects
        JPanel card1 = createSummaryCard(
                "Total Projects",
                "0",
                StyleUtil.PRIMARY,
                "📁"
        );
        lblTotalProjects = (JLabel) ((JPanel) card1.getComponent(1)).getComponent(0);

        // Card 2: Active Projects
        JPanel card2 = createSummaryCard(
                "Active Projects",
                "0",
                StyleUtil.INFO,
                "🚀"
        );
        lblActiveProjects = (JLabel) ((JPanel) card2.getComponent(1)).getComponent(0);

        // Card 3: Completed Projects
        JPanel card3 = createSummaryCard(
                "Completed",
                "0",
                StyleUtil.SUCCESS,
                "✅"
        );
        lblCompletedProjects = (JLabel) ((JPanel) card3.getComponent(1)).getComponent(0);

        // Card 4: Total Members
        JPanel card4 = createSummaryCard(
                "Team Members",
                "0",
                StyleUtil.WARNING,
                "👥"
        );
        lblTotalMembers = (JLabel) ((JPanel) card4.getComponent(1)).getComponent(0);

        panel.add(card1);
        panel.add(card2);
        panel.add(card3);
        panel.add(card4);

        return panel;
    }

    /**
     * Creates a single summary card
     *
     * @param title Card title
     * @param value Initial value
     * @param color Card accent color
     * @param icon Emoji icon
     */
    private JPanel createSummaryCard(String title, String value, Color color, String icon) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Icon panel (left)
        JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.Y_AXIS));
        iconPanel.setBackground(Color.WHITE);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconPanel.add(lblIcon);

        // Content panel (center)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblValue.setForeground(color);
        lblValue.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = ComponentFactory.createBodyLabel(title);
        lblTitle.setForeground(StyleUtil.TEXT_SECONDARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(lblValue);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(lblTitle);

        card.add(iconPanel, BorderLayout.WEST);
        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    // ==================== B. RECENT PROJECTS TABLE ====================

    /**
     * Creates the recent projects panel (B)
     * Shows latest 5 projects in a table
     */
    private JPanel createRecentProjectsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(StyleUtil.BACKGROUND);
        panel.setMaximumSize(new Dimension(1100, 350));

        // Header with title and "View All" button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(StyleUtil.BACKGROUND);

        JLabel titleLabel = ComponentFactory.createHeadingLabel("📋 Recent Projects");
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton btnViewAll = ComponentFactory.createSecondaryButton("View All →");
        btnViewAll.addActionListener(e -> mainFrame.showProjects());
        headerPanel.add(btnViewAll, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"Project Name", "Status", "Start Date", "Deadline", "Progress"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recentProjectsTable = new JTable(tableModel);
        recentProjectsTable.setFont(StyleUtil.FONT_BODY);
        recentProjectsTable.setRowHeight(40);
        recentProjectsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentProjectsTable.getTableHeader().setFont(StyleUtil.FONT_BUTTON);
        recentProjectsTable.getTableHeader().setBackground(StyleUtil.PRIMARY_LIGHT);
        recentProjectsTable.setBackground(Color.WHITE);
        recentProjectsTable.setForeground(StyleUtil.TEXT_PRIMARY);
        recentProjectsTable.setGridColor(StyleUtil.BORDER);
        recentProjectsTable.setSelectionBackground(StyleUtil.PRIMARY_LIGHT);

        // Column widths
        recentProjectsTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        recentProjectsTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        recentProjectsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        recentProjectsTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        recentProjectsTable.getColumnModel().getColumn(4).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(recentProjectsTable);
        scrollPane.setBorder(StyleUtil.createLineBorder());
        scrollPane.setPreferredSize(new Dimension(1100, 250));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ==================== C. PROJECT STATUS DISTRIBUTION ====================

    /**
     * Creates the status distribution panel (C)
     * Shows project count by status with visual bars
     */
    private JPanel createStatusDistributionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(StyleUtil.BACKGROUND);
        panel.setMaximumSize(new Dimension(1100, 300));

        // Title
        JLabel titleLabel = ComponentFactory.createHeadingLabel("📊 Project Status Distribution");
        panel.add(titleLabel, BorderLayout.NORTH);

        // Status bars container
        statusDistributionPanel = new JPanel();
        statusDistributionPanel.setLayout(new BoxLayout(statusDistributionPanel, BoxLayout.Y_AXIS));
        statusDistributionPanel.setBackground(Color.WHITE);
        statusDistributionPanel.setBorder(BorderFactory.createCompoundBorder(
                StyleUtil.createLineBorder(),
                StyleUtil.createPaddingBorder(20)
        ));

        panel.add(statusDistributionPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates a single status bar
     *
     * @param statusName Name of the status
     * @param count Number of projects
     * @param color Bar color
     * @param maxCount Maximum count for percentage calculation
     */
    private JPanel createStatusBar(String statusName, int count, Color color, int maxCount) {
        JPanel barPanel = new JPanel(new BorderLayout(15, 0));
        barPanel.setBackground(Color.WHITE);
        barPanel.setMaximumSize(new Dimension(1050, 50));
        barPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Status name label (left)
        JLabel lblStatus = new JLabel(statusName);
        lblStatus.setFont(StyleUtil.FONT_BODY);
        lblStatus.setForeground(StyleUtil.TEXT_PRIMARY);
        lblStatus.setPreferredSize(new Dimension(150, 30));
        barPanel.add(lblStatus, BorderLayout.WEST);

        // Progress bar (center)
        JProgressBar progressBar = new JProgressBar(0, maxCount);
        progressBar.setValue(count);
        progressBar.setStringPainted(true);
        progressBar.setString(count + " projects");
        progressBar.setFont(StyleUtil.FONT_BODY);
        progressBar.setForeground(color);
        progressBar.setBackground(StyleUtil.BACKGROUND);
        progressBar.setPreferredSize(new Dimension(700, 30));
        barPanel.add(progressBar, BorderLayout.CENTER);

        // Percentage label (right)
        int percentage = maxCount > 0 ? (count * 100 / maxCount) : 0;
        JLabel lblPercentage = new JLabel(percentage + "%");
        lblPercentage.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPercentage.setForeground(color);
        lblPercentage.setPreferredSize(new Dimension(60, 30));
        barPanel.add(lblPercentage, BorderLayout.EAST);

        return barPanel;
    }

    // ==================== DATA LOADING ====================

    /**
     * Load all dashboard data from database
     * This method refreshes all components
     */
    public void loadDashboardData() {
        try {
            // A. Load Summary Card Data
            loadSummaryData();

            // B. Load Recent Projects
            loadRecentProjects();

            // C. Load Status Distribution
            loadStatusDistribution();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading dashboard data: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Load data for summary cards (A)
     */
    private void loadSummaryData() {
        // Total Projects
        int totalProjects = projectDao.count();
        lblTotalProjects.setText(String.valueOf(totalProjects));

        // Active Projects (status_id = 2 for "In Progress")
        int activeProjects = projectDao.countByStatus(2);
        lblActiveProjects.setText(String.valueOf(activeProjects));

        // Completed Projects (status_id = 3 for "Completed")
        int completedProjects = projectDao.countByStatus(3);
        lblCompletedProjects.setText(String.valueOf(completedProjects));

        // Total Members (count all unique project members)
        int totalMembers = memberDao.countProjectsForUser(mainFrame.getCurrentUserId());
        // Better: Get total unique members across all projects
        // For now using a simple count
        lblTotalMembers.setText(String.valueOf(totalMembers));
    }

    /**
     * Load recent projects for table (B)
     */
    private void loadRecentProjects() {
        tableModel.setRowCount(0); // Clear existing rows

        List<Project> allProjects = projectDao.findAll();

        // Take first 5 projects (most recent by created_at)
        int limit = Math.min(5, allProjects.size());
        for (int i = 0; i < limit; i++) {
            Project project = allProjects.get(i);

            // Get status name
            String statusName = "Unknown";
            ProjectStatus status = statusDao.findById(project.getStatusId());
            if (status != null) {
                statusName = status.getStatusName();
            }

            // Format dates
            String startDate = project.getStartDate() != null ?
                    project.getStartDate().toLocalDate().toString() : "-";
            String deadline = project.getDeadline() != null ?
                    project.getDeadline().toLocalDate().toString() : "-";

            // Calculate progress (placeholder - can be improved)
            String progress = calculateProgress(project);

            Object[] row = {
                    project.getProjectName(),
                    statusName,
                    startDate,
                    deadline,
                    progress
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Calculate project progress (simplified)
     * In real scenario, you would calculate based on tasks
     */
    private String calculateProgress(Project project) {
        // Simple logic: Use status as indicator
        switch (project.getStatusId()) {
            case 1: // Planning
                return "10%";
            case 2: // In Progress
                return "50%";
            case 3: // Completed
                return "100%";
            case 4: // Cancelled
                return "0%";
            default:
                return "N/A";
        }
    }

    /**
     * Load status distribution (C)
     */
    private void loadStatusDistribution() {
        statusDistributionPanel.removeAll(); // Clear existing bars

        List<ProjectStatus> allStatuses = statusDao.findAll();
        int totalProjects = projectDao.count();

        // Find max count for scaling bars
        int maxCount = 0;
        for (ProjectStatus status : allStatuses) {
            int count = projectDao.countByStatus(status.getStatusId());
            if (count > maxCount) {
                maxCount = count;
            }
        }

        // Create a bar for each status
        Color[] colors = {StyleUtil.INFO, StyleUtil.WARNING, StyleUtil.SUCCESS, StyleUtil.DANGER};
        int colorIndex = 0;

        for (ProjectStatus status : allStatuses) {
            int count = projectDao.countByStatus(status.getStatusId());
            Color color = colors[colorIndex % colors.length];

            JPanel statusBar = createStatusBar(
                    status.getStatusName(),
                    count,
                    color,
                    totalProjects // Use total as max for percentage
            );
            statusBar.setAlignmentX(Component.LEFT_ALIGNMENT);
            statusDistributionPanel.add(statusBar);

            colorIndex++;
        }

        // Refresh UI
        statusDistributionPanel.revalidate();
        statusDistributionPanel.repaint();
    }

    /**
     * Public method to refresh dashboard
     * Call this when data changes
     */
    public void refreshDashboard() {
        loadDashboardData();
    }
}