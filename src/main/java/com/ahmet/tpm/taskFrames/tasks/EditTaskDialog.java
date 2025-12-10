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

public class EditTaskDialog extends JDialog {

    private TasksModulePanel parentModule;
    private Frame parentFrame; // ============ Frame (generic) KULLANIYORUZ ============

    // DAOs
    private TaskDao taskDao;
    private ProjectDao projectDao;
    private TaskStatusDao taskStatusDao;
    private TaskPriorityDao taskPriorityDao;

    // ============ BİLDİRİM SERVİSİ ============
    private NotificationService notificationService;

    // Current task being edited
    private Task task;
    private int oldStatusId; // ============ ESKİ STATUS'U SAKLAMAK İÇİN ============

    // Form fields
    private JComboBox<Project> cmbProject;
    private JTextField txtTaskName;
    private JTextArea txtDescription;
    private JComboBox<TaskStatus> cmbStatus;
    private JComboBox<TaskPriority> cmbPriority;
    private JTextField txtEstimatedHours;
    private JTextField txtDueDate;

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

    // ============ ORTAK INITIALIZATION ============
    private void initializeCommon(int taskId) {
        this.taskDao = new TaskDao();
        this.projectDao = new ProjectDao();
        this.taskStatusDao = new TaskStatusDao();
        this.taskPriorityDao = new TaskPriorityDao();

        // ============ BİLDİRİM SERVİSİNİ BAŞLAT ============
        this.notificationService = new NotificationService();

        // Load task
        this.task = taskDao.findById(taskId);

        // ============ ESKİ STATUS'U SAKLA ============
        this.oldStatusId = (this.task != null) ? this.task.getStatusId() : -1;

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
        setSize(600, 750);
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
    }

    private void saveChanges() {
        if (!validateForm()) {
            return;
        }

        try {
            // Update task object

            // Project
            Project selectedProject = (Project) cmbProject.getSelectedItem();
            task.setProjectId(selectedProject.getProjectId());

            // Task name
            task.setTaskName(txtTaskName.getText().trim());

            // Description
            String description = txtDescription.getText().trim();
            task.setDescription(!description.isEmpty() ? description : null);

            // Status
            TaskStatus selectedStatus = (TaskStatus) cmbStatus.getSelectedItem();
            int newStatusId = selectedStatus.getStatusId();
            task.setStatusId(newStatusId);

            // Priority
            TaskPriority selectedPriority = (TaskPriority) cmbPriority.getSelectedItem();
            task.setPriorityId(selectedPriority.getPriorityId());

            // Estimated hours
            String hoursStr = txtEstimatedHours.getText().trim();
            if (!hoursStr.isEmpty()) {
                task.setEstimatedHours(Double.parseDouble(hoursStr));
            } else {
                task.setEstimatedHours(0);
            }

            // Due date
            String dueDateStr = txtDueDate.getText().trim();
            if (!dueDateStr.isEmpty()) {
                LocalDate date = LocalDate.parse(dueDateStr);
                task.setDueDate(date);
            } else {
                task.setDueDate(null);
            }

            // Update in database
            taskDao.update(task);

            // ============ BİLDİRİM GÖNDER - BAŞLANGIÇ ============

            // STATUS DEĞİŞTİYSE BİLDİRİM GÖNDER
            if (oldStatusId != newStatusId) {
                notificationService.notifyTaskStatusChange(
                        task.getTaskId(),
                        task.getTaskName(),
                        selectedStatus.getStatusName(),
                        getCurrentUsername()
                );
            }

            // DONE'A GEÇTİYSE COMPLETION BİLDİRİMİ
            if (newStatusId == 4 && oldStatusId != 4) {  // 4 = DONE status
                notificationService.notifyTaskCompletion(
                        task.getTaskId(),
                        task.getTaskName(),
                        getCurrentUsername()
                );
            }

            // ============ BİLDİRİM GÖNDER - BİTİŞ ============

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

    // ============ HELPER METHOD - getCurrentUsername() ============
    private String getCurrentUsername() {
        if (parentFrame instanceof MainFrame) {
            return ((MainFrame) parentFrame).getCurrentUsername();
        } else if (parentFrame instanceof TaskMainFrame) {
            return ((TaskMainFrame) parentFrame).getCurrentUsername();
        }
        return "Unknown User";
    }
}