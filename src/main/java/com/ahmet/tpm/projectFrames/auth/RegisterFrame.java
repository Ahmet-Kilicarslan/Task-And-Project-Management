package com.ahmet.tpm.projectFrames.auth;

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

public class RegisterFrame extends JFrame {

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

    public RegisterFrame() {
        this.userDao = new UserDao();
        this.departmentDao = new DepartmentDao();

        initializeFrame();
        createUI();
        loadDepartments();
    }

    private void initializeFrame() {
        setTitle("ProjectFlow - Register");
        setSize(450, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // BEYAZ ARKA PLAN
        getContentPane().setBackground(Color.WHITE);
    }

    private void createUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE); // BEYAZ
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        // Title
        JLabel lblTitle = new JLabel("CREATE ACCOUNT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(StyleUtil.TEXT_PRIMARY); // SIYAH
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(10));

        JLabel lblSubtitle = new JLabel("Join TaskFlow today");
        lblSubtitle.setFont(StyleUtil.FONT_BODY);
        lblSubtitle.setForeground(StyleUtil.TEXT_SECONDARY);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblSubtitle);
        mainPanel.add(Box.createVerticalStrut(30));

        // Username Label
        JLabel lblUsername = new JLabel("USERNAME");
        lblUsername.setFont(StyleUtil.FONT_BODY);
        lblUsername.setForeground(StyleUtil.TEXT_PRIMARY);
        lblUsername.setAlignmentX(Component.CENTER_ALIGNMENT);  // ORTALANDI
        mainPanel.add(lblUsername);
        mainPanel.add(Box.createVerticalStrut(8));

        // Username Field
        txtUsername = new JTextField();
        txtUsername.setFont(StyleUtil.FONT_BODY);
        txtUsername.setMaximumSize(new Dimension(350, 45));
        txtUsername.setBackground(Color.WHITE);        // BEYAZ KUTU
        txtUsername.setForeground(Color.BLACK);  // SİYAH YAZI (görünür olsun)
        txtUsername.setCaretColor(Color.BLACK);  // İmleç rengi
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        txtUsername.setAlignmentX(Component.CENTER_ALIGNMENT);  // ORTALANDI
        mainPanel.add(txtUsername);
        mainPanel.add(Box.createVerticalStrut(15));

        // Full Name Label
        JLabel lblFullName = new JLabel("FULL NAME");
        lblFullName.setFont(StyleUtil.FONT_BODY);
        lblFullName.setForeground(StyleUtil.TEXT_PRIMARY);
        lblFullName.setAlignmentX(Component.CENTER_ALIGNMENT);  // ORTALANDI
        mainPanel.add(lblFullName);
        mainPanel.add(Box.createVerticalStrut(8));

        // Full Name Field
        txtFullName = new JTextField();
        txtFullName.setFont(StyleUtil.FONT_BODY);
        txtFullName.setMaximumSize(new Dimension(350, 45));
        txtFullName.setBackground(Color.WHITE);        // BEYAZ KUTU
        txtFullName.setForeground(Color.BLACK);  // SİYAH YAZI (görünür olsun)
        txtFullName.setCaretColor(Color.BLACK);  // İmleç rengi
        txtFullName.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        txtFullName.setAlignmentX(Component.CENTER_ALIGNMENT);  // ORTALANDI
        mainPanel.add(txtFullName);
        mainPanel.add(Box.createVerticalStrut(15));

        // Email Label
        JLabel lblEmail = new JLabel("EMAIL");
        lblEmail.setFont(StyleUtil.FONT_BODY);
        lblEmail.setForeground(StyleUtil.TEXT_PRIMARY);
        lblEmail.setAlignmentX(Component.CENTER_ALIGNMENT);  // ORTALANDI
        mainPanel.add(lblEmail);
        mainPanel.add(Box.createVerticalStrut(8));

        // Email Field
        txtEmail = new JTextField();
        txtEmail.setFont(StyleUtil.FONT_BODY);
        txtEmail.setMaximumSize(new Dimension(350, 45));
        txtEmail.setBackground(Color.WHITE);        // BEYAZ KUTU
        txtEmail.setForeground(Color.BLACK);  // SİYAH YAZI (görünür olsun)
        txtEmail.setCaretColor(Color.BLACK);  // İmleç rengi
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        txtEmail.setAlignmentX(Component.CENTER_ALIGNMENT);  // ORTALANDI
        mainPanel.add(txtEmail);
        mainPanel.add(Box.createVerticalStrut(15));

        // Password Label
        JLabel lblPassword = new JLabel("PASSWORD");
        lblPassword.setFont(StyleUtil.FONT_BODY);
        lblPassword.setForeground(StyleUtil.TEXT_PRIMARY);
        lblPassword.setAlignmentX(Component.CENTER_ALIGNMENT);  // ORTALANDI
        mainPanel.add(lblPassword);
        mainPanel.add(Box.createVerticalStrut(8));

        // Password Field
        txtPassword = new JPasswordField();
        txtPassword.setFont(StyleUtil.FONT_BODY);
        txtPassword.setMaximumSize(new Dimension(350, 45));
        txtPassword.setBackground(Color.WHITE);        // BEYAZ KUTU
        txtPassword.setForeground(Color.BLACK);  // SİYAH YAZI (görünür olsun)
        txtPassword.setCaretColor(Color.BLACK);  // İmleç rengi
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        txtPassword.setAlignmentX(Component.CENTER_ALIGNMENT);  // ORTALANDI
        mainPanel.add(txtPassword);
        mainPanel.add(Box.createVerticalStrut(15));

        // Department Label
        JLabel lblDepartment = new JLabel("DEPARTMENT");
        lblDepartment.setFont(StyleUtil.FONT_BODY);
        lblDepartment.setForeground(StyleUtil.TEXT_PRIMARY);
        lblDepartment.setAlignmentX(Component.CENTER_ALIGNMENT);  // ORTALANDI
        mainPanel.add(lblDepartment);
        mainPanel.add(Box.createVerticalStrut(8));

        // Department ComboBox
        cmbDepartment = new JComboBox<>();
        cmbDepartment.setFont(StyleUtil.FONT_BODY);
        cmbDepartment.setMaximumSize(new Dimension(350, 45));
        cmbDepartment.setBackground(Color.WHITE);        // BEYAZ KUTU
        cmbDepartment.setForeground(Color.BLACK);  // SİYAH YAZI (görünür olsun)
        cmbDepartment.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Department) {
                    setText(((Department) value).getDepartmentName());
                }
                // RENK AYARLARI
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
        cmbDepartment.setAlignmentX(Component.CENTER_ALIGNMENT);  // ORTALANDI
        mainPanel.add(cmbDepartment);
        mainPanel.add(Box.createVerticalStrut(30));

        // Register Button
        btnRegister = new JButton("CREATE ACCOUNT");
        btnRegister.setFont(StyleUtil.FONT_BUTTON);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setBackground(StyleUtil.PRIMARY); // MAVI
        btnRegister.setFocusPainted(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setMaximumSize(new Dimension(350, 45));
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);  // ORTALANDI
        btnRegister.addActionListener(e -> handleRegister());

        // Hover effect
        btnRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnRegister.setBackground(StyleUtil.PRIMARY_DARK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnRegister.setBackground(StyleUtil.PRIMARY);
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
        lblLoginLink.setForeground(StyleUtil.PRIMARY);
        lblLoginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblLoginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openLoginFrame();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lblLoginLink.setForeground(StyleUtil.PRIMARY_DARK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lblLoginLink.setForeground(StyleUtil.PRIMARY);
            }
        });

        linkPanel.add(lblAlreadyHave);
        linkPanel.add(lblLoginLink);
        mainPanel.add(linkPanel);

        add(mainPanel);
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
            // Check if username already exists
            User existingUser = userDao.findByUsername(username);
            if (existingUser != null) {
                JOptionPane.showMessageDialog(this,
                        "Username already taken!",
                        "Registration Failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if email already exists
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

            // Başarılı kayıt
            JOptionPane.showMessageDialog(this,
                    "Account created successfully!\nPlease login to continue.",
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            // Login frame'e dön
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
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            RegisterFrame frame = new RegisterFrame();
            frame.setVisible(true);
        });
    }
}