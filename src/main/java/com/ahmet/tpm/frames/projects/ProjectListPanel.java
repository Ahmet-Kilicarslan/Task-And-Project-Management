package com.ahmet.tpm.frames.projects;

import com.ahmet.tpm.dao.ProjectDao;
import com.ahmet.tpm.dao.ProjectStatusDao;
import com.ahmet.tpm.frames.MainFrame;
import com.ahmet.tpm.models.Project;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProjectListPanel extends JPanel {

    private ProjectsModulePanel parentModule;
    private MainFrame mainFrame;

    // DAOs
    private ProjectDao projectDao;
    private ProjectStatusDao statusDao;

    // UI Components
    private JTable projectTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;

    public ProjectListPanel(ProjectsModulePanel parentModule, MainFrame mainFrame) {
        this.parentModule = parentModule;
        this.mainFrame = mainFrame;
        this.projectDao = new ProjectDao();
        this.statusDao = new ProjectStatusDao();

        setLayout(new BorderLayout());
        setBackground(StyleUtil.BACKGROUND);

        initializeUI();
        loadProjects();
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
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        toolbar.setBackground(StyleUtil.SURFACE);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, StyleUtil.BORDER));

        // Title
        JLabel titleLabel = ComponentFactory.createHeadingLabel("📁 Projects");
        toolbar.add(titleLabel);

        toolbar.add(Box.createHorizontalStrut(20));

        // Create button
        JButton btnCreate = ComponentFactory.createPrimaryButton("➕ Create New");
        btnCreate.addActionListener(e -> parentModule.openCreateProjectDialog());
        toolbar.add(btnCreate);

        // Refresh button
        JButton btnRefresh = ComponentFactory.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> refreshData());
        toolbar.add(btnRefresh);

        toolbar.add(Box.createHorizontalStrut(20));

        // Search field
        searchField = new JTextField(20);
        searchField.setFont(StyleUtil.FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        toolbar.add(new JLabel("🔍"));
        toolbar.add(searchField);

        // Status filter
        statusFilter = new JComboBox<>(new String[]{"All Status", "Planning", "Active", "Completed", "Cancelled"});
        statusFilter.setFont(StyleUtil.FONT_BODY);
        statusFilter.addActionListener(e -> filterProjects());
        toolbar.add(statusFilter);

        // Search button
        JButton btnSearch = ComponentFactory.createSecondaryButton("Search");
        btnSearch.addActionListener(e -> filterProjects());
        toolbar.add(btnSearch);

        return toolbar;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(StyleUtil.BACKGROUND);
        panel.setBorder(StyleUtil.createPaddingBorder(20));

        // Table model - Updated columns (removed Code and Budget)
        String[] columnNames = {"ID", "Project Name", "Status", "Start Date", "Deadline", "Department"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Read-only table
            }
        };

        projectTable = new JTable(tableModel);
        projectTable.setFont(StyleUtil.FONT_BODY);
        projectTable.setRowHeight(35);
        projectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectTable.getTableHeader().setFont(StyleUtil.FONT_BUTTON);
        projectTable.getTableHeader().setBackground(StyleUtil.PRIMARY_LIGHT);

        // Set column widths
        projectTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        projectTable.getColumnModel().getColumn(1).setPreferredWidth(250);  // Project Name
        projectTable.getColumnModel().getColumn(2).setPreferredWidth(100);  // Status
        projectTable.getColumnModel().getColumn(3).setPreferredWidth(100);  // Start Date
        projectTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Deadline
        projectTable.getColumnModel().getColumn(5).setPreferredWidth(150);  // Department

        // Double-click to view details
        projectTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = projectTable.getSelectedRow();
                    if (row != -1) {
                        int projectId = (int) tableModel.getValueAt(row, 0);
                        parentModule.showProjectDetails(projectId);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(projectTable);
        scrollPane.setBorder(StyleUtil.createLineBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with stats and actions
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(StyleUtil.BACKGROUND);
        bottomPanel.setBorder(StyleUtil.createPaddingBorder(10, 0, 0, 0));

        JLabel statsLabel = ComponentFactory.createBodyLabel("Total Projects: 0");
        statsLabel.setName("statsLabel");  // For easy reference
        bottomPanel.add(statsLabel, BorderLayout.WEST);

        JButton btnViewSelected = ComponentFactory.createPrimaryButton("👁️ View Selected");
        btnViewSelected.addActionListener(e -> viewSelectedProject());
        bottomPanel.add(btnViewSelected, BorderLayout.EAST);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadProjects() {
        tableModel.setRowCount(0);  // Clear table

        List<Project> projects = projectDao.findAll();

        for (Project project : projects) {
            Object[] row = {
                    project.getProjectId(),
                    project.getProjectName(),
                    getStatusName(project.getStatusId()),
                    project.getStartDate() != null ? project.getStartDate().toString() : "-",
                    project.getDeadline() != null ? project.getDeadline().toString() : "-",
                    getDepartmentName(project.getDepartmentId())
            };
            tableModel.addRow(row);
        }

        updateStats(projects.size());
    }

    private String getStatusName(int statusId) {
        var status = statusDao.findById(statusId);
        return status != null ? status.getStatusName() : "Unknown";
    }

    private String getDepartmentName(Integer departmentId) {
        if (departmentId == null) {
            return "-";
        }
        // You'll need to inject DepartmentDao or pass department name from Project
        return "Dept " + departmentId;  // Placeholder - TODO: Load actual department name
    }

    private void filterProjects() {
        String searchText = searchField.getText().trim().toLowerCase();
        String selectedStatus = (String) statusFilter.getSelectedItem();

        tableModel.setRowCount(0);

        List<Project> projects = projectDao.findAll();
        int count = 0;

        for (Project project : projects) {
            // Filter by search text
            if (!searchText.isEmpty()) {
                if (!project.getProjectName().toLowerCase().contains(searchText)) {
                    continue;
                }
            }

            // Filter by status
            if (!selectedStatus.equals("All Status")) {
                String projectStatus = getStatusName(project.getStatusId());
                if (!projectStatus.equals(selectedStatus)) {
                    continue;
                }
            }

            Object[] row = {
                    project.getProjectId(),
                    project.getProjectName(),
                    getStatusName(project.getStatusId()),
                    project.getStartDate() != null ? project.getStartDate().toString() : "-",
                    project.getDeadline() != null ? project.getDeadline().toString() : "-",
                    getDepartmentName(project.getDepartmentId())
            };
            tableModel.addRow(row);
            count++;
        }

        updateStats(count);
    }

    private void viewSelectedProject() {
        int row = projectTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a project to view",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int projectId = (int) tableModel.getValueAt(row, 0);
        parentModule.showProjectDetails(projectId);
    }

    private void updateStats(int count) {
        // Find stats label and update it
        Component[] components = ((JPanel) getComponent(1)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                for (Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JLabel && "statsLabel".equals(subComp.getName())) {
                        ((JLabel) subComp).setText("Total Projects: " + count);
                    }
                }
            }
        }
    }

    public void refreshData() {
        loadProjects();
    }
}