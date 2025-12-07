package com.ahmet.tpm.utils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/*
  Central styling configuration



 */
public class StyleUtil {

    // ==================== COLOR PALETTE ====================
    // Primary Colors
    public static final Color PRIMARY = new Color(102, 126, 234);
    public static final Color PRIMARY_DARK = new Color(81, 101, 187);
    public static final Color PRIMARY_LIGHT = new Color(232, 240, 254);

    // Semantic Colors
    public static final Color SUCCESS = new Color(40, 167, 69);
    public static final Color DANGER = new Color(220, 53, 69);
    public static final Color DANGER_DARK = new Color(200, 35, 51);
    public static final Color WARNING = new Color(255, 193, 7);
    public static final Color INFO = new Color(23, 162, 184);

    // Neutral Colors
    public static final Color BACKGROUND = new Color(245, 247, 250);
    public static final Color SURFACE = new Color(255, 255, 255);
    public static final Color BORDER = new Color(222, 226, 230);

    // Text Colors
    public static final Color TEXT_PRIMARY = new Color(52, 58, 64);
    public static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    public static final Color TEXT_DISABLED = new Color(173, 181, 189);

    // Hover/Active States
    public static final Color HOVER_LIGHT = new Color(248, 249, 250);
    public static final Color ACTIVE_LIGHT = new Color(232, 240, 254);

    // ==================== FONTS ====================
    public static final Font FONT_LARGE = new Font("Segoe UI", Font.BOLD, 32);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBHEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);

    // ==================== BORDERS ====================
    public static Border createLineBorder() {
        return BorderFactory.createLineBorder(BORDER, 1);
    }

    public static Border createLineBorder(int thickness) {
        return BorderFactory.createLineBorder(BORDER, thickness);
    }

    public static Border createPaddingBorder(int padding) {
        return BorderFactory.createEmptyBorder(padding, padding, padding, padding);
    }

    public static Border createPaddingBorder(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                createLineBorder(),
                createPaddingBorder(20)
        );
    }

    // ==================== DIMENSIONS ====================
    public static final int NAVBAR_HEIGHT = 70;
    public static final int BUTTON_HEIGHT = 40;
    public static final int INPUT_HEIGHT = 40;
    public static final int CARD_PADDING = 20;
    public static final int CONTENT_PADDING = 30;

    // ==================== HELPER METHODS ====================

    /**
     * Apply card styling to a panel
     */
    public static void styleCard(JPanel panel) {
        panel.setBackground(SURFACE);
        panel.setBorder(createCardBorder());
    }

    /**
     * Apply standard panel styling
     */
    public static void stylePanel(JPanel panel) {
        panel.setBackground(BACKGROUND);
    }
}