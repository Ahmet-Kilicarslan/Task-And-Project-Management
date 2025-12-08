package com.ahmet.tpm.projectFrames.projects;

import com.ahmet.tpm.dao.ProjectDao;
import com.ahmet.tpm.dao.ProjectStatusDao;
import com.ahmet.tpm.dao.UserDao;
import com.ahmet.tpm.dao.TaskDao;
import com.ahmet.tpm.dao.DepartmentDao;
import com.ahmet.tpm.dao.ProjectMemberDao;
import com.ahmet.tpm.projectFrames.MainFrame;
import com.ahmet.tpm.models.Project;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;
import com.ahmet.tpm.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class ProjectDetailsPanel extends JPanel {

    private ProjectsModulePanel parentModule;
    private MainFrame mainFrame;

    // DAOs
    private ProjectDao projectDao;
    private ProjectStatusDao statusDao;
    private UserDao userDao;
    private TaskDao taskDao;
    private DepartmentDao departmentDao;
    private ProjectMemberDao projectMemberDao;

    // Current project
    private Project currentProject;

    // UI Components
    private JLabel lblProjectName;
    private JLabel lblStatus;
    private JLabel lblStartDate;
    private JLabel lblDeadline;
    private JLabel lblDepartment;
    private JLabel lblCreatedBy;
    private JLabel lblCreatedAt;
    private JTextArea txtDescription;
    private JLabel lblTotalTasks;
    private JLabel lblCompletedTasks;
    private JLabel lblInProgressTasks;
    private JLabel lblTotalMembers;

    public ProjectDetailsPanel(ProjectsModulePanel parentModule, MainFrame mainFrame) {
        this.parentModule = parentModule;
        this.mainFrame = mainFrame;
        this.projectDao = new ProjectDao();
        this.statusDao = new ProjectStatusDao();
        this.userDao = new UserDao();
        this.taskDao = new TaskDao();
        this.departmentDao = new DepartmentDao();
        this.projectMemberDao = new ProjectMemberDao();

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
        JButton btnBack = ComponentFactory.createSecondaryButton("← Back to List");
        btnBack.addActionListener(e -> parentModule.showProjectList());
        panel.add(btnBack, BorderLayout.WEST);

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(StyleUtil.SURFACE);

        JButton btnEdit = ComponentFactory.createPrimaryButton("✏️ Edit");
        btnEdit.addActionListener(e -> editProject());

        JButton btnMembers = ComponentFactory.createSecondaryButton("👥 Members");
        btnMembers.addActionListener(e -> manageMembers());

        JButton btnDelete = ComponentFactory.createDangerButton("🗑️ Delete");
        btnDelete.addActionListener(e -> deleteProject());

        actionPanel.add(btnEdit);
        actionPanel.add(btnMembers);
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
        lblProjectName = ComponentFactory.createTitleLabel("Project Name");
        lblProjectName.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblProjectName);
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

        // Statistics Card
        JPanel statsCard = createStatsCard();
        statsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(statsCard);

        return panel;
    }

    private JPanel createBasicInfoCard() {
        JPanel card = ComponentFactory.createCard();
        card.setLayout(new GridLayout(7, 2, 15, 15));
        card.setMaximumSize(new Dimension(800, 300));

        // Status
        card.add(ComponentFactory.createBodyLabel("Status:"));
        lblStatus = ComponentFactory.createBodyLabel("-");
        lblStatus.setForeground(StyleUtil.PRIMARY);
        lblStatus.setFont(StyleUtil.FONT_BUTTON);
        card.add(lblStatus);

        // Start Date
        card.add(ComponentFactory.createBodyLabel("Start Date:"));
        lblStartDate = ComponentFactory.createBodyLabel("-");
        lblStartDate.setForeground(StyleUtil.TEXT_PRIMARY);
        card.add(lblStartDate);

        // Deadline
        card.add(ComponentFactory.createBodyLabel("Deadline:"));
        lblDeadline = ComponentFactory.createBodyLabel("-");
        lblDeadline.setForeground(StyleUtil.TEXT_PRIMARY);
        card.add(lblDeadline);

        // Department
        card.add(ComponentFactory.createBodyLabel("Department:"));
        lblDepartment = ComponentFactory.createBodyLabel("-");
        lblDepartment.setForeground(StyleUtil.TEXT_PRIMARY);
        card.add(lblDepartment);

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

        // Total Members
        card.add(ComponentFactory.createBodyLabel("Team Members:"));
        lblTotalMembers = ComponentFactory.createBodyLabel("-");
        lblTotalMembers.setForeground(StyleUtil.TEXT_PRIMARY);
        card.add(lblTotalMembers);

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

    private JPanel createStatsCard() {
        JPanel card = ComponentFactory.createCard();
        card.setLayout(new BorderLayout(0, 15));
        card.setMaximumSize(new Dimension(800, 150));

        JLabel titleLabel = ComponentFactory.createHeadingLabel("📊 Task Statistics");
        card.add(titleLabel, BorderLayout.NORTH);

        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        statsGrid.setBackground(StyleUtil.SURFACE);

        // Total Tasks
        JPanel totalPanel = createStatBox("Total Tasks", "0", StyleUtil.PRIMARY);
        lblTotalTasks = (JLabel) totalPanel.getComponent(0);

        // Completed Tasks
        JPanel completedPanel = createStatBox("Completed", "0", StyleUtil.SUCCESS);
        lblCompletedTasks = (JLabel) completedPanel.getComponent(0);

        // In Progress Tasks
        JPanel inProgressPanel = createStatBox("In Progress", "0", StyleUtil.INFO);
        lblInProgressTasks = (JLabel) inProgressPanel.getComponent(0);

        statsGrid.add(totalPanel);
        statsGrid.add(completedPanel);
        statsGrid.add(inProgressPanel);

        card.add(statsGrid, BorderLayout.CENTER);

        return card;
    }

    private JPanel createStatBox(String label, String value, Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(StyleUtil.BACKGROUND);
        panel.setBorder(StyleUtil.createPaddingBorder(15));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel labelLabel = ComponentFactory.createBodyLabel(label);
        labelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(valueLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(labelLabel);

        return panel;
    }

    public void loadProject(int projectId) throws SQLException {
        currentProject = projectDao.findById(projectId);

        if (currentProject == null) {
            UIHelper.showError(mainFrame, "Project not found!");
            parentModule.showProjectList();
            return;
        }

        displayProjectData();
        loadTaskStatistics();
        loadMemberCount();
    }

    private void displayProjectData() throws SQLException {
        // Project Name
        lblProjectName.setText("📄 " + currentProject.getProjectName());

        // Status
        var status = statusDao.findById(currentProject.getStatusId());
        lblStatus.setText(status != null ? status.getStatusName() : "Unknown");

        // Dates
        lblStartDate.setText(currentProject.getStartDate() != null ?
                currentProject.getStartDate().toString() : "Not set");

        lblDeadline.setText(currentProject.getDeadline() != null ?
                currentProject.getDeadline().toString() : "Not set");

        // Department
        if (currentProject.getDepartmentId() != null) {
            var department = departmentDao.findById(currentProject.getDepartmentId());
            lblDepartment.setText(department != null ? department.getDepartmentName() : "Unknown");
        } else {
            lblDepartment.setText("No department assigned");
        }

        // Creator
        var creator = userDao.findById(currentProject.getCreatedBy());
        lblCreatedBy.setText(creator != null ? creator.getFullName() : "Unknown");

        // Created At
        lblCreatedAt.setText(currentProject.getCreatedAt() != null ?
                currentProject.getCreatedAt().toString() : "-");

        // Description
        txtDescription.setText(currentProject.getDescription() != null ?
                currentProject.getDescription() : "No description provided.");
    }

    private void loadTaskStatistics() {
        if (currentProject == null) return;

        int projectId = currentProject.getProjectId();

        // Total tasks for this project
        int totalTasks = taskDao.countByProject(projectId);
        lblTotalTasks.setText(String.valueOf(totalTasks));

        // Completed tasks (status_id = 4 for "DONE" - adjust based on your data)
        int completedTasks = taskDao.countByProjectAndStatus(projectId, 4);
        lblCompletedTasks.setText(String.valueOf(completedTasks));

        // In Progress tasks (status_id = 2 for "IN PROGRESS" - adjust based on your data)
        int inProgressTasks = taskDao.countByProjectAndStatus(projectId, 2);
        lblInProgressTasks.setText(String.valueOf(inProgressTasks));
    }

    private void loadMemberCount() {
        if (currentProject == null) return;

        int memberCount = projectMemberDao.countMembersInProject(currentProject.getProjectId());
        lblTotalMembers.setText(memberCount + " member" + (memberCount != 1 ? "s" : ""));
    }

    private void editProject() {
        if (currentProject != null) {
            parentModule.openEditProjectDialog(currentProject.getProjectId());
        }
    }

    private void manageMembers() {
        if (currentProject != null) {
            parentModule.openManageMembersDialog(currentProject.getProjectId());
        }
    }

    private void deleteProject() {
        if (currentProject == null) return;

        boolean confirm = UIHelper.showConfirmDialog(mainFrame,
                "Are you sure you want to delete this project?\n" +
                        "Project: " + currentProject.getProjectName() + "\n\n" +
                        "This will also delete all associated tasks and data.",
                "Confirm Delete");

        if (confirm) {
            try {
                projectDao.delete(currentProject.getProjectId());
                UIHelper.showSuccess(mainFrame, "Project deleted successfully!");
                parentModule.showProjectList();
            } catch (Exception e) {
                UIHelper.showError(mainFrame, "Error deleting project: " + e.getMessage());
            }
        }
    }

    public void refreshCurrentProject() throws SQLException {
        if (currentProject != null) {
            loadProject(currentProject.getProjectId());
        }
    }
}
