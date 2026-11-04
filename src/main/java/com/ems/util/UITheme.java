package com.ems.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class UITheme {

    // Color Palette
    public static final Color PRIMARY = new Color(37, 99, 235);        // Royal Blue #2563EB
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);   // #1D4ED8
    public static final Color ACCENT = new Color(14, 165, 233);        // Cyan/Sky #0EA5E9
    public static final Color SUCCESS = new Color(16, 185, 129);       // Emerald #10B981
    public static final Color WARNING = new Color(245, 158, 11);       // Amber #F59E0B
    public static final Color DANGER = new Color(239, 68, 68);         // Rose/Red #EF4444
    public static final Color BACKGROUND = new Color(248, 250, 252);   // Slate 50 #F8FAFC
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_MAIN = new Color(15, 23, 42);       // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);   // Slate 500
    public static final Color BORDER = new Color(226, 232, 240);       // Slate 200

    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    /**
     * Attempts to initialize FlatLaf; safely falls back to System Look & Feel if not present.
     */
    public static void setupLookAndFeel() {
        try {
            // Attempt FlatLaf Light
            Class<?> flatLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            Object flatLaf = flatLafClass.getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel((LookAndFeel) flatLaf);

            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Table.rowHeight", 36);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("TableHeader.font", FONT_BOLD);
        } catch (Throwable t) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        UIManager.put("defaultFont", FONT_REGULAR);
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_MAIN);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(SUCCESS);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    public static JTextField createSearchField(String placeholder) {
        JTextField tf = new JTextField(20);
        tf.setFont(FONT_REGULAR);
        tf.setForeground(TEXT_MUTED);
        tf.setText(placeholder);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(TEXT_MAIN);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (tf.getText().trim().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(TEXT_MUTED);
                }
            }
        });
        return tf;
    }

    public static JPanel createHeaderBar(String title, String subtitle, String userRole, String userName, Runnable onLogout) {
        JPanel header = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(30, 41, 59), getWidth(), getHeight(), new Color(15, 23, 42));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Left: Branding & Title
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);
        JLabel lblTitle = new JLabel("🎟️ " + title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel(subtitle != null ? subtitle : "Event Registration & Ticketing Platform");
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(new Color(148, 163, 184));

        left.add(lblTitle);
        left.add(lblSub);
        header.add(left, BorderLayout.WEST);

        // Right: User badge & Logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        if (userName != null && !userName.isEmpty()) {
            JLabel lblUser = new JLabel("👤 " + userName + " (" + userRole + ")");
            lblUser.setFont(FONT_REGULAR);
            lblUser.setForeground(new Color(226, 232, 240));
            right.add(lblUser);
        }

        if (onLogout != null) {
            JButton btnLogout = createSecondaryButton("Log Out");
            btnLogout.setForeground(new Color(239, 68, 68));
            btnLogout.addActionListener(e -> onLogout.run());
            right.add(btnLogout);
        }

        header.add(right, BorderLayout.EAST);
        return header;
    }

    public static JPanel createMetricCard(String title, String value, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

                // Top accent strip
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth() - 1, 5, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(TEXT_MUTED);

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblVal.setForeground(TEXT_MAIN);

        JLabel lblSub = new JLabel(subtitle != null ? subtitle : "");
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(TEXT_MUTED);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);
        return card;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setFont(FONT_REGULAR);
        table.setGridColor(BORDER);
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(224, 231, 255));
        table.setSelectionForeground(TEXT_MAIN);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_MAIN);
        header.setPreferredSize(new Dimension(0, 36));

        // Center status column with badge renderer if column exists
        DefaultTableCellRenderer badgeRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(FONT_BOLD);

                if (value != null) {
                    String str = value.toString();
                    if ("Approved".equalsIgnoreCase(str) || "Paid".equalsIgnoreCase(str)) {
                        lbl.setForeground(SUCCESS);
                        lbl.setText("● " + str);
                    } else if ("Pending".equalsIgnoreCase(str)) {
                        lbl.setForeground(WARNING);
                        lbl.setText("⏳ " + str);
                    } else if ("Rejected".equalsIgnoreCase(str) || "Cancelled".equalsIgnoreCase(str) || "Failed".equalsIgnoreCase(str)) {
                        lbl.setForeground(DANGER);
                        lbl.setText("✖ " + str);
                    } else {
                        lbl.setForeground(TEXT_MAIN);
                    }
                }
                return lbl;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            String colName = table.getColumnName(i).toLowerCase();
            if (colName.contains("status") || colName.contains("approval")) {
                table.getColumnModel().getColumn(i).setCellRenderer(badgeRenderer);
            }
        }
    }
}

