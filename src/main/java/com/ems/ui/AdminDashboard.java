package com.ems.ui;

import com.ems.dao.BookingDAO;
import com.ems.dao.EventDAO;
import com.ems.dao.UserDAO;
import com.ems.model.DashboardStats;
import com.ems.model.Event;
import com.ems.model.User;
import com.ems.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class AdminDashboard extends JFrame {

    private final User currentUser;
    private final UserDAO userDAO = new UserDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    // Metric Labels
    private JLabel lblRevenueVal, lblBookingsVal, lblApprovedVal, lblPendingVal;

    // Table Models
    private DefaultTableModel eventModel, organizerModel, bookingModel;
    private JTable eventTable, organizerTable, bookingTable;

    public AdminDashboard(User user) {
        this.currentUser = user != null ? user : new User(1, "Admin User", "admin@ems.com", "", "Admin");

        setTitle("EMS - Executive Admin Control Center");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);
        setContentPane(root);

        // Header
        JPanel header = UITheme.createHeaderBar(
                "Admin Control Center",
                "Full oversight of events, organizers, bookings, and revenue",
                "Administrator",
                currentUser.getName(),
                () -> {
                    dispose();
                    new LoginFrame().setVisible(true);
                }
        );
        root.add(header, BorderLayout.NORTH);

        // Center: Metric Cards + Tabs
        JPanel center = new JPanel(new BorderLayout(0, 15));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Metric Cards Strip
        center.add(createMetricsStrip(), BorderLayout.NORTH);

        // Main Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.addTab("📋 Approve & Manage Events", createEventsPanel());
        tabs.addTab("👥 Manage Organizers", createOrganizersPanel());
        tabs.addTab("🎟️ All Platform Bookings", createBookingsPanel());

        center.add(tabs, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        // Initial Data Load
        refreshAllData();
    }

    private JPanel createMetricsStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 4, 15, 0));
        strip.setOpaque(false);

        JPanel c1 = UITheme.createMetricCard("Total Revenue", "$0.00", "Gross ticket sales", UITheme.SUCCESS);
        JPanel c2 = UITheme.createMetricCard("Total Bookings", "0", "Completed transactions", UITheme.PRIMARY);
        JPanel c3 = UITheme.createMetricCard("Approved Events", "0", "Active in catalog", UITheme.ACCENT);
        JPanel c4 = UITheme.createMetricCard("Pending Approvals", "0", "Awaiting admin review", UITheme.WARNING);

        lblRevenueVal = (JLabel) ((BorderLayout) c1.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        lblBookingsVal = (JLabel) ((BorderLayout) c2.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        lblApprovedVal = (JLabel) ((BorderLayout) c3.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        lblPendingVal = (JLabel) ((BorderLayout) c4.getLayout()).getLayoutComponent(BorderLayout.CENTER);

        strip.add(c1);
        strip.add(c2);
        strip.add(c3);
        strip.add(c4);
        return strip;
    }

    private JPanel createEventsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnApprove = UITheme.createSuccessButton("✓ Approve Selected");
        JButton btnReject = UITheme.createDangerButton("✕ Reject Selected");
        JButton btnDelete = UITheme.createSecondaryButton("🗑️ Delete Event");
        JButton btnRefresh = UITheme.createSecondaryButton("🔄 Refresh");

        toolbar.add(btnApprove);
        toolbar.add(btnReject);
        toolbar.add(btnDelete);
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        eventModel = new DefaultTableModel(new String[]{
                "EventID", "Title", "Category", "Organizer", "Date", "Time", "Location", "ApprovalStatus"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        eventTable = new JTable(eventModel);
        UITheme.styleTable(eventTable);
        panel.add(new JScrollPane(eventTable), BorderLayout.CENTER);

        // Action Listeners
        btnApprove.addActionListener(e -> updateSelectedEventStatus("Approved"));
        btnReject.addActionListener(e -> updateSelectedEventStatus("Rejected"));

        btnDelete.addActionListener(e -> {
            int row = eventTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select an event to delete.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int id = (int) eventModel.getValueAt(row, 0);
            String title = (String) eventModel.getValueAt(row, 1);
            int opt = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to permanently delete event:\n\"" + title + "\"?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    eventDAO.deleteEvent(id, 0);
                    refreshAllData();
                    JOptionPane.showMessageDialog(this, "Event deleted successfully.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting event: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnRefresh.addActionListener(e -> refreshAllData());
        return panel;
    }

    private void updateSelectedEventStatus(String newStatus) {
        int row = eventTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event from the table first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int eventId = (int) eventModel.getValueAt(row, 0);
        String title = (String) eventModel.getValueAt(row, 1);

        try {
            boolean ok = eventDAO.updateApprovalStatus(eventId, newStatus);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Event \"" + title + "\" is now marked as " + newStatus + ".");
                refreshAllData();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createOrganizersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top form to add organizer
        JPanel topForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topForm.setBackground(Color.WHITE);
        topForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JTextField tfName = new JTextField(12);
        JTextField tfEmail = new JTextField(15);
        JTextField tfPass = new JTextField("organizer123", 10);
        JButton btnAddOrg = UITheme.createPrimaryButton("+ Add Organizer");
        JButton btnRefreshOrg = UITheme.createSecondaryButton("🔄 Refresh");

        topForm.add(new JLabel("Full Name:"));
        topForm.add(tfName);
        topForm.add(new JLabel("Email:"));
        topForm.add(tfEmail);
        topForm.add(new JLabel("Default Password:"));
        topForm.add(tfPass);
        topForm.add(btnAddOrg);
        topForm.add(btnRefreshOrg);
        panel.add(topForm, BorderLayout.NORTH);

        // Organizer Table
        organizerModel = new DefaultTableModel(new String[]{"UserID", "Organizer Name", "Email Address", "Role"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        organizerTable = new JTable(organizerModel);
        UITheme.styleTable(organizerTable);
        panel.add(new JScrollPane(organizerTable), BorderLayout.CENTER);

        btnAddOrg.addActionListener(e -> {
            String name = tfName.getText().trim();
            String email = tfEmail.getText().trim();
            String pass = tfPass.getText().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name, Email, and Password are all required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                boolean ok = userDAO.addOrganizer(name, email, pass);
                if (ok) {
                    tfName.setText("");
                    tfEmail.setText("");
                    JOptionPane.showMessageDialog(this, "Organizer \"" + name + "\" registered successfully!");
                    refreshAllData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to add organizer: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRefreshOrg.addActionListener(e -> refreshAllData());
        return panel;
    }

    private JPanel createBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);
        JButton btnRefreshBookings = UITheme.createSecondaryButton("🔄 Refresh Bookings");
        toolbar.add(btnRefreshBookings);
        panel.add(toolbar, BorderLayout.NORTH);

        bookingModel = new DefaultTableModel(new String[]{
                "BookingID", "Attendee Name", "Attendee Email", "Event Title", "Ticket Tier", "Qty", "Total Paid", "Date", "Status"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        bookingTable = new JTable(bookingModel);
        UITheme.styleTable(bookingTable);
        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);

        btnRefreshBookings.addActionListener(e -> refreshAllData());
        return panel;
    }

    public void refreshAllData() {
        // 1. Refresh Metrics
        try {
            DashboardStats stats = bookingDAO.getSystemStats();
            lblRevenueVal.setText("$" + String.format("%.2f", stats.getTotalRevenue()));
            lblBookingsVal.setText(String.valueOf(stats.getTotalBookings()));
            lblApprovedVal.setText(String.valueOf(stats.getApprovedEvents()));
            lblPendingVal.setText(String.valueOf(stats.getPendingEvents()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 2. Refresh Events Table
        try {
            eventModel.setRowCount(0);
            List<Event> events = eventDAO.getAllEventsForAdmin();
            for (Event ev : events) {
                eventModel.addRow(new Object[]{
                        ev.getEventId(),
                        ev.getTitle(),
                        ev.getCategory(),
                        ev.getOrganizerName() != null ? ev.getOrganizerName() : "ID: " + ev.getOrganizerId(),
                        ev.getEventDate(),
                        ev.getEventTime(),
                        ev.getLocation(),
                        ev.getApprovalStatus()
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 3. Refresh Organizers Table
        try {
            organizerModel.setRowCount(0);
            List<User> orgs = userDAO.findAllOrganizers();
            for (User u : orgs) {
                organizerModel.addRow(new Object[]{u.getUserId(), u.getName(), u.getEmail(), u.getRole()});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 4. Refresh Bookings Table
        try {
            bookingModel.setRowCount(0);
            List<BookingDAO.BookingRecord> bookings = bookingDAO.getAllBookingsForAdmin();
            for (BookingDAO.BookingRecord b : bookings) {
                bookingModel.addRow(new Object[]{
                        b.bookingId,
                        b.userName,
                        b.userEmail,
                        b.eventTitle,
                        b.ticketType,
                        b.quantity,
                        "$" + String.format("%.2f", b.amount != null ? b.amount : BigDecimal.ZERO),
                        b.bookingDate,
                        b.status
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

