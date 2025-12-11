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
    private TaskCommentDao taskCommentDao;

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
    private TimeTrackingPanel timeTrackingPanel;

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
        this.taskCommentDao = new TaskCommentDao();

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
        JButton btnBack = ComponentFactory.createSecondaryButton("Back to List");
        btnBack.addActionListener(e -> parentModule.showTaskList());
        panel.add(btnBack, BorderLayout.WEST);

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(StyleUtil.SURFACE);

        JButton btnEdit = ComponentFactory.createPrimaryButton("Edit");
        btnEdit.addActionListener(e -> editTask());

        JButton btnAssign = ComponentFactory.createSecondaryButton("Manage Assignees");
        btnAssign.addActionListener(e -> manageAssignees());

        JButton btnLogTime = ComponentFactory.createSecondaryButton("Log Time");
        btnLogTime.addActionListener(e -> openLogTimeDialog());

        JButton btnDelete = ComponentFactory.createDangerButton("Delete");
        btnDelete.addActionListener(e -> deleteTask());

        actionPanel.add(btnEdit);
        actionPanel.add(btnAssign);
        actionPanel.add(btnLogTime);
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
        panel.add(Box.createVerticalStrut(20));

        // Comments Section
        JPanel commentsCard = createCommentsCard();
        commentsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(commentsCard);
        panel.add(Box.createVerticalStrut(20));

        // Time Tracking Card
        JPanel timeEntriesCard = createTimeEntriesCard();
        timeEntriesCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(timeEntriesCard);

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
        card.setMaximumSize(new Dimension(800, 180));

        JLabel titleLabel = ComponentFactory.createHeadingLabel("Time & Progress");
        card.add(titleLabel, BorderLayout.NORTH);

        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        statsGrid.setBackground(StyleUtil.SURFACE);

        // Estimated
        JPanel estimatedPanel = createStatBox("Estimated", "0h", StyleUtil.INFO);

        // Actual
        JPanel actualPanel = createStatBox("Actual", "0h", StyleUtil.WARNING);

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
        panel.setBackground(Color.WHITE);
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
        loadComments();
        initializeTimeTrackingPanel();
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

        // Update Time & Progress card
        updateTimeProgressCard();
    }

    private void updateTimeProgressCard() {
        if (currentTask == null) return;

        // Find the Time & Progress card
        Component[] components = ((JPanel)((JScrollPane)getComponent(1)).getViewport().getView()).getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getComponentCount() > 0 && panel.getComponent(0) instanceof JLabel) {
                    JLabel titleLabel = (JLabel) panel.getComponent(0);
                    if (titleLabel.getText().equals("Time & Progress")) {
                        // Found the Time & Progress card
                        if (panel.getComponentCount() > 1 && panel.getComponent(1) instanceof JPanel) {
                            JPanel statsGrid = (JPanel) panel.getComponent(1);

                            // Get estimated hours
                            double estimated = currentTask.getEstimatedHours();

                            // Get actual hours
                            double actual = timeTrackingDao.getTotalHoursForTask(currentTask.getTaskId());

                            // Calculate remaining
                            double remaining = estimated - actual;

                            // Update each stat box
                            if (statsGrid.getComponentCount() >= 3) {
                                updateStatBox((JPanel) statsGrid.getComponent(0), estimated + "h");
                                updateStatBox((JPanel) statsGrid.getComponent(1), actual + "h");

                                // Color code remaining based on value
                                JPanel remainingPanel = (JPanel) statsGrid.getComponent(2);
                                JLabel remainingValue = (JLabel) remainingPanel.getComponent(0);
                                remainingValue.setText(remaining + "h");
                                if (remaining < 0) {
                                    remainingValue.setForeground(StyleUtil.DANGER); // Over budget
                                } else if (remaining == 0) {
                                    remainingValue.setForeground(StyleUtil.WARNING); // Exactly on budget
                                } else {
                                    remainingValue.setForeground(StyleUtil.SUCCESS); // Under budget
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    private void updateStatBox(JPanel statBox, String value) {
        if (statBox.getComponentCount() > 0 && statBox.getComponent(0) instanceof JLabel) {
            JLabel valueLabel = (JLabel) statBox.getComponent(0);
            valueLabel.setText(value);
        }
    }

    private void loadTimeTracking() {
        if (currentTask == null) return;

        double actualHours = timeTrackingDao.getTotalHoursForTask(currentTask.getTaskId());
        lblActualHours.setText(actualHours + " hours");

        // Also update the Time & Progress card
        updateTimeProgressCard();
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
                    sb.append(" ").append(user.getFullName())
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

    // ═══════════════════════════════════════════════════════════════════════════
    // COMMENTS SECTION METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Create the comments card with input field and display area
     */
    private JPanel createCommentsCard() {
        JPanel card = ComponentFactory.createCard();
        card.setLayout(new BorderLayout(0, 15));
        card.setMaximumSize(new Dimension(800, 500));

        // Title with comment count
        JLabel titleLabel = ComponentFactory.createHeadingLabel("Comments");
        card.add(titleLabel, BorderLayout.NORTH);

        // Comments display area (scrollable)
        commentsPanel = new JPanel();
        commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
        commentsPanel.setBackground(StyleUtil.SURFACE);

        JScrollPane commentsScrollPane = new JScrollPane(commentsPanel);
        commentsScrollPane.setBorder(BorderFactory.createLineBorder(StyleUtil.BORDER));
        commentsScrollPane.setBackground(StyleUtil.SURFACE);
        commentsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        card.add(commentsScrollPane, BorderLayout.CENTER);

        // Input area at bottom
        JPanel inputPanel = createCommentInputPanel();
        card.add(inputPanel, BorderLayout.SOUTH);

        return card;
    }

    /**
     * Create input panel for posting new comments
     */
    private JPanel createCommentInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(StyleUtil.SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, StyleUtil.BORDER),
                BorderFactory.createEmptyBorder(15, 0, 0, 0)
        ));

        // Text area for writing comment
        JTextArea txtNewComment = new JTextArea(3, 40);
        txtNewComment.setFont(StyleUtil.FONT_BODY);
        txtNewComment.setLineWrap(true);
        txtNewComment.setWrapStyleWord(true);
        txtNewComment.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        txtNewComment.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(txtNewComment);
        scrollPane.setPreferredSize(new Dimension(700, 80));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(StyleUtil.SURFACE);

        JButton btnPost = ComponentFactory.createPrimaryButton("Post Comment");
        btnPost.addActionListener(e -> {
            String commentText = txtNewComment.getText().trim();
            if (commentText.isEmpty()) {
                UIHelper.showError(mainFrame, "Comment cannot be empty");
                return;
            }

            postComment(commentText);
            txtNewComment.setText("");  // Clear input
        });

        JButton btnCancel = ComponentFactory.createSecondaryButton("Clear");
        btnCancel.addActionListener(e -> txtNewComment.setText(""));

        buttonPanel.add(btnPost);
        buttonPanel.add(btnCancel);

        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Load and display all comments for current task
     */
    private void loadComments() {
        if (currentTask == null || commentsPanel == null) return;

        commentsPanel.removeAll();

        List<TaskComment> comments = taskCommentDao.findByTask(currentTask.getTaskId());

        if (comments.isEmpty()) {
            JLabel noCommentsLabel = ComponentFactory.createBodyLabel("No comments yet. Be the first to comment!");
            noCommentsLabel.setForeground(StyleUtil.TEXT_SECONDARY);
            noCommentsLabel.setBorder(StyleUtil.createPaddingBorder(20));
            commentsPanel.add(noCommentsLabel);
        } else {
            for (TaskComment comment : comments) {
                JPanel commentPanel = createCommentPanel(comment);
                commentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                commentsPanel.add(commentPanel);
                commentsPanel.add(Box.createVerticalStrut(10));
            }
        }

        commentsPanel.revalidate();
        commentsPanel.repaint();
    }

    /**
     * Create a panel for displaying a single comment
     */
    private JPanel createCommentPanel(TaskComment comment) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setMaximumSize(new Dimension(750, Integer.MAX_VALUE));

        // Header: User name, timestamp, and actions
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        // Left side: User and time
        JPanel userTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        userTimePanel.setBackground(Color.WHITE);

        try {
            User user = userDao.findById(comment.getUserId());
            String userName = user != null ? user.getFullName() : "Unknown User";

            JLabel userLabel = new JLabel(userName);
            userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            userLabel.setForeground(StyleUtil.PRIMARY);
            userTimePanel.add(userLabel);

            userTimePanel.add(new JLabel(" • "));

            JLabel timeLabel = new JLabel(formatTimeAgo(comment.getCreatedAt()));
            timeLabel.setFont(StyleUtil.FONT_SMALL);
            timeLabel.setForeground(StyleUtil.TEXT_SECONDARY);
            userTimePanel.add(timeLabel);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        headerPanel.add(userTimePanel, BorderLayout.WEST);

        // Right side: Edit/Delete buttons (only for own comments)
        int currentUserId = mainFrame.getCurrentUserId();
        if (comment.getUserId() == currentUserId) {
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            actionPanel.setBackground(Color.WHITE);

            JButton btnEdit = new JButton("Edit");
            btnEdit.setFont(StyleUtil.FONT_SMALL);
            btnEdit.setForeground(StyleUtil.PRIMARY);
            btnEdit.setBorderPainted(false);
            btnEdit.setContentAreaFilled(false);
            btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnEdit.addActionListener(e -> editComment(comment));

            JButton btnDelete = new JButton("Delete");
            btnDelete.setFont(StyleUtil.FONT_SMALL);
            btnDelete.setForeground(StyleUtil.DANGER);
            btnDelete.setBorderPainted(false);
            btnDelete.setContentAreaFilled(false);
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDelete.addActionListener(e -> deleteComment(comment));

            actionPanel.add(btnEdit);
            actionPanel.add(new JLabel("|"));
            actionPanel.add(btnDelete);

            headerPanel.add(actionPanel, BorderLayout.EAST);
        }

        panel.add(headerPanel);
        panel.add(Box.createVerticalStrut(10));

        // Comment text
        JTextArea commentTextArea = new JTextArea(comment.getCommentText());
        commentTextArea.setFont(StyleUtil.FONT_BODY);
        commentTextArea.setForeground(StyleUtil.TEXT_PRIMARY);
        commentTextArea.setLineWrap(true);
        commentTextArea.setWrapStyleWord(true);
        commentTextArea.setEditable(false);
        commentTextArea.setBackground(Color.WHITE);
        commentTextArea.setBorder(null);
        panel.add(commentTextArea);

        return panel;
    }

    /**
     * Format timestamp as "X minutes/hours/days ago"
     */
    private String formatTimeAgo(java.time.LocalDateTime timestamp) {
        java.time.Duration duration = java.time.Duration.between(timestamp, java.time.LocalDateTime.now());

        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "just now";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (seconds < 604800) {
            long days = seconds / 86400;
            return days + (days == 1 ? " day ago" : " days ago");
        } else {
            long weeks = seconds / 604800;
            return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        }
    }

    /**
     * Post a new comment
     */
    private void postComment(String commentText) {
        if (currentTask == null) return;

        int currentUserId = mainFrame.getCurrentUserId();

        TaskComment comment = new TaskComment(currentTask.getTaskId(), currentUserId, commentText);
        taskCommentDao.insert(comment);

        UIHelper.showSuccess(mainFrame, "Comment posted successfully!");
        loadComments();  // Refresh comments display
    }

    /**
     * Edit an existing comment
     */
    private void editComment(TaskComment comment) {
        String newText = JOptionPane.showInputDialog(
                mainFrame,
                "Edit your comment:",
                comment.getCommentText()
        );

        if (newText != null && !newText.trim().isEmpty()) {
            comment.setCommentText(newText.trim());
            taskCommentDao.update(comment);
            UIHelper.showSuccess(mainFrame, "Comment updated successfully!");
            loadComments();  // Refresh display
        }
    }

    /**
     * Delete a comment
     */
    private void deleteComment(TaskComment comment) {
        boolean confirm = UIHelper.showConfirmDialog(mainFrame,
                "Delete this comment?\n\nThis action cannot be undone.",
                "Confirm Delete");

        if (confirm) {
            taskCommentDao.delete(comment.getCommentId());
            UIHelper.showSuccess(mainFrame, "Comment deleted successfully!");
            loadComments();  // Refresh display
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TIME TRACKING SECTION METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Create the time tracking card
     */
    private JPanel createTimeEntriesCard() {
        JPanel card = ComponentFactory.createCard();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(800, 400));

        JLabel titleLabel = ComponentFactory.createHeadingLabel("Time Tracking");
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        card.add(titleLabel, BorderLayout.NORTH);

        // Time tracking panel will be initialized when task is loaded
        JPanel placeholder = new JPanel();
        placeholder.setBackground(Color.WHITE);
        placeholder.add(ComponentFactory.createBodyLabel("Loading time entries..."));
        card.add(placeholder, BorderLayout.CENTER);

        return card;
    }

    /**
     * Initialize the time tracking panel once task is loaded
     */
    private void initializeTimeTrackingPanel() {
        if (currentTask == null) return;

        // Find the time entries card and update it with the time tracking panel
        Component[] components = ((JPanel)((JScrollPane)getComponent(1)).getViewport().getView()).getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                // Find the card with "Time Tracking" title
                if (panel.getComponentCount() > 0 && panel.getComponent(0) instanceof JLabel) {
                    JLabel label = (JLabel) panel.getComponent(0);
                    if (label.getText().equals("Time Tracking")) {
                        // Remove placeholder and add time tracking panel
                        if (panel.getComponentCount() > 1) {
                            panel.remove(1);
                        }

                        timeTrackingPanel = new TimeTrackingPanel(
                                mainFrame,
                                currentTask.getTaskId(),
                                mainFrame.getCurrentUserId(),
                                this::loadTimeTracking
                        );
                        panel.add(timeTrackingPanel, BorderLayout.CENTER);
                        panel.revalidate();
                        panel.repaint();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Open the log time dialog
     */
    private void openLogTimeDialog() {
        if (currentTask == null) return;

        LogTimeDialog dialog = new LogTimeDialog(
                mainFrame,
                currentTask.getTaskId(),
                mainFrame.getCurrentUserId(),
                () -> {
                    try {
                        loadTimeTracking();
                        if (timeTrackingPanel != null) {
                            timeTrackingPanel.loadTimeEntries();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
        dialog.setVisible(true);
    }
}