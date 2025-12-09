package com.ahmet.tpm.taskFrames.dashboard;

import com.ahmet.tpm.dao.TaskDao;
import com.ahmet.tpm.dao.TaskMemberDao;
import com.ahmet.tpm.taskFrames.TaskMainFrame;
import com.ahmet.tpm.models.Task;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Dashboard panel for Task Management System
 * Shows task statistics and recent tasks
 */
public class TaskDashboard extends JPanel {

    private TaskMainFrame mainFrame;
    private TaskDao taskDao;
    private TaskMemberDao taskMemberDao;

    // Statistics labels
    private JLabel lblMyTasks;
    private JLabel lblTodoTasks;
    private JLabel lblInProgressTasks;
    private JLabel lblOverdueTasks;

    // Recent tasks table
    private JTable recentTasksTable;
    private DefaultTableModel tableModel;

    public TaskDashboard(TaskMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.taskDao = new TaskDao();
        this.taskMemberDao = new TaskMemberDao();

        setLayout(new BorderLayout());
        setBackground(StyleUtil.BACKGROUND);

        initializeUI();
        loadStatistics();
        loadRecentTasks();
    }

    private void initializeUI() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(StyleUtil.BACKGROUND);
        contentPanel.setBorder(StyleUtil.createPaddingBorder(30, 40, 30, 40));

        // Welcome section
        JPanel welcomePanel = createWelcomePanel();
        welcomePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(welcomePanel);
        contentPanel.add(Box.createVerticalStrut(30));

        // Statistics cards
        JPanel statsPanel = createStatsPanel();
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(statsPanel);
        contentPanel.add(Box.createVerticalStrut(30));

        // Recent tasks section
        JPanel recentTasksPanel = createRecentTasksPanel();
        recentTasksPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(recentTasksPanel);

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(StyleUtil.BACKGROUND);
        panel.setMaximumSize(new Dimension(1100, 100));

