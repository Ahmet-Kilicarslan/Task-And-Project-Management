package com.ahmet.tpm.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/*
 Factory for creating consistently styled components


 */
public class ComponentFactory {

    // ==================== BUTTONS ====================

    /**
     * Create a styled navigation button (for navbar)
     */
    public static JButton createNavButton(String text, boolean selected) {
        JButton button = new JButton(text);

        // Remove all default styling
        button.setFont(StyleUtil.FONT_BUTTON);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(StyleUtil.createPaddingBorder(10, 20, 10, 20));

        // Critical: Override UI to prevent black text
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());

        // Set initial colors
        if (selected) {
            button.setForeground(StyleUtil.PRIMARY);
            button.setOpaque(true);
            button.setBackground(StyleUtil.ACTIVE_LIGHT);
        } else {
            button.setForeground(StyleUtil.TEXT_SECONDARY);
        }

        return button;
    }

    /**
     * Create a primary action button
     */
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(StyleUtil.FONT_BUTTON);
        button.setForeground(Color.WHITE);
        button.setBackground(StyleUtil.PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, StyleUtil.BUTTON_HEIGHT));

        // Override UI to prevent styling issues
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(StyleUtil.PRIMARY_DARK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(StyleUtil.PRIMARY);
            }
        });

        return button;
    }

    /**
     * Create a danger button (delete, logout, etc.)
     */
    public static JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(StyleUtil.FONT_BODY);
        button.setForeground(Color.WHITE);
        button.setBackground(StyleUtil.DANGER);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(StyleUtil.createPaddingBorder(8, 16, 8, 16));

        // Override UI
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(StyleUtil.DANGER_DARK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(StyleUtil.DANGER);
            }
        });

        return button;
    }

    /**
     * Create a secondary/outline button
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(StyleUtil.FONT_BUTTON);
        button.setForeground(StyleUtil.PRIMARY);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtil.PRIMARY, 2),
                StyleUtil.createPaddingBorder(8, 18, 8, 18)
        ));
        button.setPreferredSize(new Dimension(120, StyleUtil.BUTTON_HEIGHT));

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(StyleUtil.ACTIVE_LIGHT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });

        return button;
    }

    // ==================== LABELS ====================

    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(StyleUtil.FONT_LARGE);
        label.setForeground(StyleUtil.TEXT_PRIMARY);
        return label;
    }

    public static JLabel createHeadingLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(StyleUtil.FONT_HEADING);
        label.setForeground(StyleUtil.TEXT_PRIMARY);
        return label;
    }

    public static JLabel createBodyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(StyleUtil.FONT_BODY);
        label.setForeground(StyleUtil.TEXT_SECONDARY);
        return label;
    }

    // ==================== PANELS ====================

    public static JPanel createCard() {
        JPanel panel = new JPanel();
        StyleUtil.styleCard(panel);
        return panel;
    }

    public static JPanel createContentPanel() {
        JPanel panel = new JPanel();
        StyleUtil.stylePanel(panel);
        panel.setBorder(StyleUtil.createPaddingBorder(
                StyleUtil.CONTENT_PADDING,
                StyleUtil.CONTENT_PADDING,
                StyleUtil.CONTENT_PADDING,
                StyleUtil.CONTENT_PADDING
        ));
        return panel;
    }
}