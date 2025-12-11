package com.ahmet.tpm.taskFrames.tasks;

import com.ahmet.tpm.dao.TimeTrackingDao;
import com.ahmet.tpm.models.TimeTracking;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;
import com.ahmet.tpm.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class LogTimeDialog extends JDialog {

    private TimeTrackingDao timeTrackingDao;
    private int taskId;
    private int userId;
    private JFrame parentFrame;
    private Runnable onSuccess;

    // UI Components
    private JComboBox<String> cmbDate;
    private JTextField txtHours;

    public LogTimeDialog(JFrame parent, int taskId, int userId, Runnable onSuccess) {
        super(parent, "Log Time", true);
        this.parentFrame = parent;
        this.taskId = taskId;
        this.userId = userId;
        this.onSuccess = onSuccess;
        this.timeTrackingDao = new TimeTrackingDao();

        initializeUI();
        setSize(450, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(StyleUtil.BACKGROUND);

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(StyleUtil.SURFACE);
        panel.setBorder(StyleUtil.createPaddingBorder(20));

        JLabel titleLabel = ComponentFactory.createHeadingLabel("Log Time Entry");
        panel.add(titleLabel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(StyleUtil.createPaddingBorder(20));

        // Date Field
        panel.add(createFieldLabel("Date:"));
        panel.add(Box.createVerticalStrut(8));
        cmbDate = createDateComboBox();
        cmbDate.setMaximumSize(new Dimension(400, 35));
        panel.add(cmbDate);
        panel.add(Box.createVerticalStrut(25));

        // Hours Field
        panel.add(createFieldLabel("Hours Worked:"));
        panel.add(Box.createVerticalStrut(8));
        txtHours = ComponentFactory.createTextField();
        txtHours.setMaximumSize(new Dimension(400, 35));
        txtHours.setPreferredSize(new Dimension(400, 35));
        panel.add(txtHours);
        panel.add(Box.createVerticalStrut(25));

        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(StyleUtil.FONT_BUTTON);
        label.setForeground(StyleUtil.TEXT_PRIMARY);
        return label;
    }

    private JComboBox<String> createDateComboBox() {
        // Create combo box with last 7 days
        String[] dateOptions = new String[8];
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 8; i++) {
            LocalDate date = today.minusDays(i);
            String label;
            if (i == 0) {
                label = "Today (" + date.toString() + ")";
            } else if (i == 1) {
                label = "Yesterday (" + date.toString() + ")";
            } else {
                label = date.toString();
            }
            dateOptions[i] = label;
        }

        JComboBox<String> combo = new JComboBox<>(dateOptions);
        combo.setFont(StyleUtil.FONT_BODY);
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.BLACK);
        return combo;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(StyleUtil.createPaddingBorder(15, 20, 15, 20));

        JButton btnCancel = ComponentFactory.createSecondaryButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(130, 35));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = ComponentFactory.createPrimaryButton("Log Time");
        btnSave.setPreferredSize(new Dimension(130, 35));
        btnSave.addActionListener(e -> saveTimeEntry());

        panel.add(btnCancel);
        panel.add(btnSave);

        return panel;
    }

    private void saveTimeEntry() {
        // Validate input
        if (txtHours.getText().trim().isEmpty()) {
            UIHelper.showError(parentFrame, "Please enter hours worked");
            txtHours.requestFocus();
            return;
        }

        double hours;
        try {
            hours = Double.parseDouble(txtHours.getText().trim());
            if (hours <= 0 || hours > 24) {
                UIHelper.showError(parentFrame, "Hours must be between 0 and 24");
                txtHours.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            UIHelper.showError(parentFrame, "Please enter a valid number for hours");
            txtHours.requestFocus();
            return;
        }

        // Get selected date
        String selectedDateStr = (String) cmbDate.getSelectedItem();
        LocalDate workDate = extractDateFromComboBox(selectedDateStr);

        // Create time entry
        TimeTracking timeEntry = new TimeTracking(taskId, userId, workDate, hours);

        // Save to database
        try {
            Integer entryId = timeTrackingDao.insertAndGetId(timeEntry);
            if (entryId != null) {
                UIHelper.showSuccess(parentFrame, "Time logged successfully!");

                // Call success callback
                if (onSuccess != null) {
                    onSuccess.run();
                }

                dispose();
            } else {
                UIHelper.showError(parentFrame, "Failed to log time entry");
            }
        } catch (Exception e) {
            UIHelper.showError(parentFrame, "Error logging time: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private LocalDate extractDateFromComboBox(String dateStr) {
        // Extract date from strings like "Today (2024-12-11)" or "2024-12-11"
        if (dateStr.contains("(")) {
            int start = dateStr.indexOf("(") + 1;
            int end = dateStr.indexOf(")");
            dateStr = dateStr.substring(start, end);
        }
        return LocalDate.parse(dateStr);
    }
}