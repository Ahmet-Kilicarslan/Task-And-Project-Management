package com.ahmet.tpm.taskFrames.profile;

import com.ahmet.tpm.dao.DepartmentDao;
import com.ahmet.tpm.dao.UserDao;
import com.ahmet.tpm.models.Department;
import com.ahmet.tpm.models.User;
import com.ahmet.tpm.taskFrames.TaskMainFrame;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Profile panel for Task Management System
 * Same functionality as Project system's ProfilePanel
 */
public class TaskProfilePanel extends JPanel {

    private TaskMainFrame mainFrame;
    private UserDao userDao;
    private DepartmentDao departmentDao;
    private User currentUser;

    // UI Components
    private JTextField txtUsername;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JPasswordField txtOldPassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private JComboBox<Department> cmbDepartment;
    private JLabel lblCreatedAt;
    private JLabel lblUserId;

    // Mode
    private boolean isEditMode = false;
    private JButton btnEdit;
    private JButton btnSave;
    private JButton btnCancel;

    public TaskProfilePanel(TaskMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userDao = new UserDao();
        this.departmentDao = new DepartmentDao();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initializeUI();
        loadUserData();
    }

    private void initializeUI() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Title with green accent
        JLabel lblTitle = new JLabel("MY PROFILE");
        lblTitle.setFont(StyleUtil.FONT_LARGE);
        lblTitle.setForeground(StyleUtil.SUCCESS);  // Green for Task system
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblTitle);
        contentPanel.add(Box.createVerticalStrut(30));

        // Info Card
        JPanel infoCard = createInfoCard();
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(infoCard);
        contentPanel.add(Box.createVerticalStrut(20));

        // Password Card
        JPanel passwordCard = createPasswordCard();
        passwordCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(passwordCard);
        contentPanel.add(Box.createVerticalStrut(20));

        // Action Buttons
        JPanel buttonPanel = createButtonPanel();
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(buttonPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createInfoCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.SUCCESS, 2),  // Green border
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        card.setMaximumSize(new Dimension(800, 500));

        JLabel cardTitle = new JLabel("ACCOUNT INFORMATION");
        cardTitle.setFont(StyleUtil.FONT_HEADING);
        cardTitle.setForeground(StyleUtil.SUCCESS);  // Green
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(cardTitle);
        card.add(Box.createVerticalStrut(20));

        // User ID
        card.add(createFieldLabel("USER ID"));
        lblUserId = new JLabel();
        lblUserId.setFont(StyleUtil.FONT_BODY);
        lblUserId.setForeground(Color.BLACK);
        lblUserId.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblUserId);
        card.add(Box.createVerticalStrut(15));

        // Username
        card.add(createFieldLabel("USERNAME"));
        txtUsername = createTextField();
        txtUsername.setEditable(false);
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(15));

        // Full Name
        card.add(createFieldLabel("FULL NAME"));
        txtFullName = createTextField();
        card.add(txtFullName);
        card.add(Box.createVerticalStrut(15));

        // Email
        card.add(createFieldLabel("EMAIL"));
        txtEmail = createTextField();
        card.add(txtEmail);
        card.add(Box.createVerticalStrut(15));

        // Department
        card.add(createFieldLabel("DEPARTMENT"));
        cmbDepartment = new JComboBox<>();
        cmbDepartment.setFont(StyleUtil.FONT_BODY);
        cmbDepartment.setMaximumSize(new Dimension(750, 45));
        cmbDepartment.setBackground(Color.WHITE);
        cmbDepartment.setForeground(Color.BLACK);
        cmbDepartment.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbDepartment.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Department) {
                    setText(((Department) value).getDepartmentName());
                }
                if (isSelected) {
                    setBackground(new Color(220, 248, 230));  // Light green
                    setForeground(Color.BLACK);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                }
                return this;
            }
        });
        card.add(cmbDepartment);
        card.add(Box.createVerticalStrut(15));

        // Created At
        card.add(createFieldLabel("MEMBER SINCE"));
        lblCreatedAt = new JLabel();
        lblCreatedAt.setFont(StyleUtil.FONT_BODY);
        lblCreatedAt.setForeground(Color.GRAY);
        lblCreatedAt.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblCreatedAt);

        return card;
    }

    private JPanel createPasswordCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.SUCCESS, 2),  // Green border
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        card.setMaximumSize(new Dimension(800, 350));

        JLabel cardTitle = new JLabel("CHANGE PASSWORD");
        cardTitle.setFont(StyleUtil.FONT_HEADING);
        cardTitle.setForeground(StyleUtil.SUCCESS);  // Green
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(cardTitle);
        card.add(Box.createVerticalStrut(20));

        // Old Password
        card.add(createFieldLabel("OLD PASSWORD"));
        txtOldPassword = createPasswordField();
        card.add(txtOldPassword);
        card.add(Box.createVerticalStrut(15));

        // New Password
        card.add(createFieldLabel("NEW PASSWORD"));
        txtNewPassword = createPasswordField();
        card.add(txtNewPassword);
        card.add(Box.createVerticalStrut(15));

        // Confirm Password
        card.add(createFieldLabel("CONFIRM NEW PASSWORD"));
        txtConfirmPassword = createPasswordField();
        card.add(txtConfirmPassword);

        return card;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(800, 50));

        btnEdit = new JButton("Edit Profile");
        btnEdit.setFont(StyleUtil.FONT_BUTTON);
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setBackground(StyleUtil.SUCCESS);  // Green
        btnEdit.setFocusPainted(false);
        btnEdit.setBorderPainted(false);
        btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEdit.addActionListener(e -> enableEditMode());

        btnSave = new JButton("Save Changes");
        btnSave.setFont(StyleUtil.FONT_BUTTON);
        btnSave.setForeground(Color.WHITE);
        btnSave.setBackground(StyleUtil.SUCCESS);  // Green
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveChanges());
        btnSave.setVisible(false);

        btnCancel = ComponentFactory.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> cancelEdit());
        btnCancel.setVisible(false);

        panel.add(btnEdit);
        panel.add(btnSave);
        panel.add(btnCancel);

        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Color.BLACK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(StyleUtil.FONT_BODY);
        field.setMaximumSize(new Dimension(750, 45));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setEditable(false);
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(StyleUtil.FONT_BODY);
        field.setMaximumSize(new Dimension(750, 45));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setEditable(false);
        return field;
    }

    private void loadUserData() {
        try {
            currentUser = userDao.findById(mainFrame.getCurrentUserId());

            if (currentUser == null) {
                JOptionPane.showMessageDialog(this,
                        "User not found!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            lblUserId.setText("#" + currentUser.getUserId());
            txtUsername.setText(currentUser.getUsername());
            txtFullName.setText(currentUser.getFullName());
            txtEmail.setText(currentUser.getEmail());

            if (currentUser.getCreatedAt() != null) {
                lblCreatedAt.setText(currentUser.getCreatedAt().toLocalDate().toString());
            }

            loadDepartments();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading user data: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadDepartments() {
        cmbDepartment.removeAllItems();
        List<Department> departments = departmentDao.findAll();

        for (Department dept : departments) {
            cmbDepartment.addItem(dept);

            if (currentUser.getDepartmentId() != null &&
                    dept.getDepartmentId() == currentUser.getDepartmentId()) {
                cmbDepartment.setSelectedItem(dept);
            }
        }
    }

    private void enableEditMode() {
        isEditMode = true;

        txtFullName.setEditable(true);
        txtEmail.setEditable(true);
        cmbDepartment.setEnabled(true);

        txtOldPassword.setEditable(true);
        txtNewPassword.setEditable(true);
        txtConfirmPassword.setEditable(true);

        txtFullName.setBackground(new Color(255, 255, 220));
        txtEmail.setBackground(new Color(255, 255, 220));

        btnEdit.setVisible(false);
        btnSave.setVisible(true);
        btnCancel.setVisible(true);
    }

    private void cancelEdit() {
        isEditMode = false;

        loadUserData();

        txtFullName.setEditable(false);
        txtEmail.setEditable(false);
        cmbDepartment.setEnabled(false);

        txtOldPassword.setEditable(false);
        txtNewPassword.setEditable(false);
        txtConfirmPassword.setEditable(false);

        txtFullName.setBackground(Color.WHITE);
        txtEmail.setBackground(Color.WHITE);

        txtOldPassword.setText("");
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");

        btnEdit.setVisible(true);
        btnSave.setVisible(false);
        btnCancel.setVisible(false);
    }

    private void saveChanges() {
        if (!isEditMode) return;

        try {
            String fullName = txtFullName.getText().trim();
            String email = txtEmail.getText().trim();

            if (fullName.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Full Name and Email are required!",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!email.contains("@")) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid email address!",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            User existingUser = userDao.findByEmail(email);
            if (existingUser != null && existingUser.getUserId() != currentUser.getUserId()) {
                JOptionPane.showMessageDialog(this,
                        "Email already in use by another user!",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            currentUser.setFullName(fullName);
            currentUser.setEmail(email);

            Department selectedDept = (Department) cmbDepartment.getSelectedItem();
            if (selectedDept != null) {
                currentUser.setDepartmentId(selectedDept.getDepartmentId());
            }

            // Handle password change
            String oldPassword = new String(txtOldPassword.getPassword());
            String newPassword = new String(txtNewPassword.getPassword());
            String confirmPassword = new String(txtConfirmPassword.getPassword());

            if (!oldPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty()) {
                if (!currentUser.getPassword().equals(oldPassword)) {
                    JOptionPane.showMessageDialog(this,
                            "Old password is incorrect!",
                            "Password Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (newPassword.length() < 6) {
                    JOptionPane.showMessageDialog(this,
                            "New password must be at least 6 characters!",
                            "Password Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(this,
                            "New passwords do not match!",
                            "Password Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                currentUser.setPassword(newPassword);
            }

            boolean success = userDao.update(currentUser);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Profile updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                cancelEdit();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to update profile!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving changes: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void refreshUserData() {
        loadUserData();
    }
}