package com.ahmet.tpm.projectFrames.projects;

import com.ahmet.tpm.dao.ProjectMemberDao;
import com.ahmet.tpm.dao.UserDao;
import com.ahmet.tpm.dao.ProjectDao;
import com.ahmet.tpm.projectFrames.MainFrame;
import com.ahmet.tpm.models.ProjectMember;
import com.ahmet.tpm.models.User;
import com.ahmet.tpm.models.Project;
import com.ahmet.tpm.service.NotificationService; // ============ BİLDİRİM İMPORT ============
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;
import com.ahmet.tpm.service.NotificationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ManageMembersDialog extends JDialog {

    private ProjectsModulePanel parentModule;
    private MainFrame mainFrame;


    // DAOs
    private ProjectMemberDao projectMemberDao;
    private UserDao userDao;
    private ProjectDao projectDao;

    // ============ BİLDİRİM SERVİSİ ============
    private NotificationService notificationService;

    // Current project
    private int projectId;
    private Project project;

    // UI Components
    private JTable membersTable;
    private DefaultTableModel tableModel;
    private JComboBox<User> cmbUsers;
    private JTextField txtRole;
    private JLabel lblProjectName;
    private JLabel lblMemberCount;

    public ManageMembersDialog(MainFrame mainFrame, ProjectsModulePanel parentModule, int projectId) {
        super(mainFrame, "Manage Project Members", true);
        this.mainFrame = mainFrame;
        this.parentModule = parentModule;
        this.projectId = projectId;
        this.projectMemberDao = new ProjectMemberDao();
        this.userDao = new UserDao();
        this.projectDao = new ProjectDao();

        // ============ BİLDİRİM SERVİSİNİ BAŞLAT ============
        this.notificationService = new NotificationService();

        // Load project
        this.project = projectDao.findById(projectId);
        if (this.project == null) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Project not found!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        initializeDialog();
        loadMembers();
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

        // Current members section
        JPanel membersSection = createMembersSection();
        centerPanel.add(membersSection, BorderLayout.CENTER);

        // Add member section
        JPanel addMemberSection = createAddMemberSection();
        centerPanel.add(addMemberSection, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(StyleUtil.PRIMARY_LIGHT);
        panel.setBorder(StyleUtil.createPaddingBorder(20));

        // Project info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(StyleUtil.PRIMARY_LIGHT);

        lblProjectName = ComponentFactory.createHeadingLabel( project.getProjectName());
        lblProjectName.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblMemberCount = ComponentFactory.createBodyLabel("Total Members: 0");
        lblMemberCount.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMemberCount.setForeground(StyleUtil.TEXT_SECONDARY);

        infoPanel.add(lblProjectName);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblMemberCount);

        panel.add(infoPanel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createMembersSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(StyleUtil.SURFACE);

        // Title
        JLabel titleLabel = ComponentFactory.createHeadingLabel("Current Members");
        panel.add(titleLabel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"Member ID", "User", "Email", "Role in Project"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        membersTable = new JTable(tableModel);
        membersTable.setFont(StyleUtil.FONT_BODY);
        membersTable.setRowHeight(35);
        membersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        membersTable.getTableHeader().setFont(StyleUtil.FONT_BUTTON);
        membersTable.getTableHeader().setBackground(StyleUtil.PRIMARY_LIGHT);
        membersTable.setBackground(Color.WHITE);
        membersTable.setForeground(StyleUtil.TEXT_PRIMARY);
        membersTable.setGridColor(StyleUtil.BORDER);
        membersTable.setSelectionBackground(StyleUtil.PRIMARY_LIGHT);
        membersTable.setSelectionForeground(StyleUtil.TEXT_PRIMARY);

        // Column widths
        membersTable.getColumnModel().getColumn(0).setPreferredWidth(80);   // Member ID
        membersTable.getColumnModel().getColumn(1).setPreferredWidth(150);  // User
        membersTable.getColumnModel().getColumn(2).setPreferredWidth(200);  // Email
        membersTable.getColumnModel().getColumn(3).setPreferredWidth(150);  // Role

        JScrollPane scrollPane = new JScrollPane(membersTable);
        scrollPane.setBorder(StyleUtil.createLineBorder());
        scrollPane.setPreferredSize(new Dimension(750, 250));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Action buttons for selected member
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionPanel.setBackground(StyleUtil.SURFACE);

        JButton btnUpdateRole = ComponentFactory.createSecondaryButton(" Update Role");
        btnUpdateRole.addActionListener(e -> updateMemberRole());

        JButton btnRemove = ComponentFactory.createDangerButton(" Remove Member");
        btnRemove.addActionListener(e -> removeMember());

        actionPanel.add(btnUpdateRole);
        actionPanel.add(btnRemove);

        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAddMemberSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(StyleUtil.BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                StyleUtil.createLineBorder(),
                StyleUtil.createPaddingBorder(15)
        ));

        // Title
        JLabel titleLabel = ComponentFactory.createHeadingLabel(" Add New Member");
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
        cmbUsers.setPreferredSize(new Dimension(250, 35));
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

        // Role input
        JLabel lblRole = ComponentFactory.createBodyLabel("Role:");
        txtRole = new JTextField(15);
        txtRole.setFont(StyleUtil.FONT_BODY);
        txtRole.setPreferredSize(new Dimension(200, 35));
        txtRole.setBackground(Color.WHITE);
        txtRole.setForeground(StyleUtil.TEXT_PRIMARY);
        txtRole.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        // Add button
        JButton btnAdd = ComponentFactory.createPrimaryButton(" Add Member");
        btnAdd.addActionListener(e -> addMember());

        formPanel.add(lblUser);
        formPanel.add(cmbUsers);
        formPanel.add(Box.createHorizontalStrut(10));
        formPanel.add(lblRole);
        formPanel.add(txtRole);
        formPanel.add(Box.createHorizontalStrut(10));
        formPanel.add(btnAdd);

        panel.add(formPanel);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panel.setBackground(StyleUtil.SURFACE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, StyleUtil.BORDER));

        JButton btnClose = ComponentFactory.createSecondaryButton(" Done");
        btnClose.addActionListener(e -> {
            try {
                parentModule.onProjectUpdated();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            dispose();
        });

        panel.add(btnClose);

        return panel;
    }

    private void loadMembers() {
        tableModel.setRowCount(0);

        List<ProjectMember> members = projectMemberDao.findByProject(projectId);

        for (ProjectMember member : members) {
            try {
                User user = userDao.findById(member.getUserId());
                if (user != null) {
                    Object[] row = {
                            member.getProjectMemberId(),
                            user.getFullName(),
                            user.getEmail(),
                            member.getRoleInProject() != null ? member.getRoleInProject() : "-"
                    };
                    tableModel.addRow(row);
                }
            } catch (SQLException e) {
                System.err.println("Error loading user: " + e.getMessage());
            }
        }

        // Update count
        lblMemberCount.setText("Total Members: " + members.size());
    }

    private void loadAvailableUsers() {
        cmbUsers.removeAllItems();

        try {
            List<User> allUsers = userDao.findAll();
            List<Integer> existingMemberIds = projectMemberDao.getUserIdsForProject(projectId);

            for (User user : allUsers) {
                // Only add users who are not already members
                if (!existingMemberIds.contains(user.getUserId())) {
                    cmbUsers.addItem(user);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading users: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addMember() {
        // Validation
        User selectedUser = (User) cmbUsers.getSelectedItem();
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to add!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String role = txtRole.getText().trim();
        if (role.isEmpty()) {
            role = "Member"; // Default role
        }

        // Check if already a member (extra safety check)
        if (projectMemberDao.isMemberOfProject(selectedUser.getUserId(), projectId)) {
            JOptionPane.showMessageDialog(this,
                    "This user is already a member of the project!",
                    "Duplicate Member",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Add member
        try {
            ProjectMember newMember = new ProjectMember(projectId, selectedUser.getUserId(), role);
            projectMemberDao.insert(newMember);

            // ============ BİLDİRİM GÖNDER - BAŞLANGIÇ ============
            notificationService.notifyProjectMemberAdded(
                    projectId,
                    selectedUser.getUserId(),
                    project.getProjectName(),
                    mainFrame.getCurrentUsername()
            );

            // ========== ADD THIS LINE ==========
            if (mainFrame.getNotificationBell() != null) {
                mainFrame.getNotificationBell().refreshUnreadCount();
            }
            // ===================================
            // ============ BİLDİRİM GÖNDER - BİTİŞ ============


            JOptionPane.showMessageDialog(this,
                    "Member added successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Refresh
            loadMembers();
            loadAvailableUsers();
            txtRole.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error adding member: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateMemberRole() {
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a member to update!",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get current member info
        int projectMemberId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentRole = (String) tableModel.getValueAt(selectedRow, 3);
        String userName = (String) tableModel.getValueAt(selectedRow, 1);

        // Show input dialog for new role
        String newRole = (String) JOptionPane.showInputDialog(
                this,
                "Enter new role for " + userName + ":",
                "Update Member Role",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                currentRole
        );

        if (newRole != null && !newRole.trim().isEmpty()) {
            try {
                // Get the ProjectMember object
                ProjectMember member = projectMemberDao.findByProjectAndUser(
                        projectId,
                        getUserIdFromMemberId(projectMemberId)
                );

                if (member != null) {
                    projectMemberDao.updateRole(projectId, member.getUserId(), newRole.trim());

                    JOptionPane.showMessageDialog(this,
                            "Member role updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    loadMembers();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error updating member role: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void removeMember() {
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a member to remove!",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userName = (String) tableModel.getValueAt(selectedRow, 1);
        int projectMemberId = (int) tableModel.getValueAt(selectedRow, 0);

        // Confirm removal
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to remove " + userName + " from this project?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int userId = getUserIdFromMemberId(projectMemberId);
                projectMemberDao.delete(projectId, userId);

                // ============ BİLDİRİM GÖNDER - BAŞLANGIÇ ============
                notificationService.notifyProjectMemberRemoved(
                        projectId,
                        userId,
                        project.getProjectName(),
                        mainFrame.getCurrentUsername()
                );
                // ============ BİLDİRİM GÖNDER - BİTİŞ ============

                JOptionPane.showMessageDialog(this,
                        "Member removed successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                loadMembers();
                loadAvailableUsers();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error removing member: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    /**
     * Helper method to get user ID from project member ID
     */
    private int getUserIdFromMemberId(int projectMemberId) {
        List<ProjectMember> members = projectMemberDao.findByProject(projectId);
        for (ProjectMember member : members) {
            if (member.getProjectMemberId() == projectMemberId) {
                return member.getUserId();
            }
        }
        return -1;
    }
}