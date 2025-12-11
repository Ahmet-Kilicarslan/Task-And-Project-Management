package com.ahmet.tpm.taskFrames.tasks;

import com.ahmet.tpm.dao.*;
import com.ahmet.tpm.projectFrames.MainFrame;
import com.ahmet.tpm.taskFrames.TaskMainFrame; // ============ TASKMAINFRAME İMPORT ============
import com.ahmet.tpm.models.*;
import com.ahmet.tpm.service.NotificationService; // ============ BİLDİRİM İMPORT ============
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;
import com.ahmet.tpm.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class EditTaskDialog extends JDialog {

    private TasksModulePanel parentModule;
    private Frame parentFrame;

    // DAOs
    private TaskDao taskDao;
    private ProjectDao projectDao;
    private TaskStatusDao taskStatusDao;
    private TaskPriorityDao taskPriorityDao;
    private UserDao userDao;
    private TaskDependencyDao taskDependencyDao;

    private NotificationService notificationService;

    // Current task being edited
    private Task task;
    private int oldStatusId;
    private int oldPriorityId;
    private String oldTaskName;
    private LocalDate oldDueDate;;

    // Form fields
    private JComboBox<Project> cmbProject;
    private JTextField txtTaskName;
    private JTextArea txtDescription;
    private JComboBox<TaskStatus> cmbStatus;
    private JComboBox<TaskPriority> cmbPriority;
    private JTextField txtEstimatedHours;
    private JTextField txtDueDate;

    // Dependency selection
    private JList<Task> dependencyList;
    private DefaultListModel<Task> dependencyListModel;
    private List<Integer> originalDependencyIds;

    // ============ CONSTRUCTOR - MainFrame İLE ============
    public EditTaskDialog(MainFrame mainFrame, TasksModulePanel parentModule, int taskId) {
        super(mainFrame, "Edit Task", true);
        this.parentFrame = mainFrame;
        this.parentModule = parentModule;
        initializeCommon(taskId);
    }

    // ============ CONSTRUCTOR - TaskMainFrame İLE ============
    public EditTaskDialog(TaskMainFrame mainFrame, TasksModulePanel parentModule, int taskId) {
        super(mainFrame, "Edit Task", true);
        this.parentFrame = mainFrame;
        this.parentModule = parentModule;
        initializeCommon(taskId);
    }


    private void initializeCommon(int taskId) {
        this.taskDao = new TaskDao();
        this.projectDao = new ProjectDao();
        this.taskStatusDao = new TaskStatusDao();
        this.taskPriorityDao = new TaskPriorityDao();
        this.userDao = new UserDao();
        this.taskDependencyDao = new TaskDependencyDao();


        this.notificationService = new NotificationService();

        // Load task
        this.task = taskDao.findById(taskId);

        this.oldStatusId = this.task.getStatusId();
        this.oldPriorityId = this.task.getPriorityId();
        this.oldTaskName = this.task.getTaskName();
        this.oldDueDate = this.task.getDueDate();

        if (this.task == null) {
            UIHelper.showError((JFrame) parentFrame, "Task not found!");
            dispose();
            return;
        }

        initializeDialog();
        loadDropdownData();
        populateFormWithTaskData();
    }

    private void initializeDialog() {
        setSize(600, 900);  // Increased height for dependency section
        setLocationRelativeTo(parentFrame);
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(StyleUtil.SURFACE);
        panel.setBorder(StyleUtil.createPaddingBorder(20));

        // Title
        JLabel titleLabel = ComponentFactory.createHeadingLabel("Edit Task");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));

        // ✅ NEW: Warning panel for incomplete dependencies (will be populated later)
        JPanel warningPanel = createDependencyWarningPanel();
        warningPanel.setName("dependencyWarning");  // Name it so we can find it later
        warningPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(warningPanel);
        panel.add(Box.createVerticalStrut(10));

        // Project selector
        cmbProject = new JComboBox<>();
        cmbProject.setFont(StyleUtil.FONT_BODY);
        cmbProject.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Project) {
                    setText(((Project) value).getProjectName());
                }
                return this;
            }
        });
        panel.add(createFormField("Project *", cmbProject));

        // Task name
        panel.add(createFormField("Task Name *", txtTaskName = new JTextField()));

        // Due date
        panel.add(createFormField("Due Date (YYYY-MM-DD)", txtDueDate = new JTextField()));

        // Status
        cmbStatus = new JComboBox<>();
        cmbStatus.setFont(StyleUtil.FONT_BODY);
        cmbStatus.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TaskStatus) {
                    setText(((TaskStatus) value).getStatusName());
                }
                return this;
            }
        });
        panel.add(createFormField("Status *", cmbStatus));

        // Priority
        cmbPriority = new JComboBox<>();
        cmbPriority.setFont(StyleUtil.FONT_BODY);
        cmbPriority.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TaskPriority) {
                    setText(((TaskPriority) value).getPriorityName());
                }
                return this;
            }
        });
        panel.add(createFormField("Priority *", cmbPriority));

        // Estimated hours
        panel.add(createFormField("Estimated Hours", txtEstimatedHours = new JTextField()));

        // Description
        JLabel descLabel = ComponentFactory.createBodyLabel("Description");
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(5));

        txtDescription = new JTextArea(5, 40);
        txtDescription.setFont(StyleUtil.FONT_BODY);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBackground(Color.WHITE);
        txtDescription.setForeground(StyleUtil.TEXT_PRIMARY);
        txtDescription.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JScrollPane scrollPane = new JScrollPane(txtDescription);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setMaximumSize(new Dimension(550, 120));
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(10));

        // Task Dependencies Section
        panel.add(createDependencySection());
        panel.add(Box.createVerticalStrut(10));

        // Note
        JLabel noteLabel = new JLabel("<html><b>Note:</b> Fields marked with * are required</html>");
        noteLabel.setFont(StyleUtil.FONT_SMALL);
        noteLabel.setForeground(StyleUtil.TEXT_SECONDARY);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(noteLabel);

        return panel;
    }

    private JPanel createFormField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(StyleUtil.SURFACE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(550, 70));

        JLabel label = ComponentFactory.createBodyLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(5));

        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setFont(StyleUtil.FONT_BODY);
        field.setMaximumSize(new Dimension(550, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (field instanceof JTextField) {
            ((JTextField) field).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(StyleUtil.BORDER),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
        }

        panel.add(field);
        panel.add(Box.createVerticalStrut(10));

        return panel;
    }

    /**
     * Create warning panel for incomplete dependencies
     * Initially empty, will be populated by applyDependencyValidation()
     */
    private JPanel createDependencyWarningPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(255, 243, 205));  // Light yellow/warning color
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 193, 7), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setVisible(false);  // Hidden by default
        panel.setMaximumSize(new Dimension(550, 150));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panel.setBackground(StyleUtil.SURFACE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, StyleUtil.BORDER));

        JButton btnSave = ComponentFactory.createPrimaryButton("Save Changes");
        btnSave.addActionListener(e -> saveChanges());

        JButton btnCancel = ComponentFactory.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        panel.add(btnSave);
        panel.add(btnCancel);

        return panel;
    }

    private void loadDropdownData() {
        // Load projects
        List<Project> projects = projectDao.findAll();
        for (Project project : projects) {
            cmbProject.addItem(project);
        }

        // Load statuses
        List<TaskStatus> statuses = taskStatusDao.findAll();
        for (TaskStatus status : statuses) {
            cmbStatus.addItem(status);
        }

        // Load priorities
        List<TaskPriority> priorities = taskPriorityDao.findAll();
        for (TaskPriority priority : priorities) {
            cmbPriority.addItem(priority);
        }
    }

    private void populateFormWithTaskData() {
        // Task Name
        txtTaskName.setText(task.getTaskName());

        // Description
        if (task.getDescription() != null) {
            txtDescription.setText(task.getDescription());
        }

        // Due Date
        if (task.getDueDate() != null) {
            txtDueDate.setText(task.getDueDate().toString());
        }

        // Estimated Hours
        if (task.getEstimatedHours() > 0) {
            txtEstimatedHours.setText(String.valueOf(task.getEstimatedHours()));
        }

        // Project - Select the matching project
        for (int i = 0; i < cmbProject.getItemCount(); i++) {
            Project p = cmbProject.getItemAt(i);
            if (p.getProjectId() == task.getProjectId()) {
                cmbProject.setSelectedIndex(i);
                break;
            }
        }

        // Status - Select the matching status
        for (int i = 0; i < cmbStatus.getItemCount(); i++) {
            if (cmbStatus.getItemAt(i).getStatusId() == task.getStatusId()) {
                cmbStatus.setSelectedIndex(i);
                break;
            }
        }

        // Priority - Select the matching priority
        for (int i = 0; i < cmbPriority.getItemCount(); i++) {
            if (cmbPriority.getItemAt(i).getPriorityId() == task.getPriorityId()) {
                cmbPriority.setSelectedIndex(i);
                break;
            }
        }

        // ✅ NEW: Apply dependency validation for DONE status
        applyDependencyValidation();
    }

    /**
     * Check if all dependencies are complete and disable DONE status if not
     */
    private void applyDependencyValidation() {
        // Check if all dependencies are completed
        if (!taskDao.areAllDependenciesCompleted(task.getTaskId())) {
            // Get incomplete dependency names
            List<String> incompleteNames = taskDao.getIncompleteDependencyNames(task.getTaskId());

            // ✅ Show visual warning panel
            showDependencyWarning(incompleteNames);

            // Find and disable DONE status in combobox
            for (int i = 0; i < cmbStatus.getItemCount(); i++) {
                TaskStatus status = cmbStatus.getItemAt(i);
                if ("DONE".equals(status.getStatusName())) {
                    // Create custom renderer to show disabled item
                    cmbStatus.setRenderer(new DefaultListCellRenderer() {
                        @Override
                        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                                      int index, boolean isSelected, boolean cellHasFocus) {
                            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                            if (value instanceof TaskStatus) {
                                TaskStatus s = (TaskStatus) value;
                                setText(s.getStatusName());

                                // Disable DONE status visually
                                if ("DONE".equals(s.getStatusName())) {
                                    setForeground(Color.GRAY);
                                    setText(s.getStatusName() + " (dependencies incomplete)");
                                }
                            }
                            return this;
                        }
                    });

                    // Add action listener to prevent selecting DONE
                    cmbStatus.addActionListener(e -> {
                        TaskStatus selected = (TaskStatus) cmbStatus.getSelectedItem();
                        if (selected != null && "DONE".equals(selected.getStatusName())) {
                            if (!taskDao.areAllDependenciesCompleted(task.getTaskId())) {
                                // Show warning and revert selection
                                String message = "Cannot mark this task as DONE.\n\n" +
                                        "The following dependencies must be completed first:\n\n" +
                                        String.join("\n", incompleteNames);

                                JOptionPane.showMessageDialog(this,
                                        message,
                                        "Dependencies Not Complete",
                                        JOptionPane.WARNING_MESSAGE);

                                // Revert to previous status
                                for (int j = 0; j < cmbStatus.getItemCount(); j++) {
                                    if (cmbStatus.getItemAt(j).getStatusId() == task.getStatusId()) {
                                        cmbStatus.setSelectedIndex(j);
                                        break;
                                    }
                                }
                            }
                        }
                    });

                    System.out.println("⚠ DONE status disabled - incomplete dependencies:");
                    for (String name : incompleteNames) {
                        System.out.println("  - " + name);
                    }

                    break;
                }
            }
        } else {
            System.out.println("✓ All dependencies complete - DONE status available");
        }
    }

    /**
     * Show warning panel with incomplete dependencies
     */
    private void showDependencyWarning(List<String> incompleteNames) {
        // Find the warning panel
        Container contentPane = getContentPane();
        JPanel warningPanel = findComponentByName((Container) contentPane.getComponent(0), "dependencyWarning");

        if (warningPanel != null) {
            warningPanel.removeAll();

            // Warning icon and title
            JLabel titleLabel = new JLabel("⚠ Cannot Mark as DONE");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            titleLabel.setForeground(new Color(138, 109, 0));
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            warningPanel.add(titleLabel);
            warningPanel.add(Box.createVerticalStrut(8));

            // Explanation
            JLabel explanationLabel = new JLabel("Complete these dependencies first:");
            explanationLabel.setFont(StyleUtil.FONT_BODY);
            explanationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            warningPanel.add(explanationLabel);
            warningPanel.add(Box.createVerticalStrut(5));

            // List of incomplete dependencies
            for (String depName : incompleteNames) {
                JLabel depLabel = new JLabel("  • " + depName);
                depLabel.setFont(StyleUtil.FONT_BODY);
                depLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                warningPanel.add(depLabel);
            }

            warningPanel.setVisible(true);
            warningPanel.revalidate();
            warningPanel.repaint();
        }
    }

    /**
     * Helper method to find component by name
     */
    private JPanel findComponentByName(Container container, String name) {
        for (Component comp : container.getComponents()) {
            if (name.equals(comp.getName()) && comp instanceof JPanel) {
                return (JPanel) comp;
            }
            if (comp instanceof Container) {
                JPanel found = findComponentByName((Container) comp, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void saveChanges() {
        if (!validateForm()) {
            return;
        }

        try {
            // ============ STEP 1: UPDATE TASK OBJECT ============

            // Project
            Project selectedProject = (Project) cmbProject.getSelectedItem();
            task.setProjectId(selectedProject.getProjectId());

            // Task name
            String newTaskName = txtTaskName.getText().trim();
            task.setTaskName(newTaskName);

            // Description
            String description = txtDescription.getText().trim();
            task.setDescription(!description.isEmpty() ? description : null);

            // Status
            TaskStatus selectedStatus = (TaskStatus) cmbStatus.getSelectedItem();
            int newStatusId = selectedStatus.getStatusId();
            task.setStatusId(newStatusId);

            // Priority
            TaskPriority selectedPriority = (TaskPriority) cmbPriority.getSelectedItem();
            int newPriorityId = selectedPriority.getPriorityId();
            task.setPriorityId(newPriorityId);

            // Estimated hours
            String hoursStr = txtEstimatedHours.getText().trim();
            if (!hoursStr.isEmpty()) {
                task.setEstimatedHours(Double.parseDouble(hoursStr));
            } else {
                task.setEstimatedHours(0);
            }

            // Due date
            LocalDate newDueDate = null;
            String dueDateStr = txtDueDate.getText().trim();
            if (!dueDateStr.isEmpty()) {
                newDueDate = LocalDate.parse(dueDateStr);
                task.setDueDate(newDueDate);
            } else {
                task.setDueDate(null);
            }

            // ============ STEP 2: UPDATE IN DATABASE ============
            taskDao.update(task);

            // ============ STEP 2.5: UPDATE DEPENDENCIES ============
            updateDependencies();

            // ============ STEP 3: SEND NOTIFICATIONS ============
            // Get current user's name for notifications
            String updaterName = getCurrentUsername();

            // Check if status changed (most important notification)
            if (newStatusId != oldStatusId) {
                String newStatusName = selectedStatus.getStatusName();
                notificationService.notifyTaskStatusChange(
                        task.getTaskId(),
                        newTaskName,
                        newStatusName,
                        updaterName
                );
                System.out.println("✓ Status change notification sent");
            }

            // Check if priority changed
            if (newPriorityId != oldPriorityId) {
                notificationService.notifyTaskUpdate(
                        task.getTaskId(),
                        newTaskName,
                        updaterName,
                        "priority"
                );
                System.out.println("✓ Priority change notification sent");
            }

            // Check if task name changed
            if (!newTaskName.equals(oldTaskName)) {
                notificationService.notifyTaskUpdate(
                        task.getTaskId(),
                        newTaskName,
                        updaterName,
                        "task name"
                );
                System.out.println("✓ Task name change notification sent");
            }

            // Check if due date changed
            boolean dueDateChanged = false;
            if (oldDueDate == null && newDueDate != null) {
                dueDateChanged = true;
            } else if (oldDueDate != null && !oldDueDate.equals(newDueDate)) {
                dueDateChanged = true;
            }

            if (dueDateChanged) {
                notificationService.notifyTaskUpdate(
                        task.getTaskId(),
                        newTaskName,
                        updaterName,
                        "due date"
                );
                System.out.println("✓ Due date change notification sent");
            }

            // ============ STEP 4: SHOW SUCCESS & REFRESH UI ============
            UIHelper.showSuccess((JFrame) parentFrame, "Task updated successfully!");

            // Notify parent and close
            try {
                parentModule.onTaskUpdated();
            } catch (Exception ex) {
                // If we're in list view, just refresh list
                parentModule.showTaskList();
            }
            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid number format for estimated hours!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error updating task: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        // Project
        if (cmbProject.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a project!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Task name
        if (txtTaskName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Task Name is required!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            txtTaskName.requestFocus();
            return false;
        }

        // Status
        if (cmbStatus.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a status!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Priority
        if (cmbPriority.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a priority!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate estimated hours if provided
        String hoursStr = txtEstimatedHours.getText().trim();
        if (!hoursStr.isEmpty()) {
            try {
                double hours = Double.parseDouble(hoursStr);
                if (hours < 0) {
                    JOptionPane.showMessageDialog(this,
                            "Estimated hours must be positive!",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid estimated hours format!",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        // Validate due date if provided
        String dueDateStr = txtDueDate.getText().trim();
        if (!dueDateStr.isEmpty()) {
            try {
                LocalDate.parse(dueDateStr);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Due Date format! Use YYYY-MM-DD",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                txtDueDate.requestFocus();
                return false;
            }
        }

        return true;
    }

    private String getCurrentUsername() {
        if (parentFrame instanceof MainFrame) {
            return ((MainFrame) parentFrame).getCurrentUsername();
        } else if (parentFrame instanceof TaskMainFrame) {
            return ((TaskMainFrame) parentFrame).getCurrentUsername();
        }
        return "Unknown User";
    }

    // ==================== DEPENDENCY SECTION ====================

    private JPanel createDependencySection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(StyleUtil.SURFACE);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(StyleUtil.BORDER, 2),
                        "Task Dependencies (Optional)",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        StyleUtil.FONT_SUBHEADING,
                        StyleUtil.TEXT_PRIMARY
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(550, 220));

        JLabel warningLabel = new JLabel("<html><b>⚠ Warning:</b> This task cannot depend on itself!</html>");
        warningLabel.setFont(StyleUtil.FONT_SMALL);
        warningLabel.setForeground(StyleUtil.DANGER);
        warningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(warningLabel);
        section.add(Box.createVerticalStrut(5));

        JLabel instructionLabel = new JLabel("<html>Select tasks that must be completed before this task can start.<br/>Hold Ctrl/Cmd to select multiple tasks.</html>");
        instructionLabel.setFont(StyleUtil.FONT_SMALL);
        instructionLabel.setForeground(StyleUtil.TEXT_SECONDARY);
        instructionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(instructionLabel);
        section.add(Box.createVerticalStrut(8));

        // Create list model and JList
        dependencyListModel = new DefaultListModel<>();
        dependencyList = new JList<>(dependencyListModel);
        dependencyList.setFont(StyleUtil.FONT_BODY);
        dependencyList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        dependencyList.setVisibleRowCount(4);
        dependencyList.setBackground(Color.WHITE);
        dependencyList.setForeground(StyleUtil.TEXT_PRIMARY);

        // Custom cell renderer to show task info
        dependencyList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Task) {
                    Task t = (Task) value;
                    setText(String.format("#%d - %s [%s]",
                            t.getTaskId(),
                            t.getTaskName(),
                            getStatusName(t.getStatusId())));
                }
                if (isSelected) {
                    setBackground(StyleUtil.PRIMARY_LIGHT);
                    setForeground(StyleUtil.TEXT_PRIMARY);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(StyleUtil.TEXT_PRIMARY);
                }
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(dependencyList);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(scrollPane);

        // Load available tasks and pre-select existing dependencies
        loadAvailableTasksForDependencies();

        return section;
    }

    private void loadAvailableTasksForDependencies() {
        dependencyListModel.clear();
        System.out.println("📋 [EDIT] Loading tasks for dependencies...");

        if (task == null || task.getProjectId() == null) {
            System.out.println("⚠ [EDIT] No task or project");
            return;
        }

        System.out.println("✓ [EDIT] Current task: #" + task.getTaskId() + " - " + task.getTaskName());
        System.out.println("✓ [EDIT] Project ID: " + task.getProjectId());

        // Load all tasks from same project
        List<Task> projectTasks = taskDao.findByProject(task.getProjectId());
        System.out.println("✓ [EDIT] Found " + projectTasks.size() + " tasks in this project");

        // Load existing dependencies
        originalDependencyIds = taskDependencyDao.getDependencyIdsForTask(task.getTaskId());
        System.out.println("✓ [EDIT] Existing dependencies: " + originalDependencyIds);

        List<Integer> indicesToSelect = new ArrayList<>();
        int index = 0;

        for (Task t : projectTasks) {
            // Exclude the current task itself (prevent self-dependency)
            if (t.getTaskId() != task.getTaskId()) {
                dependencyListModel.addElement(t);
                System.out.println("  - Added task: #" + t.getTaskId() + " - " + t.getTaskName());

                // Mark for selection if it's an existing dependency
                if (originalDependencyIds.contains(t.getTaskId())) {
                    indicesToSelect.add(index);
                    System.out.println("    └─ Will pre-select (existing dependency)");
                }
                index++;
            }
        }

        // Pre-select existing dependencies
        if (!indicesToSelect.isEmpty()) {
            int[] indices = indicesToSelect.stream().mapToInt(Integer::intValue).toArray();
            dependencyList.setSelectedIndices(indices);
            System.out.println("✓ [EDIT] Pre-selected " + indicesToSelect.size() + " existing dependencies");
        }
    }

    private String getStatusName(int statusId) {
        TaskStatus status = taskStatusDao.findById(statusId);
        return status != null ? status.getStatusName() : "Unknown";
    }

    private void updateDependencies() {
        // Get currently selected dependencies
        List<Task> selectedDependencies = dependencyList.getSelectedValuesList();
        List<Integer> newDependencyIds = new ArrayList<>();

        for (Task t : selectedDependencies) {
            newDependencyIds.add(t.getTaskId());
        }

        // Find dependencies to remove (in original but not in new)
        for (Integer oldDepId : originalDependencyIds) {
            if (!newDependencyIds.contains(oldDepId)) {
                taskDependencyDao.delete(task.getTaskId(), oldDepId);
                System.out.println("✗ Removed dependency: Task " + task.getTaskId() +
                        " no longer depends on Task " + oldDepId);
            }
        }

        // Find dependencies to add (in new but not in original)
        for (Integer newDepId : newDependencyIds) {
            if (!originalDependencyIds.contains(newDepId)) {
                taskDependencyDao.addDependency(task.getTaskId(), newDepId);
                System.out.println("✓ Added dependency: Task " + task.getTaskId() +
                        " now depends on Task " + newDepId);
            }
        }

        int removed = (int) originalDependencyIds.stream().filter(id -> !newDependencyIds.contains(id)).count();
        int added = (int) newDependencyIds.stream().filter(id -> !originalDependencyIds.contains(id)).count();

        if (removed > 0 || added > 0) {
            System.out.println("✓ Dependencies updated: " + added + " added, " + removed + " removed");
        }
    }
}