package com.ems.ui;

import com.ems.dao.BookingDAO;
import com.ems.dao.EventDAO;
import com.ems.dao.TicketDAO;
import com.ems.model.DashboardStats;
import com.ems.model.Event;
import com.ems.model.Ticket;
import com.ems.model.User;
import com.ems.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class OrganizerDashboard extends JFrame {

    private final User currentOrganizer;
    private final EventDAO eventDAO = new EventDAO();
    private final TicketDAO ticketDAO = new TicketDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    // Metrics
    private JLabel lblMyEventsVal, lblTicketsSoldVal, lblRevenueVal;

    // Table Models
    private DefaultTableModel eventModel, ticketModel, rosterModel;
    private JTable eventTable, ticketTable, rosterTable;

    // Dropdown for Ticket creation
    private JComboBox<EventDropdownItem> cbOrganizerEvents;

    public OrganizerDashboard(User organizer) {
        this.currentOrganizer = organizer != null ? organizer : new User(2, "Organizer", "organizer@ems.com", "", "Organizer");

        setTitle("EMS - Organizer Studio & Event Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);
        setContentPane(root);

        // Header
        JPanel header = UITheme.createHeaderBar(
                "Organizer Studio",
                "Create events, manage ticket tiers, and track attendee sales",
                "Organizer",
                currentOrganizer.getName(),
                () -> {
                    dispose();
                    new LoginFrame().setVisible(true);
                }
        );
        root.add(header, BorderLayout.NORTH);

        // Main Center Panel
        JPanel center = new JPanel(new BorderLayout(0, 15));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Metric Strip
        center.add(createMetricsStrip(), BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.addTab("📅 My Events", createEventsPanel());
        tabs.addTab("🎟️ Manage Ticket Tiers", createTicketsPanel());
        tabs.addTab("👥 Attendee Sales Roster", createRosterPanel());

        center.add(tabs, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        // Initial Data Load
        refreshAllData();
    }

    private JPanel createMetricsStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 3, 15, 0));
        strip.setOpaque(false);

        JPanel c1 = UITheme.createMetricCard("My Events", "0", "Total organized events", UITheme.PRIMARY);
        JPanel c2 = UITheme.createMetricCard("Tickets Sold", "0", "Total tickets bought", UITheme.ACCENT);
        JPanel c3 = UITheme.createMetricCard("Total Earnings", "$0.00", "Gross revenue from events", UITheme.SUCCESS);

        lblMyEventsVal = (JLabel) ((BorderLayout) c1.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        lblTicketsSoldVal = (JLabel) ((BorderLayout) c2.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        lblRevenueVal = (JLabel) ((BorderLayout) c3.getLayout()).getLayoutComponent(BorderLayout.CENTER);

        strip.add(c1);
        strip.add(c2);
        strip.add(c3);
        return strip;
    }

    private JPanel createEventsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Event Creation Form Card
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 6, 4, 6);

        JTextField tfTitle = new JTextField(15);
        JComboBox<String> cbCategory = new JComboBox<>(new String[]{
                "Technology", "Workshop", "Concert", "Conference", "Networking", "Sports", "General"
        });
        JTextField tfDate = new JTextField(LocalDate.now().plusDays(14).toString(), 10);
        JTextField tfTime = new JTextField("18:00:00", 8);
        JTextField tfLocation = new JTextField(15);
        JTextField tfDescription = new JTextField(25);

        JButton btnCreate = UITheme.createPrimaryButton("+ Create Event");
        JButton btnDelete = UITheme.createDangerButton("🗑️ Delete Event");
        JButton btnRefresh = UITheme.createSecondaryButton("🔄 Refresh");

        // Row 0
        c.gridx = 0; c.gridy = 0; formCard.add(new JLabel("Event Title:"), c);
        c.gridx = 1; c.gridy = 0; formCard.add(tfTitle, c);
        c.gridx = 2; c.gridy = 0; formCard.add(new JLabel("Category:"), c);
        c.gridx = 3; c.gridy = 0; formCard.add(cbCategory, c);

        // Row 1
        c.gridx = 0; c.gridy = 1; formCard.add(new JLabel("Date (yyyy-mm-dd):"), c);
        c.gridx = 1; c.gridy = 1; formCard.add(tfDate, c);
        c.gridx = 2; c.gridy = 1; formCard.add(new JLabel("Time (hh:mm:ss):"), c);
        c.gridx = 3; c.gridy = 1; formCard.add(tfTime, c);

        // Row 2
        c.gridx = 0; c.gridy = 2; formCard.add(new JLabel("Venue / Location:"), c);
        c.gridx = 1; c.gridy = 2; formCard.add(tfLocation, c);
        c.gridx = 2; c.gridy = 2; formCard.add(new JLabel("Description:"), c);
        c.gridx = 3; c.gridy = 2; formCard.add(tfDescription, c);

        // Row 3 - Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnCreate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);

        c.gridx = 0; c.gridy = 3; c.gridwidth = 4;
        c.insets = new Insets(10, 6, 4, 6);
        formCard.add(btnPanel, c);

        panel.add(formCard, BorderLayout.NORTH);

        // Table
        eventModel = new DefaultTableModel(new String[]{
                "EventID", "Title", "Category", "Date", "Time", "Location", "ApprovalStatus"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        eventTable = new JTable(eventModel);
        UITheme.styleTable(eventTable);
        panel.add(new JScrollPane(eventTable), BorderLayout.CENTER);

        // Logic
        btnCreate.addActionListener(e -> {
            String title = tfTitle.getText().trim();
            String cat = (String) cbCategory.getSelectedItem();
            String dateStr = tfDate.getText().trim();
            String timeStr = tfTime.getText().trim();
            String loc = tfLocation.getText().trim();
            String desc = tfDescription.getText().trim();

            if (title.isEmpty() || dateStr.isEmpty() || timeStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title, Date, and Time are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Date sqlDate = Date.valueOf(dateStr);
                Time sqlTime = Time.valueOf(timeStr);

                Event ev = new Event(0, currentOrganizer.getUserId(), title, desc, cat, sqlDate, sqlTime, loc, "Pending");
                boolean ok = eventDAO.createEvent(ev);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Event created successfully!\nIt has been submitted to Admin for approval.");
                    tfTitle.setText("");
                    tfLocation.setText("");
                    tfDescription.setText("");
                    refreshAllData();
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Date or Time format.\nPlease use YYYY-MM-DD for date and HH:MM:SS for time.", "Format Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDelete.addActionListener(e -> {
            int row = eventTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select an event to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int eventId = (int) eventModel.getValueAt(row, 0);
            String title = (String) eventModel.getValueAt(row, 1);
            int opt = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete event \"" + title + "\"?\nAll associated tickets and bookings will also be deleted.",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    eventDAO.deleteEvent(eventId, currentOrganizer.getUserId());
                    refreshAllData();
                    JOptionPane.showMessageDialog(this, "Event deleted.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting event: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnRefresh.addActionListener(e -> refreshAllData());
        return panel;
    }

    private JPanel createTicketsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Form Card
        JPanel topForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        topForm.setBackground(Color.WHITE);
        topForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        cbOrganizerEvents = new JComboBox<>();
        JTextField tfType = new JTextField("General Admission", 12);
        JTextField tfPrice = new JTextField("25.00", 6);
        JTextField tfQuantity = new JTextField("100", 5);

        JButton btnAddTicket = UITheme.createPrimaryButton("+ Add Ticket Tier");
        JButton btnDeleteTicket = UITheme.createDangerButton("🗑️ Delete");
        JButton btnRefreshTickets = UITheme.createSecondaryButton("🔄 Refresh");

        topForm.add(new JLabel("Select Event:"));
        topForm.add(cbOrganizerEvents);
        topForm.add(new JLabel("Tier Name:"));
        topForm.add(tfType);
        topForm.add(new JLabel("Price ($):"));
        topForm.add(tfPrice);
        topForm.add(new JLabel("Quantity:"));
        topForm.add(tfQuantity);
        topForm.add(btnAddTicket);
        topForm.add(btnDeleteTicket);
        topForm.add(btnRefreshTickets);

        panel.add(topForm, BorderLayout.NORTH);

        // Table
        ticketModel = new DefaultTableModel(new String[]{
                "TicketID", "Event Title", "Tier Type", "Price", "Remaining Stock"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        ticketTable = new JTable(ticketModel);
        UITheme.styleTable(ticketTable);
        panel.add(new JScrollPane(ticketTable), BorderLayout.CENTER);

        btnAddTicket.addActionListener(e -> {
            EventDropdownItem item = (EventDropdownItem) cbOrganizerEvents.getSelectedItem();
            if (item == null) {
                JOptionPane.showMessageDialog(this, "Please select an event. If you have no events, create one first.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String type = tfType.getText().trim();
            String priceStr = tfPrice.getText().trim();
            String qtyStr = tfQuantity.getText().trim();

            if (type.isEmpty() || priceStr.isEmpty() || qtyStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All ticket fields are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                BigDecimal price = new BigDecimal(priceStr);
                int qty = Integer.parseInt(qtyStr);
                if (qty <= 0 || price.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(this, "Price and Quantity must be positive values.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Ticket ticket = new Ticket(0, item.eventId, type, price, qty);
                boolean ok = ticketDAO.createTicket(ticket);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Ticket tier \"" + type + "\" added to \"" + item.title + "\"!");
                    refreshAllData();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Price and Quantity must be valid numbers.", "Format Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding ticket: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDeleteTicket.addActionListener(e -> {
            int row = ticketTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a ticket to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int ticketId = (int) ticketModel.getValueAt(row, 0);
            int opt = JOptionPane.showConfirmDialog(this, "Delete this ticket tier?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    ticketDAO.deleteTicket(ticketId);
                    refreshAllData();
                    JOptionPane.showMessageDialog(this, "Ticket deleted.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnRefreshTickets.addActionListener(e -> refreshAllData());
        return panel;
    }

    private JPanel createRosterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);
        JButton btnRefreshRoster = UITheme.createSecondaryButton("🔄 Refresh Attendee Roster");
        toolbar.add(btnRefreshRoster);
        panel.add(toolbar, BorderLayout.NORTH);

        rosterModel = new DefaultTableModel(new String[]{
                "BookingID", "Attendee Name", "Attendee Email", "Event Title", "Tier", "Qty", "Total Paid", "Date"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        rosterTable = new JTable(rosterModel);
        UITheme.styleTable(rosterTable);
        panel.add(new JScrollPane(rosterTable), BorderLayout.CENTER);

        btnRefreshRoster.addActionListener(e -> refreshAllData());
        return panel;
    }

    public void refreshAllData() {
        int orgId = currentOrganizer.getUserId();

        // 1. Refresh Metrics
        try {
            DashboardStats stats = bookingDAO.getOrganizerStats(orgId);
            lblMyEventsVal.setText(String.valueOf(stats.getTotalEvents()));
            lblTicketsSoldVal.setText(String.valueOf(stats.getTicketsSold()));
            lblRevenueVal.setText("$" + String.format("%.2f", stats.getTotalRevenue()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 2. Refresh Events Table & Dropdown
        try {
            eventModel.setRowCount(0);
            cbOrganizerEvents.removeAllItems();
            List<Event> events = eventDAO.getEventsByOrganizer(orgId);
            for (Event ev : events) {
                eventModel.addRow(new Object[]{
                        ev.getEventId(),
                        ev.getTitle(),
                        ev.getCategory(),
                        ev.getEventDate(),
                        ev.getEventTime(),
                        ev.getLocation(),
                        ev.getApprovalStatus()
                });
                cbOrganizerEvents.addItem(new EventDropdownItem(ev.getEventId(), ev.getTitle()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 3. Refresh Tickets Table
        try {
            ticketModel.setRowCount(0);
            List<Ticket> tickets = ticketDAO.getTicketsByOrganizer(orgId);
            for (Ticket t : tickets) {
                ticketModel.addRow(new Object[]{
                        t.getTicketId(),
                        t.getEventTitle(),
                        t.getType(),
                        "$" + String.format("%.2f", t.getPrice()),
                        t.getQuantity()
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 4. Refresh Attendee Roster
        try {
            rosterModel.setRowCount(0);
            List<BookingDAO.BookingRecord> bookings = bookingDAO.getBookingsByOrganizer(orgId);
            for (BookingDAO.BookingRecord b : bookings) {
                rosterModel.addRow(new Object[]{
                        b.bookingId,
                        b.userName,
                        b.userEmail,
                        b.eventTitle,
                        b.ticketType,
                        b.quantity,
                        "$" + String.format("%.2f", b.amount != null ? b.amount : BigDecimal.ZERO),
                        b.bookingDate
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static class EventDropdownItem {
        public final int eventId;
        public final String title;

        public EventDropdownItem(int eventId, String title) {
            this.eventId = eventId;
            this.title = title;
        }

        @Override
        public String toString() {
            return eventId + ": " + title;
        }
    }
}

