package com.ahmet.tpm.taskFrames.tasks;

import com.ahmet.tpm.dao.*;
import com.ahmet.tpm.models.Project;
import com.ahmet.tpm.models.Task;
import com.ahmet.tpm.taskFrames.TaskMainFrame;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TaskListPanel extends JPanel {

    private TasksModulePanel parentModule;
    private TaskMainFrame mainFrame;

    // DAOs
    private TaskDao taskDao;
    private ProjectDao projectDao;
    private TaskStatusDao statusDao;
    private TaskPriorityDao priorityDao;

    // UI Components
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> projectFilter;
    private JComboBox<String> statusFilter;
    private JComboBox<String> priorityFilter;

    public TaskListPanel(TasksModulePanel parentModule, TaskMainFrame mainFrame) {
        this.parentModule = parentModule;
        this.mainFrame = mainFrame;
        this.taskDao = new TaskDao();
        this.projectDao = new ProjectDao();
        this.statusDao = new TaskStatusDao();
        this.priorityDao = new TaskPriorityDao();

        setLayout(new BorderLayout());
        setBackground(StyleUtil.BACKGROUND);

        initializeUI();
        loadTasks();
    }

    private void initializeUI() {
        // Top toolbar
        JPanel toolbar = createToolbar();
        add(toolbar, BorderLayout.NORTH);

        // Center - Table
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setBackground(StyleUtil.SURFACE);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, StyleUtil.BORDER));

        // Title
        JLabel titleLabel = ComponentFactory.createHeadingLabel("Tasks");
        toolbar.add(titleLabel);
        toolbar.add(Box.createHorizontalStrut(20));

        // Create button
        JButton btnCreate = ComponentFactory.createPrimaryButton("Create New");
        btnCreate.addActionListener(e -> parentModule.openCreateTaskDialog());
        toolbar.add(btnCreate);
        toolbar.add(Box.createHorizontalStrut(10));

        // Refresh button
        JButton btnRefresh = ComponentFactory.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshData());
        toolbar.add(btnRefresh);
        toolbar.add(Box.createHorizontalStrut(20));

        // Search
        toolbar.add(new JLabel("Search:"));
        toolbar.add(Box.createHorizontalStrut(5));

        searchField = new JTextField(15);
        searchField.setMaximumSize(new Dimension(200, 35));
        searchField.setFont(StyleUtil.FONT_BODY);
        searchField.setBackground(Color.WHITE);
        searchField.setForeground(StyleUtil.TEXT_PRIMARY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        toolbar.add(searchField);

        toolbar.add(Box.createHorizontalStrut(20));

        // Project filter
        toolbar.add(new JLabel("Project:"));
        toolbar.add(Box.createHorizontalStrut(5));

        projectFilter = new JComboBox<>();
        projectFilter.setFont(StyleUtil.FONT_BODY);
        projectFilter.setBackground(Color.WHITE);
        projectFilter.setForeground(StyleUtil.TEXT_PRIMARY);
        loadProjectFilter();
        projectFilter.setMaximumSize(new Dimension(150, 35));
        toolbar.add(projectFilter);

        toolbar.add(Box.createHorizontalStrut(20));

        // Status filter
        statusFilter = new JComboBox<>(new String[]{"All Status", "TODO", "IN PROGRESS", "IN REVIEW", "DONE"});
        statusFilter.setFont(StyleUtil.FONT_BODY);
        statusFilter.setBackground(Color.WHITE);
        statusFilter.setForeground(StyleUtil.TEXT_PRIMARY);
        statusFilter.setMaximumSize(new Dimension(150, 35));
        toolbar.add(statusFilter);

        toolbar.add(Box.createHorizontalStrut(20));

        // Priority filter
        priorityFilter = new JComboBox<>(new String[]{"All Priority", "LOW", "MEDIUM", "HIGH", "CRITICAL"});
        priorityFilter.setFont(StyleUtil.FONT_BODY);
        priorityFilter.setBackground(Color.WHITE);
        priorityFilter.setForeground(StyleUtil.TEXT_PRIMARY);
        priorityFilter.setMaximumSize(new Dimension(150, 35));
        toolbar.add(priorityFilter);

        toolbar.add(Box.createHorizontalStrut(20));

        // Search button
        JButton btnSearch = ComponentFactory.createSecondaryButton("Filter");
        btnSearch.addActionListener(e -> filterTasks());
        toolbar.add(btnSearch);

        // Right side boşluk (her şeyi sola yaslar)
        toolbar.add(Box.createHorizontalGlue());

        return toolbar;
    }


    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(StyleUtil.BACKGROUND);
        panel.setBorder(StyleUtil.createPaddingBorder(20));

        // Table model
        String[] columnNames = {"ID", "Task Name", "Project", "Status", "Priority", "Due Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        taskTable = new JTable(tableModel);
        taskTable.setFont(StyleUtil.FONT_BODY);
        taskTable.setRowHeight(35);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.getTableHeader().setFont(StyleUtil.FONT_BUTTON);
        taskTable.getTableHeader().setBackground(StyleUtil.PRIMARY_LIGHT);
        taskTable.setBackground(Color.WHITE);
        taskTable.setForeground(StyleUtil.TEXT_PRIMARY);
        taskTable.setGridColor(StyleUtil.BORDER);
        taskTable.setSelectionBackground(StyleUtil.PRIMARY_LIGHT);
        taskTable.setSelectionForeground(StyleUtil.TEXT_PRIMARY);

        // Column widths
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        taskTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        taskTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        // Double-click to view details
        taskTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = taskTable.getSelectedRow();
                    if (row != -1) {
                        int taskId = (int) tableModel.getValueAt(row, 0);
                        parentModule.showTaskDetails(taskId);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBorder(StyleUtil.createLineBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(StyleUtil.BACKGROUND);
        bottomPanel.setBorder(StyleUtil.createPaddingBorder(10, 0, 0, 0));

        JLabel statsLabel = ComponentFactory.createBodyLabel("Total Tasks: 0");
        statsLabel.setName("statsLabel");
        bottomPanel.add(statsLabel, BorderLayout.WEST);

        JButton btnViewSelected = ComponentFactory.createPrimaryButton("View Selected");
        btnViewSelected.addActionListener(e -> viewSelectedTask());
        bottomPanel.add(btnViewSelected, BorderLayout.EAST);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadProjectFilter() {
        projectFilter.removeAllItems();
        projectFilter.addItem("All Projects");

        List<Project> projects = projectDao.findAll();
        for (Project project : projects) {
            projectFilter.addItem(project.getProjectName());
        }
    }

    private void loadTasks() {
        tableModel.setRowCount(0);

        List<Task> tasks = taskDao.findAll();

        for (Task task : tasks) {
            Object[] row = {
                    task.getTaskId(),
                    task.getTaskName(),
                    getProjectName(task.getProjectId()),
                    getStatusName(task.getStatusId()),
                    getPriorityName(task.getPriorityId()),
                    task.getDueDate() != null ? task.getDueDate().toString() : "-"
            };
            tableModel.addRow(row);
        }

        updateStats(tasks.size());
    }

    private void filterTasks() {
        String searchText = searchField.getText().trim().toLowerCase();
        String selectedProject = (String) projectFilter.getSelectedItem();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        String selectedPriority = (String) priorityFilter.getSelectedItem();

        tableModel.setRowCount(0);

        List<Task> tasks = taskDao.findAll();
        int count = 0;

        for (Task task : tasks) {
            // Filter by search
            if (!searchText.isEmpty()) {
                if (!task.getTaskName().toLowerCase().contains(searchText)) {
                    continue;
                }
            }

            // Filter by project
            if (!selectedProject.equals("All Projects")) {
                String taskProjectName = getProjectName(task.getProjectId());
                if (!taskProjectName.equals(selectedProject)) {
                    continue;
                }
            }

            // Filter by status
            if (!selectedStatus.equals("All Status")) {
                String taskStatus = getStatusName(task.getStatusId());
                if (!taskStatus.equals(selectedStatus)) {
                    continue;
                }
            }

            // Filter by priority
            if (!selectedPriority.equals("All Priority")) {
                String taskPriority = getPriorityName(task.getPriorityId());
                if (!taskPriority.equals(selectedPriority)) {
                    continue;
                }
            }

            Object[] row = {
                    task.getTaskId(),
                    task.getTaskName(),
                    getProjectName(task.getProjectId()),
                    getStatusName(task.getStatusId()),
                    getPriorityName(task.getPriorityId()),
                    task.getDueDate() != null ? task.getDueDate().toString() : "-"
            };
            tableModel.addRow(row);
            count++;
        }

        updateStats(count);
    }

    private void viewSelectedTask() {
        int row = taskTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a task to view",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int taskId = (int) tableModel.getValueAt(row, 0);
        parentModule.showTaskDetails(taskId);
    }

    private String getProjectName(int projectId) {
        Project project = projectDao.findById(projectId);
        return project != null ? project.getProjectName() : "Unknown";
    }

    private String getStatusName(int statusId) {
        var status = statusDao.findById(statusId);
        return status != null ? status.getStatusName() : "Unknown";
    }

    private String getPriorityName(int priorityId) {
        var priority = priorityDao.findById(priorityId);
        return priority != null ? priority.getPriorityName() : "Unknown";
    }

    private void updateStats(int count) {
        Component[] components = ((JPanel) getComponent(1)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                for (Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JLabel && "statsLabel".equals(subComp.getName())) {
                        ((JLabel) subComp).setText("Total Tasks: " + count);
                    }
                }
            }
        }
    }

    public void refreshData() {
        loadTasks();
        loadProjectFilter();
    }
}