package com.ems.ui;

import com.ems.config.DBConnection;
import com.ems.dao.UserDAO;
import com.ems.model.User;
import com.ems.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final UserDAO userDAO = new UserDAO();
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JLabel lblStatus;

    public LoginFrame() {
        setTitle("EMS - Secure Sign In");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(480, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);
        setContentPane(root);

        // Header Banner
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(30, 41, 59), getWidth(), getHeight(), new Color(37, 99, 235));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setBorder(new EmptyBorder(26, 24, 26, 24));

        JLabel lblAppTitle = new JLabel("🎟️ Event Management System", SwingConstants.CENTER);
        lblAppTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblAppTitle.setForeground(Color.WHITE);

        JLabel lblAppSub = new JLabel("Sign in to your role-based account", SwingConstants.CENTER);
        lblAppSub.setFont(UITheme.FONT_REGULAR);
        lblAppSub.setForeground(new Color(203, 213, 225));

        header.add(lblAppTitle, BorderLayout.NORTH);
        header.add(lblAppSub, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        // Form Card Panel
        JPanel cardWrapper = new JPanel(new GridBagLayout());
        cardWrapper.setOpaque(false);
        cardWrapper.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 6, 6, 6);
        c.gridx = 0;
        int row = 0;

        // Email field
        JLabel lblEmail = new JLabel("Email Address");
        lblEmail.setFont(UITheme.FONT_BOLD);
        lblEmail.setForeground(UITheme.TEXT_MAIN);
        c.gridy = row++; formCard.add(lblEmail, c);

        txtEmail = new JTextField(20);
        txtEmail.setFont(UITheme.FONT_REGULAR);
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        c.gridy = row++; formCard.add(txtEmail, c);

        // Password field
        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(UITheme.FONT_BOLD);
        lblPassword.setForeground(UITheme.TEXT_MAIN);
        c.gridy = row++; formCard.add(lblPassword, c);

        txtPassword = new JPasswordField(20);
        txtPassword.setFont(UITheme.FONT_REGULAR);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        c.gridy = row++; formCard.add(txtPassword, c);

        // Show password checkbox
        JCheckBox chkShowPass = new JCheckBox("Show Password");
        chkShowPass.setFont(UITheme.FONT_SMALL);
        chkShowPass.setOpaque(false);
        chkShowPass.addActionListener(e -> {
            if (chkShowPass.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });
        c.gridy = row++; formCard.add(chkShowPass, c);

        // Login Button
        JButton btnLogin = UITheme.createPrimaryButton("Sign In");
        c.gridy = row++;
        c.insets = new Insets(14, 6, 6, 6);
        formCard.add(btnLogin, c);

        // Continue as Guest Button
        JButton btnGuest = UITheme.createSecondaryButton("Continue as Guest (No Login)");
        c.gridy = row++;
        c.insets = new Insets(6, 6, 12, 6);
        formCard.add(btnGuest, c);

        // Demo credentials hint panel
        JPanel hintPanel = new JPanel(new GridLayout(3, 1, 2, 2));
        hintPanel.setBackground(new Color(241, 245, 249));
        hintPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel h1 = new JLabel("🔑 Demo Admin: admin@ems.com / admin123");
        JLabel h2 = new JLabel("🔑 Demo Organizer: organizer@ems.com / organizer123");
        JLabel h3 = new JLabel("🔑 Demo Attendee: guest@ems.com / guest123");
        h1.setFont(UITheme.FONT_SMALL); h1.setForeground(UITheme.TEXT_MUTED);
        h2.setFont(UITheme.FONT_SMALL); h2.setForeground(UITheme.TEXT_MUTED);
        h3.setFont(UITheme.FONT_SMALL); h3.setForeground(UITheme.TEXT_MUTED);
        hintPanel.add(h1); hintPanel.add(h2); hintPanel.add(h3);

        c.gridy = row++;
        formCard.add(hintPanel, c);

        // Status / connection test label
        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(UITheme.FONT_SMALL);
        c.gridy = row++;
        formCard.add(lblStatus, c);

        GridBagConstraints cardC = new GridBagConstraints();
        cardC.weightx = 1.0;
        cardC.weighty = 1.0;
        cardC.fill = GridBagConstraints.HORIZONTAL;
        cardWrapper.add(formCard, cardC);
        root.add(cardWrapper, BorderLayout.CENTER);

        // Events
        btnLogin.addActionListener(e -> handleLogin());
        btnGuest.addActionListener(e -> {
            dispose();
            new GuestDashboard(null).setVisible(true);
        });

        root.getRootPane().setDefaultButton(btnLogin);

        // Background MySQL connection check
        SwingUtilities.invokeLater(this::checkDatabaseConnection);
    }

    private void checkDatabaseConnection() {
        StringBuilder sb = new StringBuilder();
        boolean ok = DBConnection.testConnection(sb);
        if (ok) {
            lblStatus.setForeground(UITheme.SUCCESS);
            lblStatus.setText("● Connected to MySQL (" + DBConnection.getDbUser() + ")");
        } else {
            lblStatus.setForeground(UITheme.DANGER);
            lblStatus.setText("⚠️ MySQL Disconnected. Check credentials in db.properties.");
        }
    }

    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email address.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your password.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = userDAO.authenticate(email, pass);
            if (user != null) {
                dispose();
                openDashboard(user);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid email or password.\n\nPlease check your credentials or run EMSDB.sql to set up default users.",
                        "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Database Error:\n" + ex.getMessage() + "\n\nEnsure MySQL is running and database 'EMSDB' is imported.",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDashboard(User user) {
        SwingUtilities.invokeLater(() -> {
            switch (user.getRole()) {
                case "Admin":
                    new AdminDashboard(user).setVisible(true);
                    break;
                case "Organizer":
                    new OrganizerDashboard(user).setVisible(true);
                    break;
                case "Attendee":
                default:
                    new GuestDashboard(user).setVisible(true);
                    break;
            }
        });
    }

    public static void main(String[] args) {
        UITheme.setupLookAndFeel();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}

