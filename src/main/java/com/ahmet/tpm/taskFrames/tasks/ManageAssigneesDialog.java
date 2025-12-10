package com.ahmet.tpm.taskFrames.tasks;

import com.ahmet.tpm.service.NotificationService;
import com.ahmet.tpm.dao.*;
import com.ahmet.tpm.taskFrames.TaskMainFrame;
import com.ahmet.tpm.models.*;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ManageAssigneesDialog extends JDialog {

    private NotificationService notificationService;

    private TasksModulePanel parentModule;
    private TaskMainFrame mainFrame;

    // DAOs
    private TaskMemberDao taskMemberDao;
    private UserDao userDao;
    private TaskDao taskDao;
    private ProjectMemberDao projectMemberDao;

    // Current task
    private int taskId;
    private Task task;

    // UI Components
    private JTable assigneesTable;
    private DefaultTableModel tableModel;
    private JComboBox<User> cmbUsers;
    private JLabel lblTaskName;
    private JLabel lblAssigneeCount;

    public ManageAssigneesDialog(TaskMainFrame mainFrame, TasksModulePanel parentModule, int taskId) {
        super(mainFrame, "Manage Task Assignees", true);
        this.mainFrame = mainFrame;
        this.parentModule = parentModule;
        this.taskId = taskId;

        // Initialize DAOs
        this.taskMemberDao = new TaskMemberDao();
        this.userDao = new UserDao();
        this.taskDao = new TaskDao();
        this.projectMemberDao = new ProjectMemberDao();

        // Initialize notification service
        this.notificationService = new NotificationService();

        // Load task
        this.task = taskDao.findById(taskId);
        if (this.task == null) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Task not found!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        initializeDialog();
        loadAssignees();
        loadAvailableUsers();
    }

    private void initializeDialog() {
        setSize(800, 600);
        setLocationRelativeTo(mainFrame);
        setLayout(new BorderLayout());
        setResizable(false);

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Center - Split into two sections
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(StyleUtil.SURFACE);
        centerPanel.setBorder(StyleUtil.createPaddingBorder(20));

        // Current assignees section
        JPanel assigneesSection = createAssigneesSection();
        centerPanel.add(assigneesSection, BorderLayout.CENTER);

        // Add assignee section
        JPanel addAssigneeSection = createAddAssigneeSection();
        centerPanel.add(addAssigneeSection, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(StyleUtil.PRIMARY_LIGHT);
        panel.setBorder(StyleUtil.createPaddingBorder(20));

        // Task info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(StyleUtil.PRIMARY_LIGHT);

        lblTaskName = ComponentFactory.createHeadingLabel("Task: " + task.getTaskName());
        lblTaskName.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblAssigneeCount = ComponentFactory.createBodyLabel("Total Assignees: 0");
        lblAssigneeCount.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblAssigneeCount.setForeground(StyleUtil.TEXT_SECONDARY);

        infoPanel.add(lblTaskName);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblAssigneeCount);

        panel.add(infoPanel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createAssigneesSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(StyleUtil.SURFACE);

        // Title
        JLabel titleLabel = ComponentFactory.createHeadingLabel("Current Assignees");
        panel.add(titleLabel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"Assignee ID", "User", "Email", "Assigned Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        assigneesTable = new JTable(tableModel);
        assigneesTable.setFont(StyleUtil.FONT_BODY);
        assigneesTable.setRowHeight(35);
        assigneesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        assigneesTable.getTableHeader().setFont(StyleUtil.FONT_BUTTON);
        assigneesTable.getTableHeader().setBackground(StyleUtil.PRIMARY_LIGHT);
        assigneesTable.setBackground(Color.WHITE);
        assigneesTable.setForeground(StyleUtil.TEXT_PRIMARY);
        assigneesTable.setGridColor(StyleUtil.BORDER);
        assigneesTable.setSelectionBackground(StyleUtil.PRIMARY_LIGHT);
        assigneesTable.setSelectionForeground(StyleUtil.TEXT_PRIMARY);

        // Column widths
        assigneesTable.getColumnModel().getColumn(0).setPreferredWidth(100);  // Assignee ID
        assigneesTable.getColumnModel().getColumn(1).setPreferredWidth(200);  // User
        assigneesTable.getColumnModel().getColumn(2).setPreferredWidth(250);  // Email
        assigneesTable.getColumnModel().getColumn(3).setPreferredWidth(150);  // Assigned Date

        JScrollPane scrollPane = new JScrollPane(assigneesTable);
        scrollPane.setBorder(StyleUtil.createLineBorder());
        scrollPane.setPreferredSize(new Dimension(750, 250));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Action button for selected assignee
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionPanel.setBackground(StyleUtil.SURFACE);

        JButton btnRemove = ComponentFactory.createDangerButton("Remove Assignee");
        btnRemove.addActionListener(e -> removeAssignee());

        actionPanel.add(btnRemove);

        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAddAssigneeSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(StyleUtil.BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                StyleUtil.createLineBorder(),
                StyleUtil.createPaddingBorder(15)
        ));

        // Title
        JLabel titleLabel = ComponentFactory.createHeadingLabel("Add New Assignee");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));

        // Form panel
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        formPanel.setBackground(StyleUtil.BACKGROUND);
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // User selection
        JLabel lblUser = ComponentFactory.createBodyLabel("Select User:");
        cmbUsers = new JComboBox<>();
        cmbUsers.setFont(StyleUtil.FONT_BODY);
        cmbUsers.setPreferredSize(new Dimension(300, 35));
        cmbUsers.setBackground(Color.WHITE);
        cmbUsers.setForeground(StyleUtil.TEXT_PRIMARY);

        cmbUsers.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User) {
                    User user = (User) value;
                    setText(user.getFullName() + " (" + user.getUsername() + ")");
                }
                return this;
            }
        });

        // Add button
        JButton btnAdd = ComponentFactory.createPrimaryButton("Add Assignee");
        btnAdd.addActionListener(e -> addAssignee());

        formPanel.add(lblUser);
        formPanel.add(cmbUsers);
        formPanel.add(Box.createHorizontalStrut(10));
        formPanel.add(btnAdd);

        panel.add(formPanel);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panel.setBackground(StyleUtil.SURFACE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, StyleUtil.BORDER));

        JButton btnClose = ComponentFactory.createSecondaryButton("Done");
        btnClose.addActionListener(e -> {
            try {
                parentModule.onTaskUpdated();
            } catch (SQLException ex) {
                // If we're in list view, just refresh
                parentModule.showTaskList();
            }
            dispose();
        });

        panel.add(btnClose);

        return panel;
    }

    private void loadAssignees() {
        tableModel.setRowCount(0);

        List<TaskMember> assignees = taskMemberDao.findByTask(taskId);

        for (TaskMember assignee : assignees) {
            try {
                User user = userDao.findById(assignee.getUserId());
                if (user != null) {
                    Object[] row = {
                            assignee.getTaskMemberId(),
                            user.getFullName(),
                            user.getEmail(),
                            assignee.getAssignedAt() != null ?
                                    assignee.getAssignedAt().toLocalDate().toString() : "-"
                    };
                    tableModel.addRow(row);
                }
            } catch (SQLException e) {
                System.err.println("Error loading user: " + e.getMessage());
            }
        }

        // Update count
        lblAssigneeCount.setText("Total Assignees: " + assignees.size());
    }

    private void loadAvailableUsers() {
        cmbUsers.removeAllItems();

        try {
            // CRITICAL: Only load users who are members of this task's project
            List<Integer> projectMemberIds = projectMemberDao.getUserIdsForProject(task.getProjectId());
            List<Integer> currentAssigneeIds = taskMemberDao.getUserIdsForTask(taskId);

            for (Integer userId : projectMemberIds) {
                // Only add if not already assigned
                if (!currentAssigneeIds.contains(userId)) {
                    User user = userDao.findById(userId);
                    if (user != null) {
                        cmbUsers.addItem(user);
                    }
                }
            }

            if (cmbUsers.getItemCount() == 0) {
                // All project members are already assigned
                cmbUsers.addItem(null);
                cmbUsers.setEnabled(false);
            } else {
                cmbUsers.setEnabled(true);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading users: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ============ ENHANCED ADD ASSIGNEE WITH NOTIFICATION ============
    private void addAssignee() {
        // Validation
        User selectedUser = (User) cmbUsers.getSelectedItem();
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to assign!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if already assigned (extra safety check)
        if (taskMemberDao.isAssignedToTask(selectedUser.getUserId(), taskId)) {
            JOptionPane.showMessageDialog(this,
                    "This user is already assigned to the task!",
                    "Duplicate Assignee",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verify user is a project member (critical business rule!)
        List<Integer> projectMembers = projectMemberDao.getUserIdsForProject(task.getProjectId());
        if (!projectMembers.contains(selectedUser.getUserId())) {
            JOptionPane.showMessageDialog(this,
                    "User must be a project member before being assigned to tasks!",
                    "Invalid Assignment",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Add assignee to database
        try {
            TaskMember newAssignee = new TaskMember(taskId, selectedUser.getUserId());
            taskMemberDao.insert(newAssignee);

            // ============ STEP 1: SEND NOTIFICATION TO ASSIGNED USER ============
            String assignerName = mainFrame.getCurrentUsername();

            notificationService.notifyTaskAssignment(
                    taskId,
                    selectedUser.getUserId(),
                    task.getTaskName(),
                    assignerName
            );

            System.out.println("✓ Assignment notification sent to: " + selectedUser.getFullName());

            // ============ STEP 2: OPTIONALLY NOTIFY OTHER TASK MEMBERS ============
            // Uncomment this if you want to notify existing assignees about new member
            /*
            List<Integer> existingAssignees = taskMemberDao.getUserIdsForTask(taskId);
            for (int userId : existingAssignees) {
                if (userId != selectedUser.getUserId()) {
                    notificationService.notifyTaskUpdate(
                        taskId,
                        task.getTaskName(),
                        assignerName,
                        "added " + selectedUser.getFullName() + " to task"
                    );
                }
            }
            */

            JOptionPane.showMessageDialog(this,
                    selectedUser.getFullName() + " has been assigned to the task!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Refresh UI
            loadAssignees();
            loadAvailableUsers();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error adding assignee: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ============ ENHANCED REMOVE ASSIGNEE WITH NOTIFICATION ============
    private void removeAssignee() {
        int selectedRow = assigneesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an assignee to remove!",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get assignee info from table
        String userName = (String) tableModel.getValueAt(selectedRow, 1);
        int taskMemberId = (int) tableModel.getValueAt(selectedRow, 0);

        // Confirm removal
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to remove " + userName + " from this task?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Get userId before deletion
                int userId = getUserIdFromTaskMemberId(taskMemberId);

                if (userId == -1) {
                    throw new Exception("Could not find user ID for selected assignee");
                }

                // ============ STEP 1: REMOVE FROM DATABASE ============
                taskMemberDao.delete(taskId, userId);

                // ============ STEP 2: SEND NOTIFICATION TO REMOVED USER ============
                String removerName = mainFrame.getCurrentUsername();

                // Create custom notification for removal
                notificationService.notifyTaskUpdate(
                        taskId,
                        task.getTaskName(),
                        removerName,
                        "removed you from task"
                );

                System.out.println("✓ Removal notification sent to: " + userName);

                // ============ STEP 3: OPTIONALLY NOTIFY OTHER TASK MEMBERS ============
                // Uncomment if you want to notify remaining assignees
                /*
                List<Integer> remainingAssignees = taskMemberDao.getUserIdsForTask(taskId);
                for (int remainingUserId : remainingAssignees) {
                    notificationService.notifyTaskUpdate(
                        taskId,
                        task.getTaskName(),
                        removerName,
                        "removed " + userName + " from task"
                    );
                }
                */

                JOptionPane.showMessageDialog(this,
                        userName + " has been removed from the task.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Refresh UI
                loadAssignees();
                loadAvailableUsers();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error removing assignee: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    /**
     * Helper method to get user ID from task member ID
     */
    private int getUserIdFromTaskMemberId(int taskMemberId) {
        List<TaskMember> assignees = taskMemberDao.findByTask(taskId);
        for (TaskMember assignee : assignees) {
            if (assignee.getTaskMemberId() == taskMemberId) {
                return assignee.getUserId();
            }
        }
        return -1;
    }
}