        JLabel lblWelcome = new JLabel("Welcome back, " + mainFrame.getCurrentUsername() + "!");
        lblWelcome.setFont(StyleUtil.FONT_LARGE);
        lblWelcome.setForeground(StyleUtil.TEXT_PRIMARY);
        lblWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Here's what's happening with your tasks today");
        lblSubtitle.setFont(StyleUtil.FONT_BODY);
        lblSubtitle.setForeground(StyleUtil.TEXT_SECONDARY);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblWelcome);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblSubtitle);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setBackground(StyleUtil.BACKGROUND);
        panel.setMaximumSize(new Dimension(1100, 150));

        // My Tasks Card
        JPanel myTasksCard = createStatCard("My Tasks", "0", StyleUtil.PRIMARY, "All assigned to you");
        lblMyTasks = (JLabel) myTasksCard.getComponent(2);

        // TODO Tasks Card
        JPanel todoCard = createStatCard("To Do", "0", StyleUtil.INFO, "Pending tasks");
        lblTodoTasks = (JLabel) todoCard.getComponent(2);

        // In Progress Card
        JPanel inProgressCard = createStatCard("In Progress", "0", StyleUtil.WARNING, "Currently working");
        lblInProgressTasks = (JLabel) inProgressCard.getComponent(2);

        // Overdue Card
        JPanel overdueCard = createStatCard("Overdue", "0", StyleUtil.DANGER, "Past due date");
        lblOverdueTasks = (JLabel) overdueCard.getComponent(2);

        panel.add(myTasksCard);
        panel.add(todoCard);
        panel.add(inProgressCard);
        panel.add(overdueCard);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color, String subtitle) {
        JPanel card = ComponentFactory.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(StyleUtil.FONT_SUBHEADING);
        lblTitle.setForeground(StyleUtil.TEXT_SECONDARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 42));
        lblValue.setForeground(color);
        lblValue.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(StyleUtil.FONT_SMALL);
        lblSubtitle.setForeground(StyleUtil.TEXT_SECONDARY);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(15));
        card.add(lblValue);
        card.add(Box.createVerticalStrut(10));
        card.add(lblSubtitle);

        return card;
    }

    private JPanel createRecentTasksPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(StyleUtil.BACKGROUND);
        panel.setMaximumSize(new Dimension(1100, 400));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(StyleUtil.BACKGROUND);

        JLabel titleLabel = ComponentFactory.createHeadingLabel("Recent Tasks");
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton btnViewAll = ComponentFactory.createSecondaryButton("View All Tasks");
        btnViewAll.addActionListener(e -> {
            // Navigate to Tasks module
            Container parent = this.getParent();
            while (parent != null && !(parent instanceof TaskMainFrame)) {
                parent = parent.getParent();
            }
            if (parent instanceof TaskMainFrame) {
                // Trigger navigation - will be handled by TaskMainFrame
                System.out.println("Navigate to Tasks module");
            }
        });
        headerPanel.add(btnViewAll, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"Task ID", "Task Name", "Project", "Status", "Priority", "Due Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recentTasksTable = new JTable(tableModel);
        recentTasksTable.setFont(StyleUtil.FONT_BODY);
        recentTasksTable.setRowHeight(35);
        recentTasksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentTasksTable.getTableHeader().setFont(StyleUtil.FONT_BUTTON);
        recentTasksTable.getTableHeader().setBackground(StyleUtil.PRIMARY_LIGHT);
        recentTasksTable.setBackground(Color.WHITE);
        recentTasksTable.setForeground(StyleUtil.TEXT_PRIMARY);
        recentTasksTable.setGridColor(StyleUtil.BORDER);

        // Column widths
        recentTasksTable.getColumnModel().getColumn(0).setPreferredWidth(70);   // ID
        recentTasksTable.getColumnModel().getColumn(1).setPreferredWidth(250);  // Name
        recentTasksTable.getColumnModel().getColumn(2).setPreferredWidth(150);  // Project
        recentTasksTable.getColumnModel().getColumn(3).setPreferredWidth(120);  // Status
        recentTasksTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Priority
        recentTasksTable.getColumnModel().getColumn(5).setPreferredWidth(100);  // Due Date

        JScrollPane scrollPane = new JScrollPane(recentTasksTable);
        scrollPane.setBorder(StyleUtil.createLineBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadStatistics() {
        int userId = mainFrame.getCurrentUserId();

        // My Tasks (assigned to current user)
        List<Integer> myTaskIds = taskMemberDao.getTaskIdsForUser(userId);
        lblMyTasks.setText(String.valueOf(myTaskIds.size()));

        // Count tasks by status
        int todoCount = 0;
        int inProgressCount = 0;
        int overdueCount = 0;

        for (Integer taskId : myTaskIds) {
            Task task = taskDao.findById(taskId);
            if (task != null) {
                // TODO = status_id 1
                if (task.getStatusId() == 1) {
                    todoCount++;
                }
                // IN PROGRESS = status_id 2
                else if (task.getStatusId() == 2) {
                    inProgressCount++;
                }

                // Check if overdue (due date < today AND not done)
                if (task.getDueDate() != null &&
                        task.getDueDate().isBefore(LocalDate.now()) &&
                        task.getStatusId() != 4) {  // 4 = DONE
                    overdueCount++;
                }
            }
        }

        lblTodoTasks.setText(String.valueOf(todoCount));
        lblInProgressTasks.setText(String.valueOf(inProgressCount));
        lblOverdueTasks.setText(String.valueOf(overdueCount));
    }

    private void loadRecentTasks() {
        tableModel.setRowCount(0);

        int userId = mainFrame.getCurrentUserId();
        List<Integer> myTaskIds = taskMemberDao.getTaskIdsForUser(userId);

        // Load up to 10 recent tasks
        int count = 0;
        for (Integer taskId : myTaskIds) {
            if (count >= 10) break;

            Task task = taskDao.findById(taskId);
            if (task != null) {
                Object[] row = {
                        task.getTaskId(),
                        task.getTaskName(),
                        "Project " + task.getProjectId(),  // TODO: Load project name
                        getStatusName(task.getStatusId()),
                        getPriorityName(task.getPriorityId()),
                        task.getDueDate() != null ? task.getDueDate().toString() : "-"
                };
                tableModel.addRow(row);
                count++;
            }
        }
    }

    private String getStatusName(int statusId) {
        return switch (statusId) {
            case 1 -> "TODO";
            case 2 -> "IN PROGRESS";
            case 3 -> "IN REVIEW";
            case 4 -> "DONE";
            default -> "Unknown";
        };
    }

    private String getPriorityName(int priorityId) {
        return switch (priorityId) {
            case 1 -> "LOW";
            case 2 -> "MEDIUM";
            case 3 -> "HIGH";
            case 4 -> "CRITICAL";
            default -> "Unknown";
        };
    }

    public void refreshData() {
        loadStatistics();
        loadRecentTasks();
    }
}