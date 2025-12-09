package com.ahmet.tpm.taskFrames.tasks;

import com.ahmet.tpm.taskFrames.TaskMainFrame;
import com.ahmet.tpm.utils.StyleUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Container panel for all task-related views
 * Uses CardLayout to switch between TaskList and TaskDetails
 * Follows the same pattern as ProjectsModulePanel
 */
public class TasksModulePanel extends JPanel {

    private CardLayout cardLayout;
    private JPanel contentPanel;

    // Child panels
    private TaskListPanel taskListPanel;
    private TaskDetailsPanel taskDetailsPanel;

    // Reference to main frame
    private TaskMainFrame mainFrame;

    // Card names (route names)
    private static final String CARD_LIST = "TASK_LIST";
    private static final String CARD_DETAILS = "TASK_DETAILS";

    public TasksModulePanel(TaskMainFrame mainFrame) {
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
        taskListPanel = new TaskListPanel(this, mainFrame);
        taskDetailsPanel = new TaskDetailsPanel(this, mainFrame);

        // Add panels to card layout
        contentPanel.add(taskListPanel, CARD_LIST);
        contentPanel.add(taskDetailsPanel, CARD_DETAILS);

        add(contentPanel, BorderLayout.CENTER);

        // Show list by default
        showTaskList();
    }

    // ==================== NAVIGATION METHODS ====================

    /**
     * Navigate to task list view
     */
    public void showTaskList() {
        taskListPanel.refreshData();
        cardLayout.show(contentPanel, CARD_LIST);
    }

    /**
     * Navigate to task details view
     */
    public void showTaskDetails(int taskId) {
        try {
            taskDetailsPanel.loadTask(taskId);
            cardLayout.show(contentPanel, CARD_DETAILS);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Error loading task: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Open Create Task Dialog (modal pop-up)
     */
    public void openCreateTaskDialog() {
        CreateTaskDialog dialog = new CreateTaskDialog(mainFrame, this);
        dialog.setVisible(true);
    }

    /**
     * Open Create Task Dialog for a specific project
     */
    public void openCreateTaskDialog(int projectId) {
        CreateTaskDialog dialog = new CreateTaskDialog(mainFrame, this, projectId);
        dialog.setVisible(true);
    }

    /**
     * Open Edit Task Dialog (modal pop-up)
     */
    public void openEditTaskDialog(int taskId) {
        EditTaskDialog dialog = new EditTaskDialog(mainFrame, this, taskId);
        dialog.setVisible(true);
    }

    /**
     * Open Manage Assignees Dialog (modal pop-up)
     */
    public void openManageAssigneesDialog(int taskId) {
        try {
            ManageAssigneesDialog dialog = new ManageAssigneesDialog(mainFrame, this, taskId);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Error opening manage assignees dialog: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Called by dialogs after successful task save
     */
    public void onTaskSaved() {
        showTaskList();
    }

    /**
     * Called by TaskDetailsPanel after task updated
     */
    public void onTaskUpdated() throws SQLException {
        taskDetailsPanel.refreshCurrentTask();
    }

    /**
     * Get the main frame reference
     */
    public TaskMainFrame getMainFrame() {
        return mainFrame;
    }
}