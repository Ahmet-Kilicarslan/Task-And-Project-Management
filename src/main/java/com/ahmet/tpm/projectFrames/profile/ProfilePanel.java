package com.ahmet.tpm.projectFrames.profile;

import com.ahmet.tpm.dao.DepartmentDao;
import com.ahmet.tpm.dao.UserDao;
import com.ahmet.tpm.models.Department;
import com.ahmet.tpm.models.User;
import com.ahmet.tpm.projectFrames.MainFrame;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ProfilePanel extends JPanel {

    private MainFrame mainFrame;
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

    public ProfilePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userDao = new UserDao();
        this.departmentDao = new DepartmentDao();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);  // BEYAZ ARKA PLAN

        initializeUI();
        loadUserData();
    }

    private void initializeUI() {
        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);  // BEYAZ
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Title
        JLabel lblTitle = ComponentFactory.createTitleLabel("MY PROFILE");
        lblTitle.setForeground(Color.BLACK);  // SİYAH
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblTitle);
        contentPanel.add(Box.createVerticalStrut(30));

        // Info Card
        JPanel infoCard = createInfoCard();
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(infoCard);
        contentPanel.add(Box.createVerticalStrut(20));

        // Password Change Card
        JPanel passwordCard = createPasswordCard();
        passwordCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(passwordCard);
        contentPanel.add(Box.createVerticalStrut(20));

        // Action Buttons
        JPanel buttonPanel = createButtonPanel();
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(buttonPanel);

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Color.WHITE);  // BEYAZ
        scrollPane.getViewport().setBackground(Color.WHITE);  // BEYAZ

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createInfoCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);  // BEYAZ
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER, 2),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        card.setMaximumSize(new Dimension(800, 500));

        // Card Title
        JLabel cardTitle = new JLabel("ACCOUNT INFORMATION");
        cardTitle.setFont(StyleUtil.FONT_HEADING);
        cardTitle.setForeground(Color.BLACK);  // SİYAH
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(cardTitle);
        card.add(Box.createVerticalStrut(20));

        // User ID (read-only)
        card.add(createFieldLabel("USER ID"));
        lblUserId = new JLabel();
        lblUserId.setFont(StyleUtil.FONT_BODY);
        lblUserId.setForeground(Color.BLACK);  // SİYAH
        lblUserId.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblUserId);
        card.add(Box.createVerticalStrut(15));

        // Username
        card.add(createFieldLabel("USERNAME"));
        txtUsername = createTextField();
        txtUsername.setEditable(false);  // Username değiştirilemez
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
        cmbDepartment.setBackground(Color.WHITE);  // BEYAZ
        cmbDepartment.setForeground(Color.BLACK);  // SİYAH
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
                    setBackground(StyleUtil.PRIMARY_LIGHT);
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

        // Created At (read-only)
        card.add(createFieldLabel("MEMBER SINCE"));
        lblCreatedAt = new JLabel();
        lblCreatedAt.setFont(StyleUtil.FONT_BODY);
        lblCreatedAt.setForeground(Color.GRAY);  // GRİ
        lblCreatedAt.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblCreatedAt);

        return card;
    }

    private JPanel createPasswordCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);  // BEYAZ
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER, 2),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        card.setMaximumSize(new Dimension(800, 350));

        // Card Title
        JLabel cardTitle = new JLabel("CHANGE PASSWORD");
        cardTitle.setFont(StyleUtil.FONT_HEADING);
        cardTitle.setForeground(Color.BLACK);  // SİYAH
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
        panel.setBackground(Color.WHITE);  // BEYAZ
        panel.setMaximumSize(new Dimension(800, 50));

        btnEdit = ComponentFactory.createPrimaryButton("Edit Profile");
        btnEdit.addActionListener(e -> enableEditMode());

        btnSave = ComponentFactory.createPrimaryButton("Save Changes");
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
        label.setForeground(Color.BLACK);  // SİYAH
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(StyleUtil.FONT_BODY);
        field.setMaximumSize(new Dimension(750, 45));
        field.setBackground(Color.WHITE);  // BEYAZ
        field.setForeground(Color.BLACK);  // SİYAH
        field.setCaretColor(Color.BLACK);  // İMLEÇ SİYAH
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setEditable(false);  // Başlangıçta düzenlenemez
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(StyleUtil.FONT_BODY);
        field.setMaximumSize(new Dimension(750, 45));
        field.setBackground(Color.WHITE);  // BEYAZ
        field.setForeground(Color.BLACK);  // SİYAH
        field.setCaretColor(Color.BLACK);  // İMLEÇ SİYAH
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setEditable(false);  // Başlangıçta düzenlenemez
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

            // Display user info
            lblUserId.setText("#" + currentUser.getUserId());
            txtUsername.setText(currentUser.getUsername());
            txtFullName.setText(currentUser.getFullName());
            txtEmail.setText(currentUser.getEmail());

            if (currentUser.getCreatedAt() != null) {
                lblCreatedAt.setText(currentUser.getCreatedAt().toLocalDate().toString());
            }

            // Load departments
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

            // Select current department
            if (currentUser.getDepartmentId() != null &&
                    dept.getDepartmentId() == currentUser.getDepartmentId()) {
                cmbDepartment.setSelectedItem(dept);
            }
        }
    }

    private void enableEditMode() {
        isEditMode = true;

        // Enable fields
        txtFullName.setEditable(true);
        txtEmail.setEditable(true);
        cmbDepartment.setEnabled(true);

        // Enable password fields
        txtOldPassword.setEditable(true);
        txtNewPassword.setEditable(true);
        txtConfirmPassword.setEditable(true);

        // Change field backgrounds to indicate editable
        txtFullName.setBackground(new Color(255, 255, 220));  // Açık sarı
        txtEmail.setBackground(new Color(255, 255, 220));

        // Show/hide buttons
        btnEdit.setVisible(false);
        btnSave.setVisible(true);
        btnCancel.setVisible(true);
    }

    private void cancelEdit() {
        isEditMode = false;

        // Reload original data
        loadUserData();

        // Disable fields
        txtFullName.setEditable(false);
        txtEmail.setEditable(false);
        cmbDepartment.setEnabled(false);

        txtOldPassword.setEditable(false);
        txtNewPassword.setEditable(false);
        txtConfirmPassword.setEditable(false);

        // Reset field backgrounds
        txtFullName.setBackground(Color.WHITE);
        txtEmail.setBackground(Color.WHITE);

        // Clear password fields
        txtOldPassword.setText("");
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");

        // Show/hide buttons
        btnEdit.setVisible(true);
        btnSave.setVisible(false);
        btnCancel.setVisible(false);
    }

    private void saveChanges() {
        if (!isEditMode) return;

        try {
            // Validate
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

            // Check if email is already taken by another user
            User existingUser = userDao.findByEmail(email);
            if (existingUser != null && existingUser.getUserId() != currentUser.getUserId()) {
                JOptionPane.showMessageDialog(this,
                        "Email already in use by another user!",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update user info
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
                // Validate password change
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

            // Save to database
            boolean success = userDao.update(currentUser);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Profile updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                cancelEdit();  // Exit edit mode
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