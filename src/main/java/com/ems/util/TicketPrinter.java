package com.ems.util;

import com.ems.model.Booking;
import com.ems.model.BookingItem;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TicketPrinter {

    /**
     * Renders an interactive, printable boarding pass / ticket component.
     */
    public static class TicketCardPanel extends JPanel implements Printable {
        private final Booking booking;
        private final BookingItem item;
        private final String eventTitle;
        private final String eventDate;
        private final String eventLocation;

        public TicketCardPanel(Booking booking, BookingItem item, String eventTitle, String eventDate, String eventLocation) {
            this.booking = booking;
            this.item = item;
            this.eventTitle = eventTitle != null ? eventTitle : "Event Ticket";
            this.eventDate = eventDate != null ? eventDate : "TBD";
            this.eventLocation = eventLocation != null ? eventLocation : "Main Venue";

            setPreferredSize(new Dimension(540, 290));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Card background
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(8, 8, w - 16, h - 16, 18, 18);

            // Left header strip (gradient)
            GradientPaint gp = new GradientPaint(8, 8, new Color(37, 99, 235), 8, 70, new Color(14, 165, 233));
            g2.setPaint(gp);
            g2.fillRoundRect(8, 8, w - 16, 60, 18, 18);
            g2.fillRect(8, 50, w - 16, 18); // Square off bottom corners of header

            // Header text
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2.drawString("🎟️ " + truncate(eventTitle, 32), 24, 38);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.drawString("OFFICIAL ADMISSION TICKET • EMS SECURE PASS", 26, 56);

            // Main body fields
            int leftX = 24;
            int rightX = 380;
            int startY = 95;

            // Attendee Name
            drawField(g2, "ATTENDEE", booking.getUserName() != null ? booking.getUserName() : "Guest Attendee", leftX, startY);
            drawField(g2, "BOOKING REF", "#EMS-" + booking.getBookingId(), leftX + 160, startY);

            // Date & Time
            drawField(g2, "DATE & TIME", eventDate, leftX, startY + 45);
            drawField(g2, "STATUS", booking.getStatus(), leftX + 160, startY + 45);

            // Location
            drawField(g2, "LOCATION", truncate(eventLocation, 38), leftX, startY + 90);

            // Ticket Tier & Quantity
            String tierInfo = (item != null ? item.getTicketType() : "General") + " (Qty: " + (item != null ? item.getQuantity() : 1) + ")";
            drawField(g2, "TICKET TIER", tierInfo, leftX, startY + 135);

            String priceStr = "$" + (booking.getTotalAmount() != null ? booking.getTotalAmount().toString() : "0.00");
            drawField(g2, "TOTAL PAID", priceStr, leftX + 160, startY + 135);

            // Dashed tear-off line
            float[] dash = {6.0f, 4.0f};
            Stroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
            g2.setColor(new Color(203, 213, 225));
            g2.setStroke(dashed);
            g2.drawLine(355, 75, 355, h - 20);

            // Cutout notches on tear-off line
            g2.setColor(new Color(248, 250, 252));
            g2.fillOval(345, -5, 20, 20);
            g2.fillOval(345, h - 15, 20, 20);

            // Draw QR Code Representation on Right side
            drawSimulatedQRCode(g2, rightX - 10, startY - 15, 120, booking.getBookingId());

            g2.setColor(new Color(100, 116, 139));
            g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2.drawString("SCAN AT ENTRANCE", rightX - 5, startY + 125);
            g2.drawString("ID: " + booking.getBookingId() + "-VAL", rightX + 5, startY + 140);

            // Card outer border
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(new Color(226, 232, 240));
            g2.drawRoundRect(8, 8, w - 16, h - 16, 18, 18);

            g2.dispose();
        }

        private void drawField(Graphics2D g2, String label, String value, int x, int y) {
            g2.setColor(new Color(100, 116, 139));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.drawString(label, x, y);

            g2.setColor(new Color(15, 23, 42));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString(value != null ? value : "-", x, y + 16);
        }

        private void drawSimulatedQRCode(Graphics2D g2, int x, int y, int size, int seed) {
            g2.setColor(Color.WHITE);
            g2.fillRect(x, y, size, size);
            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, size, size);

            int matrixSize = 21;
            int cellSize = size / matrixSize;

            // Draw corner finder patterns
            drawFinderPattern(g2, x, y, cellSize);
            drawFinderPattern(g2, x + (matrixSize - 7) * cellSize, y, cellSize);
            drawFinderPattern(g2, x, y + (matrixSize - 7) * cellSize, cellSize);

            // Pseudorandom internal bits seeded by booking ID
            long val = ((long) seed * 2654435761L) ^ 0x5DEECE66DL;
            for (int r = 0; r < matrixSize; r++) {
                for (int c = 0; c < matrixSize; c++) {
                    // skip finder patterns
                    if ((r < 8 && c < 8) || (r < 8 && c >= matrixSize - 8) || (r >= matrixSize - 8 && c < 8)) {
                        continue;
                    }
                    val = (val * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
                    if ((val >>> 40) % 2 == 0) {
                        g2.fillRect(x + c * cellSize, y + r * cellSize, cellSize, cellSize);
                    }
                }
            }
        }

        private void drawFinderPattern(Graphics2D g2, int startX, int startY, int cellSize) {
            g2.setColor(Color.BLACK);
            g2.fillRect(startX, startY, 7 * cellSize, 7 * cellSize);
            g2.setColor(Color.WHITE);
            g2.fillRect(startX + cellSize, startY + cellSize, 5 * cellSize, 5 * cellSize);
            g2.setColor(Color.BLACK);
            g2.fillRect(startX + 2 * cellSize, startY + 2 * cellSize, 3 * cellSize, 3 * cellSize);
        }

        private String truncate(String text, int max) {
            if (text == null) return "";
            return text.length() > max ? text.substring(0, max - 3) + "..." : text;
        }

        @Override
        public int print(Graphics graphics, java.awt.print.PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) return NO_SUCH_PAGE;
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            printAll(graphics);
            return PAGE_EXISTS;
        }
    }

    public static void printTicket(Component parent, TicketCardPanel panel) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(panel);
        boolean ok = job.printDialog();
        if (ok) {
            try {
                job.print();
                JOptionPane.showMessageDialog(parent, "Ticket sent to printer successfully!");
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(parent, "Print failed: " + ex.getMessage(), "Printing Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void exportAsImage(Component parent, TicketCardPanel panel) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Ticket-EMS-" + System.currentTimeMillis() + ".png"));
        int ret = chooser.showSaveDialog(parent);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".png")) {
                target = new File(target.getAbsolutePath() + ".png");
            }
            try {
                BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = img.createGraphics();
                panel.paint(g2);
                g2.dispose();
                ImageIO.write(img, "png", target);
                JOptionPane.showMessageDialog(parent, "Ticket saved to:\n" + target.getAbsolutePath(), "Ticket Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, "Failed to export ticket: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

