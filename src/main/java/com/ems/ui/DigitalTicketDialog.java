package com.ems.ui;

import com.ems.model.Booking;
import com.ems.model.BookingItem;
import com.ems.util.TicketPrinter;
import com.ems.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DigitalTicketDialog extends JDialog {

    public DigitalTicketDialog(Window owner, Booking booking, String eventTitle, String eventDate, String eventLocation) {
        super(owner, "Digital Admission Pass - EMS", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        BookingItem item = (booking.getItems() != null && !booking.getItems().isEmpty())
                ? booking.getItems().get(0)
                : null;

        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBackground(UITheme.BACKGROUND);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title banner
        JLabel lblHeader = new JLabel("🎟️ Your Official Digital Pass", SwingConstants.CENTER);
        lblHeader.setFont(UITheme.FONT_SUBHEADER);
        lblHeader.setForeground(UITheme.TEXT_MAIN);
        content.add(lblHeader, BorderLayout.NORTH);

        // Ticket card
        TicketPrinter.TicketCardPanel ticketCard = new TicketPrinter.TicketCardPanel(
                booking, item, eventTitle, eventDate, eventLocation
        );
        content.add(ticketCard, BorderLayout.CENTER);

        // Action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actions.setOpaque(false);

        JButton btnPrint = UITheme.createPrimaryButton("🖨️ Print Ticket");
        btnPrint.addActionListener(e -> TicketPrinter.printTicket(this, ticketCard));

        JButton btnSave = UITheme.createSecondaryButton("💾 Save as Image (PNG)");
        btnSave.addActionListener(e -> TicketPrinter.exportAsImage(this, ticketCard));

        JButton btnClose = UITheme.createSecondaryButton("Close");
        btnClose.addActionListener(e -> dispose());

        actions.add(btnPrint);
        actions.add(btnSave);
        actions.add(btnClose);
        content.add(actions, BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setLocationRelativeTo(owner);
    }
}

