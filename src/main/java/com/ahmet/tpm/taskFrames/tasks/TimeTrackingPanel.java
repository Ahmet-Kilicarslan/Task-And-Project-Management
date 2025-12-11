package com.ahmet.tpm.taskFrames.tasks;

import com.ahmet.tpm.dao.TimeTrackingDao;
import com.ahmet.tpm.dao.UserDao;
import com.ahmet.tpm.models.TimeTracking;
import com.ahmet.tpm.models.User;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;
import com.ahmet.tpm.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class TimeTrackingPanel extends JPanel {

    private TimeTrackingDao timeTrackingDao;
    private UserDao userDao;
    private int taskId;
    private int currentUserId;
    private JFrame parentFrame;
    private Runnable onUpdate;

    // UI Components
    private JTable timeEntriesTable;
    private DefaultTableModel tableModel;
    private JLabel lblTotalHours;

    public TimeTrackingPanel(JFrame parent, int taskId, int currentUserId, Runnable onUpdate) {
        this.parentFrame = parent;
        this.taskId = taskId;
        this.currentUserId = currentUserId;
        this.onUpdate = onUpdate;
        this.timeTrackingDao = new TimeTrackingDao();
        this.userDao = new UserDao();

        initializeUI();
        loadTimeEntries();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(StyleUtil.createPaddingBorder(15));

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Table
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Footer with total hours
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = ComponentFactory.createHeadingLabel("Time Entries");
        panel.add(titleLabel, BorderLayout.WEST);

        // Add Time button
        JButton btnAddTime = ComponentFactory.createPrimaryButton("Log Time");
        btnAddTime.setPreferredSize(new Dimension(130, 35));
        btnAddTime.addActionListener(e -> openLogTimeDialog());
        panel.add(btnAddTime, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Create table
        String[] columnNames = {"Date", "User", "Hours", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only actions column is editable
            }
        };

        timeEntriesTable = new JTable(tableModel);
        timeEntriesTable.setFont(StyleUtil.FONT_BODY);
        timeEntriesTable.setRowHeight(40);
        timeEntriesTable.setShowGrid(true);
        timeEntriesTable.setGridColor(StyleUtil.BORDER);
        timeEntriesTable.setBackground(Color.WHITE);
        timeEntriesTable.setForeground(Color.BLACK);
        timeEntriesTable.setSelectionBackground(new Color(230, 240, 255));
        timeEntriesTable.setSelectionForeground(Color.BLACK);

        // Center align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < timeEntriesTable.getColumnCount(); i++) {
            timeEntriesTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Set column widths
        timeEntriesTable.getColumnModel().getColumn(0).setPreferredWidth(120); // Date
        timeEntriesTable.getColumnModel().getColumn(1).setPreferredWidth(200); // User
        timeEntriesTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Hours
        timeEntriesTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Actions

        // Add action buttons renderer
        timeEntriesTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        timeEntriesTable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(timeEntriesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(StyleUtil.BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JLabel label = ComponentFactory.createBodyLabel("Total Hours Logged: ");
        label.setFont(StyleUtil.FONT_BUTTON);

        lblTotalHours = ComponentFactory.createBodyLabel("0.0 hours");
        lblTotalHours.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalHours.setForeground(StyleUtil.PRIMARY);

        panel.add(label);
        panel.add(lblTotalHours);

        return panel;
    }

    public void loadTimeEntries() {
        tableModel.setRowCount(0);

        List<TimeTracking> entries = timeTrackingDao.findByTask(taskId);

        for (TimeTracking entry : entries) {
            try {
                User user = userDao.findById(entry.getUserId());
                String userName = user != null ? user.getFullName() : "Unknown User";

                Object[] row = {
                        entry.getWorkDate().toString(),
                        userName,
                        entry.getHoursWorked() + "h",
                        entry.getTimeEntryId() // Store ID for actions
                };
                tableModel.addRow(row);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Update total hours
        double totalHours = timeTrackingDao.getTotalHoursForTask(taskId);
        lblTotalHours.setText(String.format("%.1f hours", totalHours));

        // Call onUpdate callback if provided
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    private void openLogTimeDialog() {
        LogTimeDialog dialog = new LogTimeDialog(parentFrame, taskId, currentUserId, this::loadTimeEntries);
        dialog.setVisible(true);
    }

    private void deleteTimeEntry(int timeEntryId) {
        boolean confirm = UIHelper.showConfirmDialog(parentFrame,
                "Are you sure you want to delete this time entry?",
                "Confirm Delete");

        if (confirm) {
            timeTrackingDao.delete(timeEntryId);
            UIHelper.showSuccess(parentFrame, "Time entry deleted successfully!");
            loadTimeEntries();
        }
    }

    // Button Renderer for Actions column
    class ButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private JButton deleteButton;

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setBackground(Color.WHITE);

            deleteButton = new JButton("Delete");
            deleteButton.setFont(StyleUtil.FONT_SMALL);
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setBackground(StyleUtil.DANGER);
            deleteButton.setFocusPainted(false);
            deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

            add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Button Editor for Actions column
    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton deleteButton;
        private int timeEntryId;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);

            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setBackground(Color.WHITE);

            deleteButton = new JButton("Delete");
            deleteButton.setFont(StyleUtil.FONT_SMALL);
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setBackground(StyleUtil.DANGER);
            deleteButton.setFocusPainted(false);
            deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                deleteTimeEntry(timeEntryId);
            });

            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            timeEntryId = (Integer) value;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return timeEntryId;
        }
    }
}