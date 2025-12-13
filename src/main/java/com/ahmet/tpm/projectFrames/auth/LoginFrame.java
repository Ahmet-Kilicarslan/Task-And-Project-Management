package com.ahmet.tpm.projectFrames.auth;

import com.ahmet.tpm.dao.UserDao;
import com.ahmet.tpm.projectFrames.MainFrame;
import com.ahmet.tpm.models.User;
import com.ahmet.tpm.utils.StyleUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginFrame extends JFrame {

    private UserDao userDao;

    // Form components
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblRegisterLink;

    public LoginFrame() {
        this.userDao = new UserDao();

        initializeFrame();
        createUI();
    }

    private void initializeFrame() {
        setTitle("ProjectFlow - Login");
        setSize(450, 550);
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
        mainPanel.setBorder(BorderFactory.createEmptyBorder(60, 50, 60, 50));

        // Title
        JLabel lblTitle = new JLabel("LOGIN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTitle.setForeground(StyleUtil.TEXT_PRIMARY); // SIYAH
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(50));

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
        txtEmail.setBackground(Color.WHITE);
        txtEmail.setOpaque(true);
        txtEmail.setForeground(Color.BLACK);  // SİYAH YAZI
        txtEmail.setCaretColor(Color.BLACK);  // İMLEÇ SİYAH
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        txtEmail.setAlignmentX(Component.CENTER_ALIGNMENT);  // ORTALANDI
        mainPanel.add(txtEmail);
        mainPanel.add(Box.createVerticalStrut(20));

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
        txtPassword.setBackground(Color.WHITE);
        txtPassword.setOpaque(true);// MAVİ ARKA PLAN (LIGHT BLUE)
        txtPassword.setForeground(Color.BLACK);  // SİYAH YAZI
        txtPassword.setCaretColor(Color.BLACK);  // İMLEÇ SİYAH
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        txtPassword.setAlignmentX(Component.CENTER_ALIGNMENT);  // ORTALANDI
        mainPanel.add(txtPassword);
        mainPanel.add(Box.createVerticalStrut(35));

        // Login Button
        btnLogin = new JButton("SIGN IN");
        btnLogin.setFont(StyleUtil.FONT_BUTTON);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(StyleUtil.PRIMARY);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setMaximumSize(new Dimension(350, 45));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);  // ORTALANDI
        btnLogin.addActionListener(e -> handleLogin());

        // Hover effect
        btnLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogin.setBackground(StyleUtil.DANGER_DARK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnLogin.setBackground(StyleUtil.DANGER);
            }
        });

        mainPanel.add(btnLogin);
        mainPanel.add(Box.createVerticalStrut(40));

        // Register Link Panel
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        linkPanel.setBackground(Color.WHITE);
        linkPanel.setMaximumSize(new Dimension(350, 30));

        JLabel lblDontHave = new JLabel("Don't have an account?");
        lblDontHave.setFont(StyleUtil.FONT_BODY);
        lblDontHave.setForeground(StyleUtil.TEXT_SECONDARY);

        lblRegisterLink = new JLabel("Sign up");
        lblRegisterLink.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRegisterLink.setForeground(StyleUtil.PRIMARY);
        lblRegisterLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegisterLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openRegisterFrame();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lblRegisterLink.setForeground(StyleUtil.PRIMARY_DARK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lblRegisterLink.setForeground(StyleUtil.PRIMARY);
            }
        });

        linkPanel.add(lblDontHave);
        linkPanel.add(lblRegisterLink);
        mainPanel.add(linkPanel);

        add(mainPanel);
    }

    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all fields!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Email ile kullanıcı bul
            User user = userDao.findByEmail(email);

            if (user == null) {
                JOptionPane.showMessageDialog(this,
                        "Invalid email or password!",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Password kontrolü
            if (!user.getPassword().equals(password)) {
                JOptionPane.showMessageDialog(this,
                        "Invalid email or password!",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Başarılı giriş
            JOptionPane.showMessageDialog(this,
                    "Welcome back, " + user.getFullName() + "!",
                    "Login Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            // MainFrame'i aç
            MainFrame mainFrame = new MainFrame(user.getUsername(), user.getUserId());
            mainFrame.setVisible(true);

            // Login frame'i kapat
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Login error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openRegisterFrame() {
        RegisterFrame registerFrame = new RegisterFrame();
        registerFrame.setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}