package com.ahmet.tpm.taskFrames.tasks;

import com.ahmet.tpm.dao.*;
import com.ahmet.tpm.taskFrames.TaskMainFrame;
import com.ahmet.tpm.models.*;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;
import com.ahmet.tpm.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class CreateTaskDialog extends JDialog {

    private TasksModulePanel parentModule;
    private TaskMainFrame mainFrame;

    // DAOs
    private TaskDao taskDao;
    private ProjectDao projectDao;
    private TaskStatusDao taskStatusDao;
    private TaskPriorityDao taskPriorityDao;
    private TaskDependencyDao taskDependencyDao;

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

    // Optional: preselected project
    private Integer preselectedProjectId;

    public CreateTaskDialog(TaskMainFrame mainFrame, TasksModulePanel parentModule) {
        this(mainFrame, parentModule, null);
    }

    public CreateTaskDialog(TaskMainFrame mainFrame, TasksModulePanel parentModule, Integer projectId) {
        super(mainFrame, "Create New Task", true);
        this.mainFrame = mainFrame;
        this.parentModule = parentModule;
        this.preselectedProjectId = projectId;
        this.taskDao = new TaskDao();
        this.projectDao = new ProjectDao();
        this.taskStatusDao = new TaskStatusDao();
        this.taskPriorityDao = new TaskPriorityDao();
        this.taskDependencyDao = new TaskDependencyDao();

        initializeDialog();
        loadDropdownData();

        if (preselectedProjectId != null) {
            preselectProject();
        }
    }

    private void initializeDialog() {
        setSize(600, 900);  // Increased height for dependency section
        setLocationRelativeTo(mainFrame);
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
        JLabel titleLabel = ComponentFactory.createHeadingLabel("Create New Task");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));

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

        // Add listener to refresh available tasks when project changes
        cmbProject.addActionListener(e -> loadAvailableTasksForDependencies());

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

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panel.setBackground(StyleUtil.SURFACE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, StyleUtil.BORDER));

        JButton btnSave = ComponentFactory.createPrimaryButton("Create Task");
        btnSave.addActionListener(e -> saveTask());

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

        // Set default to "TODO"
        for (int i = 0; i < cmbStatus.getItemCount(); i++) {
            if (cmbStatus.getItemAt(i).getStatusName().equals("TODO")) {
                cmbStatus.setSelectedIndex(i);
                break;
            }
        }

        // Load priorities
        List<TaskPriority> priorities = taskPriorityDao.findAll();
        for (TaskPriority priority : priorities) {
            cmbPriority.addItem(priority);
        }

        // Set default to "MEDIUM"
        for (int i = 0; i < cmbPriority.getItemCount(); i++) {
            if (cmbPriority.getItemAt(i).getPriorityName().equals("MEDIUM")) {
                cmbPriority.setSelectedIndex(i);
                break;
            }
        }
    }

    private void preselectProject() {
        for (int i = 0; i < cmbProject.getItemCount(); i++) {
            Project p = cmbProject.getItemAt(i);
            if (p.getProjectId() == preselectedProjectId) {
                cmbProject.setSelectedIndex(i);
                break;
            }
        }
    }

    private void saveTask() {
        if (!validateForm()) {
            return;
        }

        try {
            // Create task object
            Task task = new Task();

            // Project
            Project selectedProject = (Project) cmbProject.getSelectedItem();
            task.setProjectId(selectedProject.getProjectId());

            // Task name
            task.setTaskName(txtTaskName.getText().trim());

            // Description
            String description = txtDescription.getText().trim();
            if (!description.isEmpty()) {
                task.setDescription(description);
            }

            // Status
            TaskStatus selectedStatus = (TaskStatus) cmbStatus.getSelectedItem();
            task.setStatusId(selectedStatus.getStatusId());

            // Priority
            TaskPriority selectedPriority = (TaskPriority) cmbPriority.getSelectedItem();
            task.setPriorityId(selectedPriority.getPriorityId());

            // Estimated hours
            String hoursStr = txtEstimatedHours.getText().trim();
            if (!hoursStr.isEmpty()) {
                task.setEstimatedHours(Double.parseDouble(hoursStr));
            }

            // Due date
            String dueDateStr = txtDueDate.getText().trim();
            if (!dueDateStr.isEmpty()) {
                LocalDate date = LocalDate.parse(dueDateStr);
                task.setDueDate(date);
            }

            // Created by
            task.setCreatedBy(mainFrame.getCurrentUserId());

            // Save to database and get the new task ID
            Integer newTaskId = taskDao.insertAndGetId(task);

            if (newTaskId != null) {
                // Save dependencies
                saveDependencies(newTaskId);
            }

            UIHelper.showSuccess(mainFrame, "Task created successfully!");

            // Notify parent and close
            parentModule.onTaskSaved();
            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid number format for estimated hours!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving task: " + e.getMessage(),
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
        section.setMaximumSize(new Dimension(550, 200));

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
                    Task task = (Task) value;
                    setText(String.format("#%d - %s [%s]",
                            task.getTaskId(),
                            task.getTaskName(),
                            getStatusName(task.getStatusId())));
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

        return section;
    }

    private void loadAvailableTasksForDependencies() {
        dependencyListModel.clear();

        Project selectedProject = (Project) cmbProject.getSelectedItem();
        System.out.println("📋 Loading tasks for dependencies...");

        if (selectedProject == null) {
            System.out.println("⚠ No project selected");
            return;
        }

        System.out.println("✓ Selected project: " + selectedProject.getProjectName() +
                " (ID: " + selectedProject.getProjectId() + ")");

        // Load all tasks from selected project
        List<Task> projectTasks = taskDao.findByProject(selectedProject.getProjectId());
        System.out.println("✓ Found " + projectTasks.size() + " tasks in this project");

        for (Task task : projectTasks) {
            dependencyListModel.addElement(task);
            System.out.println("  - Added task: #" + task.getTaskId() + " - " + task.getTaskName());
        }
    }

    private String getStatusName(int statusId) {
        TaskStatus status = taskStatusDao.findById(statusId);
        return status != null ? status.getStatusName() : "Unknown";
    }

    private void saveDependencies(int newTaskId) {
        List<Task> selectedDependencies = dependencyList.getSelectedValuesList();

        for (Task dependency : selectedDependencies) {
            taskDependencyDao.addDependency(newTaskId, dependency.getTaskId());
            System.out.println("✓ Added dependency: Task " + newTaskId +
                    " depends on Task " + dependency.getTaskId());
        }

        if (!selectedDependencies.isEmpty()) {
            System.out.println("✓ Total dependencies added: " + selectedDependencies.size());
        }
    }}