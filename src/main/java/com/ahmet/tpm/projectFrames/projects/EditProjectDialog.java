package com.ahmet.tpm.projectFrames.projects;

import com.ahmet.tpm.dao.DepartmentDao;
import com.ahmet.tpm.dao.ProjectDao;
import com.ahmet.tpm.dao.ProjectStatusDao;
import com.ahmet.tpm.projectFrames.MainFrame;
import com.ahmet.tpm.models.Department;
import com.ahmet.tpm.models.Project;
import com.ahmet.tpm.models.ProjectStatus;
import com.ahmet.tpm.utils.ComponentFactory;
import com.ahmet.tpm.utils.StyleUtil;
import com.ahmet.tpm.utils.UIHelper;
import com.ahmet.tpm.service.NotificationService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class EditProjectDialog extends JDialog {

    private ProjectsModulePanel parentModule;
    private MainFrame mainFrame;
    private NotificationService notificationService;

    // DAOs
    private ProjectDao projectDao;
    private ProjectStatusDao statusDao;
    private DepartmentDao departmentDao;

    // Current project being edited
    private Project project;

    // Form fields
    private JTextField txtProjectName;
    private JTextField txtStartDate;
    private JTextField txtDeadline;
    private JComboBox<ProjectStatus> cmbStatus;
    private JComboBox<Department> cmbDepartment;
    private JTextArea txtDescription;

    public EditProjectDialog(MainFrame mainFrame, ProjectsModulePanel parentModule, int projectId) {
        super(mainFrame, "Edit Project", true);  // true = modal
        this.mainFrame = mainFrame;
        this.parentModule = parentModule;
        this.projectDao = new ProjectDao();
        this.statusDao = new ProjectStatusDao();
        this.departmentDao = new DepartmentDao();
        this.notificationService = new NotificationService();

        // Load project
        this.project = projectDao.findById(projectId);

        if (this.project == null) {
            UIHelper.showError(mainFrame, "Project not found!");
            dispose();
            return;
        }

        initializeDialog();
        loadDropdownData();
        populateFormWithProjectData();
    }

    private void initializeDialog() {
        setSize(600, 650);
        setLocationRelativeTo(mainFrame);
        setLayout(new BorderLayout());
        setResizable(false);

        // Content panel
        JPanel contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(StyleUtil.SURFACE);
        panel.setBorder(StyleUtil.createPaddingBorder(20));

        // Title
        JLabel titleLabel = ComponentFactory.createHeadingLabel(" Edit Project");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));

        // Form fields
        panel.add(createFormField("Project Name *", txtProjectName = new JTextField()));
        panel.add(createFormField("Start Date (YYYY-MM-DD)", txtStartDate = new JTextField()));
        panel.add(createFormField("Deadline (YYYY-MM-DD)", txtDeadline = new JTextField()));

        // Status dropdown
        cmbStatus = new JComboBox<>();
        cmbStatus.setFont(StyleUtil.FONT_BODY);
        cmbStatus.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ProjectStatus) {
                    setText(((ProjectStatus) value).getStatusName());
                }
                return this;
            }
        });
        panel.add(createFormField("Status *", cmbStatus));

        // Department dropdown
        cmbDepartment = new JComboBox<>();
        cmbDepartment.setFont(StyleUtil.FONT_BODY);
        cmbDepartment.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("-- No Department --");
                } else if (value instanceof Department) {
                    setText(((Department) value).getDepartmentName());
                }
                return this;
            }
        });
        panel.add(createFormField("Department (Optional)", cmbDepartment));

        // Description
        JLabel descLabel = ComponentFactory.createBodyLabel("Description");
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(5));

        txtDescription = new JTextArea(5, 40);
        txtDescription.setFont(StyleUtil.FONT_BODY);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBackground(Color.WHITE);
        txtDescription.setForeground(StyleUtil.TEXT_PRIMARY);

        txtDescription.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JScrollPane scrollPane = new JScrollPane(txtDescription);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setMaximumSize(new Dimension(550, 120));
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(10));

        // Note
        JLabel noteLabel = new JLabel("<html><b>Note:</b> Fields marked with * are required</html>");
        noteLabel.setFont(StyleUtil.FONT_SMALL);
        noteLabel.setForeground(StyleUtil.TEXT_SECONDARY);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(noteLabel);

        return panel;
    }

    private JPanel createFormField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(StyleUtil.SURFACE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(550, 70));

        JLabel label = ComponentFactory.createBodyLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(5));

        field.setBackground(Color.WHITE);
        field.setForeground(StyleUtil.TEXT_PRIMARY);

        field.setFont(StyleUtil.FONT_BODY);
        field.setMaximumSize(new Dimension(550, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);


        if (field instanceof JTextField) {
            field.setBackground(Color.WHITE);
            field.setForeground(StyleUtil.TEXT_PRIMARY);
            ((JTextField) field).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(StyleUtil.BORDER),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
        } else if (field instanceof JComboBox) {
            field.setBackground(Color.WHITE);
            field.setForeground(StyleUtil.TEXT_PRIMARY);
        }

        panel.add(field);
        panel.add(Box.createVerticalStrut(10));

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panel.setBackground(StyleUtil.SURFACE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, StyleUtil.BORDER));

        JButton btnSave = ComponentFactory.createPrimaryButton(" Save Changes");
        btnSave.addActionListener(e -> saveChanges());

        JButton btnCancel = ComponentFactory.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        panel.add(btnSave);
        panel.add(btnCancel);

        return panel;
    }

    private void loadDropdownData() {
        // Load statuses
        List<ProjectStatus> statuses = statusDao.findAll();
        for (ProjectStatus status : statuses) {
            cmbStatus.addItem(status);
        }

        // Load departments
        cmbDepartment.addItem(null);  // Optional field
        List<Department> departments = departmentDao.findAll();
        for (Department dept : departments) {
            cmbDepartment.addItem(dept);
        }
    }

    private void populateFormWithProjectData() {
        // Project Name
        txtProjectName.setText(project.getProjectName());

        // Dates
        if (project.getStartDate() != null) {
            txtStartDate.setText(project.getStartDate().toLocalDate().toString());
        }

        if (project.getDeadline() != null) {
            txtDeadline.setText(project.getDeadline().toLocalDate().toString());
        }

        // Status - Select the matching status
        for (int i = 0; i < cmbStatus.getItemCount(); i++) {
            if (cmbStatus.getItemAt(i).getStatusId() == project.getStatusId()) {
                cmbStatus.setSelectedIndex(i);
                break;
            }
        }

        // Department - Select the matching department
        if (project.getDepartmentId() != null) {
            for (int i = 0; i < cmbDepartment.getItemCount(); i++) {
                Department dept = cmbDepartment.getItemAt(i);
                if (dept != null && dept.getDepartmentId() == project.getDepartmentId()) {
                    cmbDepartment.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Description
        if (project.getDescription() != null) {
            txtDescription.setText(project.getDescription());
        }
    }

    private void saveChanges() {
        // Validate
        if (!validateForm()) {
            return;
        }

        try {
            // Update project object
            project.setProjectName(txtProjectName.getText().trim());

            // Parse dates - Convert LocalDate to LocalDateTime
            String startDateStr = txtStartDate.getText().trim();
            if (!startDateStr.isEmpty()) {
                LocalDate date = LocalDate.parse(startDateStr);
                project.setStartDate(date.atStartOfDay());
            } else {
                project.setStartDate(null);
            }

            String deadlineStr = txtDeadline.getText().trim();
            if (!deadlineStr.isEmpty()) {
                LocalDate date = LocalDate.parse(deadlineStr);
                project.setDeadline(date.atTime(23, 59, 59));
            } else {
                project.setDeadline(null);
            }

            // Status
            ProjectStatus selectedStatus = (ProjectStatus) cmbStatus.getSelectedItem();
            project.setStatusId(selectedStatus.getStatusId());

            // Department (optional)
            Department selectedDept = (Department) cmbDepartment.getSelectedItem();
            if (selectedDept != null) {
                project.setDepartmentId(selectedDept.getDepartmentId());
            } else {
                project.setDepartmentId(null);
            }

            // Description
            String description = txtDescription.getText().trim();
            project.setDescription(!description.isEmpty() ? description : null);

            // Update in database
            projectDao.update(project);

            UIHelper.showSuccess(mainFrame, "Project updated successfully!");

            // Notify parent to refresh and close dialog
            parentModule.onProjectUpdated();
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error updating project: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        // Project Name
        if (txtProjectName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Project Name is required!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            txtProjectName.requestFocus();
            return false;
        }

        // Status
        if (cmbStatus.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a status!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Date validation (if provided)
        String startDateStr = txtStartDate.getText().trim();
        String deadlineStr = txtDeadline.getText().trim();

        if (!startDateStr.isEmpty()) {
            try {
                LocalDate.parse(startDateStr);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Start Date format! Use YYYY-MM-DD",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                txtStartDate.requestFocus();
                return false;
            }
        }

        if (!deadlineStr.isEmpty()) {
            try {
                LocalDate deadline = LocalDate.parse(deadlineStr);

                // If both dates provided, deadline should be after start date
                if (!startDateStr.isEmpty()) {
                    LocalDate startDate = LocalDate.parse(startDateStr);
                    if (deadline.isBefore(startDate)) {
                        JOptionPane.showMessageDialog(this,
                                "Deadline must be after Start Date!",
                                "Validation Error",
                                JOptionPane.ERROR_MESSAGE);
                        txtDeadline.requestFocus();
                        return false;
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Deadline format! Use YYYY-MM-DD",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                txtDeadline.requestFocus();
                return false;
            }
        }

        return true;
    }
}