package com.ahmet.tpm.projectFrames.projects;

import com.ahmet.tpm.projectFrames.MainFrame;
import com.ahmet.tpm.utils.StyleUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Container panel for all project-related views
 * Uses CardLayout to switch between ProjectList and ProjectDetails
 */
public class ProjectsModulePanel extends JPanel {

    private CardLayout cardLayout;
    private JPanel contentPanel;

    // Child panels
    private ProjectListPanel projectListPanel;
    private ProjectDetailsPanel projectDetailsPanel;

    // Reference to main frame (for user info)
    private MainFrame mainFrame;

    // Card names (like route names in Angular)
    private static final String CARD_LIST = "PROJECT_LIST";
    private static final String CARD_DETAILS = "PROJECT_DETAILS";

    public ProjectsModulePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(StyleUtil.BACKGROUND);

        initializeContent();
    }

    private void initializeContent() {
        // Create CardLayout for internal navigation
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(StyleUtil.BACKGROUND);

        // Create child panels (pass reference to this container)
        projectListPanel = new ProjectListPanel(this, mainFrame);
        projectDetailsPanel = new ProjectDetailsPanel(this, mainFrame);

        // Add panels to card layout
        contentPanel.add(projectListPanel, CARD_LIST);
        contentPanel.add(projectDetailsPanel, CARD_DETAILS);

        add(contentPanel, BorderLayout.CENTER);

        // Show list by default
        showProjectList();
    }

    // ==================== NAVIGATION METHODS ====================
    // These are like Angular's router.navigate()

    /**
     * Navigate to project list view
     */
    public void showProjectList() {
        projectListPanel.refreshData();  // Reload data
        cardLayout.show(contentPanel, CARD_LIST);
    }

    /**
     * Navigate to project details view
     */
    public void showProjectDetails(int projectId) {
        try {
            projectDetailsPanel.loadProject(projectId);  // Load specific project
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        cardLayout.show(contentPanel, CARD_DETAILS);
    }

    /**
     * Open Create Project Dialog (modal pop-up)
     */
    public void openCreateProjectDialog() {
        CreateProjectDialog dialog = new CreateProjectDialog(mainFrame, this);
        dialog.setVisible(true);
        // When dialog closes (after save), list will auto-refresh
    }

    /**
     * Open Edit Project Dialog (modal pop-up)
     */
    public void openEditProjectDialog(int projectId) {
        EditProjectDialog dialog = new EditProjectDialog(mainFrame, this, projectId);
        dialog.setVisible(true);
        // When dialog closes, details panel will refresh
    }

    /**
     * Open Manage Members Dialog (modal pop-up)
     */
    public void openManageMembersDialog(int projectId) {
        try {
            ManageMembersDialog dialog = new ManageMembersDialog(mainFrame, this, projectId);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Error opening manage members dialog: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Called by dialogs after successful save
     */
    public void onProjectSaved() {
        showProjectList();  // Navigate back to list and refresh
    }

    /**
     * Called by ProjectDetailsPanel after project updated
     */
    public void onProjectUpdated() throws SQLException {
        // Could refresh current view or show success message
        projectDetailsPanel.refreshCurrentProject();
    }
}