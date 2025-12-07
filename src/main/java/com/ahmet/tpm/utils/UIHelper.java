package com.ahmet.tpm.utils;

import javax.swing.*;

/**
 * Helper methods for common UI operations
 */
public class UIHelper {

    /**
     * Show confirmation dialog
     */
    public static boolean showConfirmDialog(JFrame parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(
                parent,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Show error message
     */
    public static void showError(JFrame parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Show success message
     */
    public static void showSuccess(JFrame parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Set system look and feel
     */
    public static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Center frame on screen
     */
    public static void centerFrame(JFrame frame) {
        frame.setLocationRelativeTo(null);
    }

    /**
     * Update nav button states (for navigation bars)
     */
    public static void updateNavButtonStates(JButton selectedButton, JButton... allButtons) {
        for (JButton button : allButtons) {
            if (button == selectedButton) {
                // Selected state
                button.setForeground(StyleUtil.PRIMARY);
                button.setOpaque(true);
                button.setBackground(StyleUtil.ACTIVE_LIGHT);
            } else {
                // Unselected state
                button.setForeground(StyleUtil.TEXT_SECONDARY);
                button.setOpaque(false);
                button.setBackground(null);
            }
        }
    }

    /**
     * Add hover effect to nav button
     */
    public static void addNavButtonHover(JButton button, JButton... allNavButtons) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                // Only hover if not currently selected
                if (!button.getBackground().equals(StyleUtil.ACTIVE_LIGHT)) {
                    button.setForeground(StyleUtil.PRIMARY);
                    button.setOpaque(true);
                    button.setBackground(StyleUtil.HOVER_LIGHT);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                // Only reset if not selected
                if (!button.getBackground().equals(StyleUtil.ACTIVE_LIGHT)) {
                    button.setForeground(StyleUtil.TEXT_SECONDARY);
                    button.setOpaque(false);
                    button.setBackground(null);
                }
            }
        });
    }
}