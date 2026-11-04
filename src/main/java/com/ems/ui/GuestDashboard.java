package com.ems.ui;

import com.ems.dao.BookingDAO;
import com.ems.dao.EventDAO;
import com.ems.dao.TicketDAO;
import com.ems.model.Booking;
import com.ems.model.BookingItem;
import com.ems.model.Event;
import com.ems.model.Ticket;
import com.ems.model.User;
import com.ems.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class GuestDashboard extends JFrame {

    private final User currentUser; // May be null if guest
    private final EventDAO eventDAO = new EventDAO();
    private final TicketDAO ticketDAO = new TicketDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    // Tab 1: Browse
    private DefaultTableModel browseModel;
    private JTable browseTable;
    private JTextField tfSearch;
    private JComboBox<String> cbCategoryFilter;
    private JTextArea taEventDescription;

    // Tab 2: Book
    private JComboBox<EventComboItem> cbBookingEvents;
    private JComboBox<TicketComboItem> cbBookingTickets;
    private JSpinner spQuantity;
    private JTextField tfAttendeeName, tfAttendeeEmail;
    private JLabel lblTotalCalculation;

    // Tab 3: My Bookings
    private JTextField tfSearchBookingId;
    private DefaultTableModel myBookingsModel;
    private JTable myBookingsTable;
    private Booking lastRetrievedBooking;
    private String lastEventTitle, lastEventDate, lastEventLocation;

    private JTabbedPane mainTabs;

    public GuestDashboard(User user) {
        this.currentUser = user;

        setTitle("EMS - Events Catalog & Ticketing Portal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);
        setContentPane(root);

        // Header
        String userName = currentUser != null ? currentUser.getName() : "Guest";
        String userRole = currentUser != null ? currentUser.getRole() : "Public Attendee";
        JPanel header = UITheme.createHeaderBar(
                "Events & Ticketing Portal",
                "Discover premier events, book tickets, and manage passes",
                userRole,
                userName,
                () -> {
                    dispose();
                    new LoginFrame().setVisible(true);
                }
        );
        root.add(header, BorderLayout.NORTH);

        // Main Tabs
        mainTabs = new JTabbedPane();
        mainTabs.setFont(UITheme.FONT_BOLD);
        mainTabs.addTab("🔍 Browse Events", createBrowsePanel());
        mainTabs.addTab("🎟️ Book Tickets", createBookingPanel());
        mainTabs.addTab("📄 My Bookings & Pass", createMyBookingsPanel());

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(15, 20, 15, 20));
        centerWrapper.add(mainTabs, BorderLayout.CENTER);

        root.add(centerWrapper, BorderLayout.CENTER);

        // Load initial events
        loadApprovedEvents();
    }

    // ==========================================
    // TAB 1: BROWSE EVENTS
    // ==========================================
    private JPanel createBrowsePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        tfSearch = UITheme.createSearchField("Search events by title, venue, or keyword...");
        tfSearch.setPreferredSize(new Dimension(320, 38));

        cbCategoryFilter = new JComboBox<>(new String[]{
                "All", "Technology", "Workshop", "Concert", "Conference", "Networking", "Sports", "General"
        });
        cbCategoryFilter.setPreferredSize(new Dimension(140, 38));

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        JButton btnReset = UITheme.createSecondaryButton("Reset");
        JButton btnProceedToBook = UITheme.createSuccessButton("Proceed to Book Selected ➔");

        filterBar.add(new JLabel("Category:"));
        filterBar.add(cbCategoryFilter);
        filterBar.add(tfSearch);
        filterBar.add(btnSearch);
        filterBar.add(btnReset);
        filterBar.add(btnProceedToBook);

        panel.add(filterBar, BorderLayout.NORTH);

        // Events Table + Preview Split
        browseModel = new DefaultTableModel(new String[]{
                "EventID", "Title", "Category", "Organizer", "Date", "Time", "Location"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        browseTable = new JTable(browseModel);
        UITheme.styleTable(browseTable);

        // Event Description Box
        JPanel previewCard = new JPanel(new BorderLayout(5, 5));
        previewCard.setBackground(Color.WHITE);
        previewCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        previewCard.setPreferredSize(new Dimension(0, 100));

        JLabel lblPrevTitle = new JLabel("EVENT DETAILS & DESCRIPTION");
        lblPrevTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPrevTitle.setForeground(UITheme.TEXT_MUTED);

        taEventDescription = new JTextArea("Select an event from the list above to view full details.");
        taEventDescription.setEditable(false);
        taEventDescription.setLineWrap(true);
        taEventDescription.setWrapStyleWord(true);
        taEventDescription.setFont(UITheme.FONT_REGULAR);
        taEventDescription.setBackground(Color.WHITE);

        previewCard.add(lblPrevTitle, BorderLayout.NORTH);
        previewCard.add(new JScrollPane(taEventDescription), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(browseTable), previewCard);
        splitPane.setResizeWeight(0.75);
        splitPane.setBorder(null);

        panel.add(splitPane, BorderLayout.CENTER);

        // Listeners
        browseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = browseTable.getSelectedRow();
                if (row != -1) {
                    String title = (String) browseModel.getValueAt(row, 1);
                    String cat = (String) browseModel.getValueAt(row, 2);
                    String loc = (String) browseModel.getValueAt(row, 6);
                    taEventDescription.setText(
                            "TITLE: " + title + "\n" +
                            "CATEGORY: " + cat + " | VENUE: " + loc + "\n" +
                            "Click 'Proceed to Book Selected' to select ticket tiers and complete purchase."
                    );
                }
            }
        });

        btnSearch.addActionListener(e -> loadApprovedEvents());
        btnReset.addActionListener(e -> {
            tfSearch.setText("Search events by title, venue, or keyword...");
            cbCategoryFilter.setSelectedIndex(0);
            loadApprovedEvents();
        });

        // Live search on enter
        tfSearch.addActionListener(e -> loadApprovedEvents());
        cbCategoryFilter.addActionListener(e -> loadApprovedEvents());

        btnProceedToBook.addActionListener(e -> {
            int row = browseTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select an event to book.", "Select Event", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int eventId = (int) browseModel.getValueAt(row, 0);
            mainTabs.setSelectedIndex(1); // Switch to Book tab
            selectEventInDropdown(eventId);
        });

        return panel;
    }

    private void loadApprovedEvents() {
        browseModel.setRowCount(0);
        String keyword = tfSearch.getText().trim();
        String cat = (String) cbCategoryFilter.getSelectedItem();

        try {
            List<Event> events = eventDAO.getApprovedEvents(keyword, cat);
            for (Event ev : events) {
                browseModel.addRow(new Object[]{
                        ev.getEventId(),
                        ev.getTitle(),
                        ev.getCategory(),
                        ev.getOrganizerName() != null ? ev.getOrganizerName() : "Organizer",
                        ev.getEventDate(),
                        ev.getEventTime(),
                        ev.getLocation()
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ==========================================
    // TAB 2: BOOK TICKETS
    // ==========================================
    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(24, 32, 24, 32)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 8, 8, 8);
        int row = 0;

        // Title
        JLabel lblFormTitle = new JLabel("🎟️ Purchase Event Admission Pass");
        lblFormTitle.setFont(UITheme.FONT_SUBHEADER);
        lblFormTitle.setForeground(UITheme.TEXT_MAIN);
        c.gridx = 0; c.gridy = row++; c.gridwidth = 2;
        formCard.add(lblFormTitle, c);
        c.gridwidth = 1;

        // 1. Select Event
        c.gridx = 0; c.gridy = row; formCard.add(new JLabel("1. Select Approved Event:"), c);
        cbBookingEvents = new JComboBox<>();
        c.gridx = 1; c.gridy = row++; formCard.add(cbBookingEvents, c);

        // 2. Ticket Tier
        c.gridx = 0; c.gridy = row; formCard.add(new JLabel("2. Available Ticket Tier:"), c);
        cbBookingTickets = new JComboBox<>();
        c.gridx = 1; c.gridy = row++; formCard.add(cbBookingTickets, c);

        // 3. Quantity
        c.gridx = 0; c.gridy = row; formCard.add(new JLabel("3. Ticket Quantity:"), c);
        spQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        c.gridx = 1; c.gridy = row++; formCard.add(spQuantity, c);

        // 4. Attendee Name
        c.gridx = 0; c.gridy = row; formCard.add(new JLabel("4. Attendee Name:"), c);
        tfAttendeeName = new JTextField(currentUser != null ? currentUser.getName() : "", 18);
        c.gridx = 1; c.gridy = row++; formCard.add(tfAttendeeName, c);

        // 5. Attendee Email
        c.gridx = 0; c.gridy = row; formCard.add(new JLabel("5. Confirmation Email:"), c);
        tfAttendeeEmail = new JTextField(currentUser != null ? currentUser.getEmail() : "", 18);
        c.gridx = 1; c.gridy = row++; formCard.add(tfAttendeeEmail, c);

        // Price Summary Banner
        lblTotalCalculation = new JLabel("Total Amount: $0.00", SwingConstants.CENTER);
        lblTotalCalculation.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalCalculation.setForeground(UITheme.PRIMARY);
        lblTotalCalculation.setOpaque(true);
        lblTotalCalculation.setBackground(new Color(238, 242, 255));
        lblTotalCalculation.setBorder(new EmptyBorder(10, 15, 10, 15));

        c.gridx = 0; c.gridy = row++; c.gridwidth = 2;
        formCard.add(lblTotalCalculation, c);

        // Confirm Button
        JButton btnConfirm = UITheme.createPrimaryButton("Confirm Booking & Issue Pass");
        c.gridx = 0; c.gridy = row++; c.gridwidth = 2;
        c.insets = new Insets(16, 8, 8, 8);
        formCard.add(btnConfirm, c);

        panel.add(formCard);

        // Wire Event Listeners
        cbBookingEvents.addActionListener(e -> updateTicketsDropdown());
        cbBookingTickets.addActionListener(e -> updateTotalCalculation());
        spQuantity.addChangeListener(e -> updateTotalCalculation());
        btnConfirm.addActionListener(e -> handleBookingProcess());

        loadBookingEventsDropdown();
        return panel;
    }

    private void loadBookingEventsDropdown() {
        cbBookingEvents.removeAllItems();
        try {
            List<Event> events = eventDAO.getApprovedEvents(null, null);
            for (Event ev : events) {
                cbBookingEvents.addItem(new EventComboItem(ev.getEventId(), ev.getTitle(), ev.getEventDate().toString(), ev.getLocation()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        updateTicketsDropdown();
    }

    private void selectEventInDropdown(int eventId) {
        for (int i = 0; i < cbBookingEvents.getItemCount(); i++) {
            EventComboItem item = cbBookingEvents.getItemAt(i);
            if (item.eventId == eventId) {
                cbBookingEvents.setSelectedIndex(i);
                break;
            }
        }
    }

    private void updateTicketsDropdown() {
        cbBookingTickets.removeAllItems();
        EventComboItem selectedEvent = (EventComboItem) cbBookingEvents.getSelectedItem();
        if (selectedEvent == null) {
            updateTotalCalculation();
            return;
        }

        try {
            List<Ticket> tickets = ticketDAO.getTicketsByEvent(selectedEvent.eventId);
            for (Ticket t : tickets) {
                if (t.getQuantity() > 0) {
                    cbBookingTickets.addItem(new TicketComboItem(t.getTicketId(), t.getType(), t.getPrice(), t.getQuantity()));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        updateTotalCalculation();
    }

    private void updateTotalCalculation() {
        TicketComboItem selectedTicket = (TicketComboItem) cbBookingTickets.getSelectedItem();
        int qty = (int) spQuantity.getValue();

        if (selectedTicket != null) {
            BigDecimal subtotal = selectedTicket.price.multiply(BigDecimal.valueOf(qty));
            lblTotalCalculation.setText("Total Amount: $" + String.format("%.2f", subtotal) + " (" + qty + " × $" + selectedTicket.price + ")");
        } else {
            lblTotalCalculation.setText("Total Amount: $0.00 (No tickets available)");
        }
    }

    private void handleBookingProcess() {
        EventComboItem selEvent = (EventComboItem) cbBookingEvents.getSelectedItem();
        TicketComboItem selTicket = (TicketComboItem) cbBookingTickets.getSelectedItem();
        String name = tfAttendeeName.getText().trim();
        String email = tfAttendeeEmail.getText().trim();
        int qty = (int) spQuantity.getValue();

        if (selEvent == null) {
            JOptionPane.showMessageDialog(this, "Please select an event.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selTicket == null) {
            JOptionPane.showMessageDialog(this, "No tickets are currently available for this event.", "Tickets Unavailable", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Attendee Name and Email are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (qty > selTicket.remainingQty) {
            JOptionPane.showMessageDialog(this, "Only " + selTicket.remainingQty + " tickets are available in this tier.", "Inventory Limit", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Process booking with ACID Transaction
            Booking booking = bookingDAO.processBooking(name, email, selTicket.ticketId, qty);

            // Refresh ticket dropdown inventory
            updateTicketsDropdown();

            // Store for quick pass lookup
            lastRetrievedBooking = booking;
            lastEventTitle = selEvent.title;
            lastEventDate = selEvent.date;
            lastEventLocation = selEvent.location;

            // Immediately launch digital ticket dialog!
            DigitalTicketDialog dialog = new DigitalTicketDialog(this, booking, selEvent.title, selEvent.date, selEvent.location);
            dialog.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Booking Failed: " + ex.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // TAB 3: MY BOOKINGS & PASS
    // ==========================================
    private JPanel createMyBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top Search Bar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        tfSearchBookingId = new JTextField(12);
        JButton btnFind = UITheme.createPrimaryButton("Find Booking ID");
        JButton btnViewTicket = UITheme.createSuccessButton("🎟️ View / Print Digital Ticket");

        top.add(new JLabel("Enter Booking ID (e.g. 1001):"));
        top.add(tfSearchBookingId);
        top.add(btnFind);
        top.add(btnViewTicket);
        panel.add(top, BorderLayout.NORTH);

        // Table
        myBookingsModel = new DefaultTableModel(new String[]{
                "BookingID", "Attendee Name", "Event Title", "Tier", "Qty", "Total Paid", "Date", "Status"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        myBookingsTable = new JTable(myBookingsModel);
        UITheme.styleTable(myBookingsTable);
        panel.add(new JScrollPane(myBookingsTable), BorderLayout.CENTER);

        // Actions
        btnFind.addActionListener(e -> {
            String input = tfSearchBookingId.getText().trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a Booking ID.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int bookingId = Integer.parseInt(input);
                Booking b = bookingDAO.getBookingById(bookingId);
                myBookingsModel.setRowCount(0);

                if (b != null && b.getItems() != null && !b.getItems().isEmpty()) {
                    BookingItem item = b.getItems().get(0);
                    myBookingsModel.addRow(new Object[]{
                            b.getBookingId(),
                            b.getUserName(),
                            item.getEventTitle(),
                            item.getTicketType(),
                            item.getQuantity(),
                            "$" + String.format("%.2f", b.getTotalAmount()),
                            b.getBookingDate(),
                            b.getStatus()
                    });

                    lastRetrievedBooking = b;
                    lastEventTitle = item.getEventTitle();
                    lastEventDate = b.getBookingDate() != null ? b.getBookingDate().toString() : "TBD";
                    lastEventLocation = "Main Venue";
                } else {
                    JOptionPane.showMessageDialog(this, "No booking found with ID: " + bookingId, "Not Found", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Booking ID must be a numeric value.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnViewTicket.addActionListener(e -> {
            if (lastRetrievedBooking == null) {
                JOptionPane.showMessageDialog(this, "Please search and load a valid booking first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            DigitalTicketDialog dialog = new DigitalTicketDialog(
                    this, lastRetrievedBooking, lastEventTitle, lastEventDate, lastEventLocation
            );
            dialog.setVisible(true);
        });

        return panel;
    }

    // ==========================================
    // HELPER ITEM CLASSES FOR CLEAN DROPDOWNS
    // ==========================================
    private static class EventComboItem {
        public final int eventId;
        public final String title;
        public final String date;
        public final String location;

        public EventComboItem(int eventId, String title, String date, String location) {
            this.eventId = eventId;
            this.title = title;
            this.date = date;
            this.location = location;
        }

        @Override
        public String toString() {
            return eventId + ": " + title + " (" + date + ")";
        }
    }

    private static class TicketComboItem {
        public final int ticketId;
        public final String type;
        public final BigDecimal price;
        public final int remainingQty;

        public TicketComboItem(int ticketId, String type, BigDecimal price, int remainingQty) {
            this.ticketId = ticketId;
            this.type = type;
            this.price = price;
            this.remainingQty = remainingQty;
        }

        @Override
        public String toString() {
            return type + " - $" + price + " (" + remainingQty + " available)";
        }
    }
}

