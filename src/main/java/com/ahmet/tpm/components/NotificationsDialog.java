package com.ahmet.tpm.components;

import com.ahmet.tpm.dao.NotificationDao;
import com.ahmet.tpm.models.Notification;
import com.ahmet.tpm.service.NotificationService;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Full-screen Notifications Dialog
 * Shows all notifications with filtering and actions
 */
public class NotificationsDialog extends JDialog {

    private int userId;
    private NotificationDao notificationDao;

    private JTable notificationsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterComboBox;
    private JLabel lblTotalCount;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public NotificationsDialog(Frame parent, int userId) {
        super(parent, "Bildirimler", true);
        this.userId = userId;
        this.notificationDao = new NotificationDao();

        setSize(1000, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initializeUI();
        loadNotifications("all");
    }

    private void initializeUI() {
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(StyleUtil.SURFACE);
        headerPanel.setBorder(StyleUtil.createPaddingBorder(20));

        JLabel titleLabel = ComponentFactory.createHeadingLabel("Bildirimler");
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setBackground(StyleUtil.SURFACE);

        JLabel filterLabel = ComponentFactory.createBodyLabel("Filtre:");
        filterComboBox = new JComboBox<>(new String[]{
                "Tümü",
                "Okunmamış",
                "Okunmuş"
        });
        filterComboBox.setFont(StyleUtil.FONT_BODY);
        filterComboBox.setBackground(Color.WHITE);
        filterComboBox.addActionListener(e -> applyFilter());

        filterPanel.add(filterLabel);
        filterPanel.add(filterComboBox);

        headerPanel.add(filterPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(StyleUtil.BACKGROUND);
        tablePanel.setBorder(StyleUtil.createPaddingBorder(20));

        String[] columnNames = {"ID", "Tür", "Başlık", "Mesaj", "Durum", "Tarih"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        notificationsTable = new JTable(tableModel);
        notificationsTable.setFont(StyleUtil.FONT_BODY);
        notificationsTable.setRowHeight(40);
        notificationsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        notificationsTable.getTableHeader().setFont(StyleUtil.FONT_BUTTON);
        notificationsTable.getTableHeader().setBackground(StyleUtil.PRIMARY_LIGHT);
        notificationsTable.setBackground(Color.WHITE);
        notificationsTable.setForeground(StyleUtil.TEXT_PRIMARY);
        notificationsTable.setGridColor(StyleUtil.BORDER);

        // Column widths
        notificationsTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        notificationsTable.getColumnModel().getColumn(1).setPreferredWidth(150);  // Type
        notificationsTable.getColumnModel().getColumn(2).setPreferredWidth(200);  // Title
        notificationsTable.getColumnModel().getColumn(3).setPreferredWidth(300);  // Message
        notificationsTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Status
        notificationsTable.getColumnModel().getColumn(5).setPreferredWidth(150);  // Date

        // Double-click handler
        notificationsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleNotificationDoubleClick();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(notificationsTable);
        scrollPane.setBorder(StyleUtil.createLineBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Stats panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.setBackground(StyleUtil.BACKGROUND);
        statsPanel.setBorder(StyleUtil.createPaddingBorder(10, 0, 0, 0));

        lblTotalCount = ComponentFactory.createBodyLabel("Toplam: 0");
        statsPanel.add(lblTotalCount);

        tablePanel.add(statsPanel, BorderLayout.SOUTH);

        add(tablePanel, BorderLayout.CENTER);

        // Bottom panel with action buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        bottomPanel.setBackground(StyleUtil.SURFACE);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, StyleUtil.BORDER));

        JButton btnMarkSelected = ComponentFactory.createPrimaryButton("Seçilenleri Okundu İşaretle");
        btnMarkSelected.addActionListener(e -> markSelectedAsRead());

        JButton btnMarkAll = ComponentFactory.createSecondaryButton("Tümünü Okundu İşaretle");
        btnMarkAll.addActionListener(e -> markAllAsRead());

        JButton btnDeleteSelected = ComponentFactory.createDangerButton("Seçilenleri Sil");
        btnDeleteSelected.addActionListener(e -> deleteSelected());

        JButton btnDeleteAll = ComponentFactory.createDangerButton("Tümünü Sil");
        btnDeleteAll.addActionListener(e -> deleteAll());

        JButton btnRefresh = ComponentFactory.createSecondaryButton("Yenile");
        btnRefresh.addActionListener(e -> applyFilter());

        JButton btnClose = ComponentFactory.createSecondaryButton("Kapat");
        btnClose.addActionListener(e -> dispose());

        bottomPanel.add(btnMarkSelected);
        bottomPanel.add(btnMarkAll);
        bottomPanel.add(btnDeleteSelected);
        bottomPanel.add(btnDeleteAll);
        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnClose);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadNotifications(String filter) {
        tableModel.setRowCount(0);

        List<Notification> notifications;

        switch (filter) {
            case "unread" -> notifications = notificationDao.getUnreadNotifications(userId);
            case "read" -> {
                // Get all and filter read
                List<Notification> all = notificationDao.getUserNotifications(userId, 0, 1000);
                notifications = all.stream().filter(Notification::isRead).toList();
            }
            default -> notifications = notificationDao.getUserNotifications(userId, 0, 1000);
        }

        for (Notification notification : notifications) {
            Object[] row = {
                    notification.getNotificationId(),
                    notification.getNotificationType() != null ? notification.getNotificationType() : "Genel",
                    notification.getTitle(),
                    notification.getMessage(),
                    notification.isRead() ? "Okundu" : "Okunmadı",
                    notification.getCreatedAt() != null ?
                            notification.getCreatedAt().format(DATE_FORMATTER) : "-"
            };
            tableModel.addRow(row);
        }

        lblTotalCount.setText("Toplam: " + notifications.size());
    }

    private void applyFilter() {
        String selected = (String) filterComboBox.getSelectedItem();
        String filter = switch (selected) {
            case "Okunmamış" -> "unread";
            case "Okunmuş" -> "read";
            default -> "all";
        };
        loadNotifications(filter);
    }

    private void handleNotificationDoubleClick() {
        int selectedRow = notificationsTable.getSelectedRow();
        if (selectedRow == -1) return;

        int notificationId = (int) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 4);

        // Mark as read if unread
        if (status.equals("Okunmadı")) {
            notificationDao.markAsRead(notificationId);
            applyFilter();
        }

        // TODO: Navigate to related task/project
        System.out.println("Notification clicked: " + notificationId);
    }

    private void markSelectedAsRead() {
        int[] selectedRows = notificationsTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Lütfen bildirim seçin",
                    "Uyarı",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int marked = 0;
        for (int row : selectedRows) {
            int notificationId = (int) tableModel.getValueAt(row, 0);
            if (notificationDao.markAsRead(notificationId)) {
                marked++;
            }
        }

        JOptionPane.showMessageDialog(this,
                marked + " bildirim okundu olarak işaretlendi",
                "Başarılı",
                JOptionPane.INFORMATION_MESSAGE);

        applyFilter();
    }

    private void markAllAsRead() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Tüm bildirimler okundu olarak işaretlensin mi?",
                "Onayla",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            notificationDao.markAllAsRead(userId);
            JOptionPane.showMessageDialog(this,
                    "Tüm bildirimler okundu olarak işaretlendi",
                    "Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);
            applyFilter();
        }
    }

    private void deleteSelected() {
        int[] selectedRows = notificationsTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Lütfen bildirim seçin",
                    "Uyarı",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                selectedRows.length + " bildirim silinsin mi?",
                "Onayla",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            int deleted = 0;
            for (int row : selectedRows) {
                int notificationId = (int) tableModel.getValueAt(row, 0);
                if (notificationDao.delete(notificationId)) {
                    deleted++;
                }
            }

            JOptionPane.showMessageDialog(this,
                    deleted + " bildirim silindi",
                    "Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);

            applyFilter();
        }
    }

    private void deleteAll() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "TÜM bildirimler silinsin mi? Bu işlem geri alınamaz!",
                "Onayla",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            notificationDao.deleteAllNotifications(userId);
            JOptionPane.showMessageDialog(this,
                    "Tüm bildirimler silindi",
                    "Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);
            applyFilter();
        }
    }
}