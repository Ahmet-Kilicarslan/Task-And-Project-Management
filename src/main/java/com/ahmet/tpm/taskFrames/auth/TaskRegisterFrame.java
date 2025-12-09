package com.ahmet.tpm.taskFrames.auth;

import com.ahmet.tpm.dao.DepartmentDao;
import com.ahmet.tpm.dao.UserDao;
import com.ahmet.tpm.models.Department;
import com.ahmet.tpm.models.User;
import com.ahmet.tpm.utils.StyleUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Standalone Register Frame for Task Management System
 */
public class TaskRegisterFrame extends JFrame {

    private UserDao userDao;
    private DepartmentDao departmentDao;

    // Form components
    private JTextField txtUsername;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JComboBox<Department> cmbDepartment;
    private JButton btnRegister;
    private JLabel lblLoginLink;

    public TaskRegisterFrame() {
        this.userDao = new UserDao();
        this.departmentDao = new DepartmentDao();

        initializeFrame();
        createUI();
        loadDepartments();
    }

    private void initializeFrame() {
        setTitle("Task Management System - Register");
        setSize(450, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        getContentPane().setBackground(Color.WHITE);
    }

    private void createUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        // Icon

        mainPanel.add(Box.createVerticalStrut(5));

        // Title
        JLabel lblTitle = new JLabel("CREATE ACCOUNT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(StyleUtil.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(10));

        JLabel lblSubtitle = new JLabel("Join Task Management System");
        lblSubtitle.setFont(StyleUtil.FONT_BODY);
        lblSubtitle.setForeground(StyleUtil.TEXT_SECONDARY);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblSubtitle);
        mainPanel.add(Box.createVerticalStrut(30));

        // Username
        mainPanel.add(createFieldLabel("USERNAME"));
        txtUsername = createTextField();
        mainPanel.add(txtUsername);
        mainPanel.add(Box.createVerticalStrut(15));

        // Full Name
        mainPanel.add(createFieldLabel("FULL NAME"));
        txtFullName = createTextField();
        mainPanel.add(txtFullName);
        mainPanel.add(Box.createVerticalStrut(15));

        // Email
        mainPanel.add(createFieldLabel("EMAIL"));
        txtEmail = createTextField();
        mainPanel.add(txtEmail);
        mainPanel.add(Box.createVerticalStrut(15));

        // Password
        mainPanel.add(createFieldLabel("PASSWORD"));
        txtPassword = createPasswordField();
        mainPanel.add(txtPassword);
        mainPanel.add(Box.createVerticalStrut(15));

        // Department
        mainPanel.add(createFieldLabel("DEPARTMENT"));
        cmbDepartment = new JComboBox<>();
        cmbDepartment.setFont(StyleUtil.FONT_BODY);
        cmbDepartment.setMaximumSize(new Dimension(350, 45));
        cmbDepartment.setBackground(Color.WHITE);
        cmbDepartment.setForeground(Color.BLACK);
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
        cmbDepartment.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(cmbDepartment);
        mainPanel.add(Box.createVerticalStrut(30));

        // Register Button (Green)
        btnRegister = new JButton("CREATE ACCOUNT");
        btnRegister.setFont(StyleUtil.FONT_BUTTON);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setBackground(StyleUtil.SUCCESS);
        btnRegister.setFocusPainted(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setMaximumSize(new Dimension(350, 45));
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.addActionListener(e -> handleRegister());

        btnRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnRegister.setBackground(new Color(32, 134, 55));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnRegister.setBackground(StyleUtil.SUCCESS);
            }
        });

        mainPanel.add(btnRegister);
        mainPanel.add(Box.createVerticalStrut(30));

        // Login Link Panel
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        linkPanel.setBackground(Color.WHITE);
        linkPanel.setMaximumSize(new Dimension(350, 30));

        JLabel lblAlreadyHave = new JLabel("Already have an account?");
        lblAlreadyHave.setFont(StyleUtil.FONT_BODY);
        lblAlreadyHave.setForeground(StyleUtil.TEXT_SECONDARY);

        lblLoginLink = new JLabel("Sign in");
        lblLoginLink.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLoginLink.setForeground(StyleUtil.SUCCESS);
        lblLoginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblLoginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openLoginFrame();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lblLoginLink.setForeground(new Color(32, 134, 55));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lblLoginLink.setForeground(StyleUtil.SUCCESS);
            }
        });

        linkPanel.add(lblAlreadyHave);
        linkPanel.add(lblLoginLink);
        mainPanel.add(linkPanel);

        add(mainPanel);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(StyleUtil.FONT_BODY);
        label.setForeground(StyleUtil.TEXT_PRIMARY);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(StyleUtil.FONT_BODY);
        field.setMaximumSize(new Dimension(350, 45));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(StyleUtil.FONT_BODY);
        field.setMaximumSize(new Dimension(350, 45));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        return field;
    }

    private void loadDepartments() {
        List<Department> departments = departmentDao.findAll();
        for (Department dept : departments) {
            cmbDepartment.addItem(dept);
        }
    }

    private void handleRegister() {
        String username = txtUsername.getText().trim();
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        Department selectedDept = (Department) cmbDepartment.getSelectedItem();

        // Validation
        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all fields!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedDept == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a department!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (username.length() < 3) {
            JOptionPane.showMessageDialog(this,
                    "Username must be at least 3 characters!",
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

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 6 characters!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Check if username exists
            User existingUser = userDao.findByUsername(username);
            if (existingUser != null) {
                JOptionPane.showMessageDialog(this,
                        "Username already taken!",
                        "Registration Failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if email exists
            existingUser = userDao.findByEmail(email);
            if (existingUser != null) {
                JOptionPane.showMessageDialog(this,
                        "Email already registered!",
                        "Registration Failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create new user
            User newUser = new User(
                    username,
                    password,
                    email,
                    fullName,
                    selectedDept.getDepartmentId()
            );

            int userId = userDao.insert(newUser);

            JOptionPane.showMessageDialog(this,
                    "Account created successfully!\nPlease login to continue.",
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            openLoginFrame();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Registration error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openLoginFrame() {
        TaskLoginFrame loginFrame = new TaskLoginFrame();
        loginFrame.setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            TaskRegisterFrame frame = new TaskRegisterFrame();
            frame.setVisible(true);
        });
    }
}