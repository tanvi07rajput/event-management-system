package com.ems.model;

import java.math.BigDecimal;

public class BookingItem {
    private int bookingItemId;
    private int bookingId;
    private int ticketId;
    private String ticketType;
    private String eventTitle;
    private int quantity;
    private BigDecimal unitPrice;

    public BookingItem() {}

    public BookingItem(int bookingItemId, int bookingId, int ticketId, String ticketType,
                       String eventTitle, int quantity, BigDecimal unitPrice) {
        this.bookingItemId = bookingItemId;
        this.bookingId = bookingId;
        this.ticketId = ticketId;
        this.ticketType = ticketType;
        this.eventTitle = eventTitle;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getBookingItemId() {
        return bookingItemId;
    }

    public void setBookingItemId(int bookingItemId) {
        this.bookingItemId = bookingItemId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

