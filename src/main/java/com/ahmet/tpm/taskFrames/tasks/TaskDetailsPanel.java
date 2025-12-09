package com.ahmet.tpm.taskFrames.tasks;

import com.ahmet.tpm.dao.*;
import com.ahmet.tpm.taskFrames.TaskMainFrame;
import com.ahmet.tpm.models.*;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;
import com.ahmet.tpm.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class TaskDetailsPanel extends JPanel {

    private TasksModulePanel parentModule;
    private TaskMainFrame mainFrame;

    // DAOs
    private TaskDao taskDao;
    private ProjectDao projectDao;
    private TaskStatusDao taskStatusDao;
    private TaskPriorityDao taskPriorityDao;
    private UserDao userDao;
    private TaskMemberDao taskMemberDao;
    private TimeTrackingDao timeTrackingDao;

    // Current task
    private Task currentTask;

    // UI Components
    private JLabel lblTaskName;
    private JLabel lblProject;
    private JLabel lblStatus;
    private JLabel lblPriority;
    private JLabel lblDueDate;
    private JLabel lblEstimatedHours;
    private JLabel lblActualHours;
    private JLabel lblCreatedBy;
    private JLabel lblCreatedAt;
    private JLabel lblAssignedMembers;
    private JLabel lblSubtaskCount;
    private JTextArea txtDescription;
    private JPanel commentsPanel;

    public TaskDetailsPanel(TasksModulePanel parentModule, TaskMainFrame mainFrame) {
        this.parentModule = parentModule;
        this.mainFrame = mainFrame;
        this.taskDao = new TaskDao();
        this.projectDao = new ProjectDao();
        this.taskStatusDao = new TaskStatusDao();
        this.taskPriorityDao = new TaskPriorityDao();
        this.userDao = new UserDao();
        this.taskMemberDao = new TaskMemberDao();
        this.timeTrackingDao = new TimeTrackingDao();

        setLayout(new BorderLayout());
        setBackground(StyleUtil.BACKGROUND);

        initializeUI();
    }

    private void initializeUI() {
        // Top - Back button and actions
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center - Scrollable content
        JPanel contentPanel = createContentPanel();
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(StyleUtil.SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, StyleUtil.BORDER),
                StyleUtil.createPaddingBorder(15, 20, 15, 20)
        ));

        // Back button
        JButton btnBack = ComponentFactory.createSecondaryButton("â† Back to List");
        btnBack.addActionListener(e -> parentModule.showTaskList());
        panel.add(btnBack, BorderLayout.WEST);

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(StyleUtil.SURFACE);

        JButton btnEdit = ComponentFactory.createPrimaryButton("Edit");
        btnEdit.addActionListener(e -> editTask());

        JButton btnAssign = ComponentFactory.createSecondaryButton("Manage Assignees");
        btnAssign.addActionListener(e -> manageAssignees());

        JButton btnDelete = ComponentFactory.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteTask());

        actionPanel.add(btnEdit);
        actionPanel.add(btnAssign);
        actionPanel.add(btnDelete);

        panel.add(actionPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(StyleUtil.BACKGROUND);
        panel.setBorder(StyleUtil.createPaddingBorder(30, 40, 30, 40));

        // Title
        lblTaskName = ComponentFactory.createTitleLabel("Task Name");
        lblTaskName.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblTaskName);
        panel.add(Box.createVerticalStrut(30));

        // Basic Info Card
        JPanel basicInfoCard = createBasicInfoCard();
        basicInfoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(basicInfoCard);
        panel.add(Box.createVerticalStrut(20));

        // Description Card
        JPanel descriptionCard = createDescriptionCard();
        descriptionCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descriptionCard);
        panel.add(Box.createVerticalStrut(20));

        // Time & Progress Card
        JPanel timeCard = createTimeCard();
        timeCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(timeCard);
        panel.add(Box.createVerticalStrut(20));

        // Assignees Card
        JPanel assigneesCard = createAssigneesCard();
        assigneesCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(assigneesCard);

        return panel;
    }

    private JPanel createBasicInfoCard() {
        JPanel card = ComponentFactory.createCard();
        card.setLayout(new GridLayout(8, 2, 15, 15));
        card.setMaximumSize(new Dimension(800, 350));

        // Project
        card.add(ComponentFactory.createBodyLabel("Project:"));
        lblProject = ComponentFactory.createBodyLabel("-");
        lblProject.setForeground(StyleUtil.PRIMARY);
        lblProject.setFont(StyleUtil.FONT_BUTTON);
        card.add(lblProject);

        // Status
        card.add(ComponentFactory.createBodyLabel("Status:"));
        lblStatus = ComponentFactory.createBodyLabel("-");
        lblStatus.setForeground(StyleUtil.INFO);
        lblStatus.setFont(StyleUtil.FONT_BUTTON);
        card.add(lblStatus);

        // Priority
        card.add(ComponentFactory.createBodyLabel("Priority:"));
        lblPriority = ComponentFactory.createBodyLabel("-");
        lblPriority.setForeground(StyleUtil.DANGER);
        lblPriority.setFont(StyleUtil.FONT_BUTTON);
        card.add(lblPriority);

        // Due Date
        card.add(ComponentFactory.createBodyLabel("Due Date:"));
        lblDueDate = ComponentFactory.createBodyLabel("-");
        lblDueDate.setForeground(StyleUtil.TEXT_PRIMARY);
        card.add(lblDueDate);

        // Estimated Hours
        card.add(ComponentFactory.createBodyLabel("Estimated Hours:"));
        lblEstimatedHours = ComponentFactory.createBodyLabel("-");
        lblEstimatedHours.setForeground(StyleUtil.TEXT_PRIMARY);
        card.add(lblEstimatedHours);

        // Actual Hours
        card.add(ComponentFactory.createBodyLabel("Actual Hours Logged:"));
        lblActualHours = ComponentFactory.createBodyLabel("-");
        lblActualHours.setForeground(StyleUtil.TEXT_PRIMARY);
        card.add(lblActualHours);

        // Created By
        card.add(ComponentFactory.createBodyLabel("Created By:"));
        lblCreatedBy = ComponentFactory.createBodyLabel("-");
        lblCreatedBy.setForeground(StyleUtil.TEXT_PRIMARY);
        card.add(lblCreatedBy);

        // Created At
        card.add(ComponentFactory.createBodyLabel("Created At:"));
        lblCreatedAt = ComponentFactory.createBodyLabel("-");
        lblCreatedAt.setForeground(StyleUtil.TEXT_SECONDARY);
        lblCreatedAt.setFont(StyleUtil.FONT_SMALL);
        card.add(lblCreatedAt);

        return card;
    }

    private JPanel createDescriptionCard() {
        JPanel card = ComponentFactory.createCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setMaximumSize(new Dimension(800, 200));

        JLabel titleLabel = ComponentFactory.createHeadingLabel("Description");
        card.add(titleLabel, BorderLayout.NORTH);

        txtDescription = new JTextArea(5, 40);
        txtDescription.setFont(StyleUtil.FONT_BODY);
        txtDescription.setForeground(StyleUtil.TEXT_SECONDARY);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setEditable(false);
        txtDescription.setBackground(Color.WHITE);
        txtDescription.setForeground(StyleUtil.TEXT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(txtDescription);
        scrollPane.setBorder(BorderFactory.createLineBorder(StyleUtil.BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTimeCard() {
        JPanel card = ComponentFactory.createCard();
        card.setLayout(new BorderLayout(0, 15));
        card.setMaximumSize(new Dimension(800, 150));

        JLabel titleLabel = ComponentFactory.createHeadingLabel("Time & Progress");
        card.add(titleLabel, BorderLayout.NORTH);

        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        statsGrid.setBackground(StyleUtil.SURFACE);

        // Estimated
        JPanel estimatedPanel = createStatBox("Estimated", "0h", StyleUtil.INFO);
        lblEstimatedHours = (JLabel) estimatedPanel.getComponent(0);

        // Actual
        JPanel actualPanel = createStatBox("Actual", "0h", StyleUtil.WARNING);
        lblActualHours = (JLabel) actualPanel.getComponent(0);

        // Remaining
        JPanel remainingPanel = createStatBox("Remaining", "0h", StyleUtil.SUCCESS);

        statsGrid.add(estimatedPanel);
        statsGrid.add(actualPanel);
        statsGrid.add(remainingPanel);

        card.add(statsGrid, BorderLayout.CENTER);

        return card;
    }

    private JPanel createStatBox(String label, String value, Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(StyleUtil.BACKGROUND);
        panel.setBorder(StyleUtil.createPaddingBorder(15));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel labelLabel = ComponentFactory.createBodyLabel(label);
        labelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(valueLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(labelLabel);

        return panel;
    }

    private JPanel createAssigneesCard() {
        JPanel card = ComponentFactory.createCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setMaximumSize(new Dimension(800, 200));

        JLabel titleLabel = ComponentFactory.createHeadingLabel("Assigned Members");
        card.add(titleLabel, BorderLayout.NORTH);

        lblAssignedMembers = ComponentFactory.createBodyLabel("No members assigned");
        lblAssignedMembers.setFont(StyleUtil.FONT_BODY);
        card.add(lblAssignedMembers, BorderLayout.CENTER);

        return card;
    }

    public void loadTask(int taskId) throws SQLException {
        currentTask = taskDao.findById(taskId);

        if (currentTask == null) {
            UIHelper.showError(mainFrame, "Task not found!");
            parentModule.showTaskList();
            return;
        }

        displayTaskData();
        loadTimeTracking();
        loadAssignees();
    }

    private void displayTaskData() throws SQLException {
        // Task Name
        lblTaskName.setText(currentTask.getTaskName());

        // Project
        Project project = projectDao.findById(currentTask.getProjectId());
        lblProject.setText(project != null ? project.getProjectName() : "Unknown");

        // Status
        TaskStatus status = taskStatusDao.findById(currentTask.getStatusId());
        lblStatus.setText(status != null ? status.getStatusName() : "Unknown");

        // Priority
        TaskPriority priority = taskPriorityDao.findById(currentTask.getPriorityId());
        lblPriority.setText(priority != null ? priority.getPriorityName() : "Unknown");

        // Due Date
        lblDueDate.setText(currentTask.getDueDate() != null ?
                currentTask.getDueDate().toString() : "Not set");

        // Estimated Hours
        lblEstimatedHours.setText(currentTask.getEstimatedHours() > 0 ?
                currentTask.getEstimatedHours() + " hours" : "Not set");

        // Creator
        User creator = userDao.findById(currentTask.getCreatedBy());
        lblCreatedBy.setText(creator != null ? creator.getFullName() : "Unknown");

        // Created At
        lblCreatedAt.setText(currentTask.getCreatedAt() != null ?
                currentTask.getCreatedAt().toString() : "-");

        // Description
        txtDescription.setText(currentTask.getDescription() != null ?
                currentTask.getDescription() : "No description provided.");
    }

    private void loadTimeTracking() {
        if (currentTask == null) return;

        double actualHours = timeTrackingDao.getTotalHoursForTask(currentTask.getTaskId());
        lblActualHours.setText(actualHours + " hours");
    }

    private void loadAssignees() {
        if (currentTask == null) return;

        List<TaskMember> members = taskMemberDao.findByTask(currentTask.getTaskId());

        if (members.isEmpty()) {
            lblAssignedMembers.setText("No members assigned");
            return;
        }

        StringBuilder sb = new StringBuilder("<html>");
        for (TaskMember member : members) {
            try {
                User user = userDao.findById(member.getUserId());
                if (user != null) {
                    sb.append("â€¢ ").append(user.getFullName())
                            .append(" (").append(user.getEmail()).append(")<br>");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        sb.append("</html>");

        lblAssignedMembers.setText(sb.toString());
    }

    private void editTask() {
        if (currentTask != null) {
            parentModule.openEditTaskDialog(currentTask.getTaskId());
        }
    }

    private void manageAssignees() {
        if (currentTask != null) {
            parentModule.openManageAssigneesDialog(currentTask.getTaskId());
        }
    }

    private void deleteTask() {
        if (currentTask == null) return;

        boolean confirm = UIHelper.showConfirmDialog(mainFrame,
                "Are you sure you want to delete this task?\n" +
                        "Task: " + currentTask.getTaskName() + "\n\n" +
                        "This will also delete all associated data.",
                "Confirm Delete");

        if (confirm) {
            try {
                taskDao.delete(currentTask.getTaskId());
                UIHelper.showSuccess(mainFrame, "Task deleted successfully!");
                parentModule.showTaskList();
            } catch (Exception e) {
                UIHelper.showError(mainFrame, "Error deleting task: " + e.getMessage());
            }
        }
    }

    public void refreshCurrentTask() throws SQLException {
        if (currentTask != null) {
            loadTask(currentTask.getTaskId());
        }
    }
}