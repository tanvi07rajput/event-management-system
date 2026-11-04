package com.ems.model;

import java.math.BigDecimal;

public class Ticket {
    private int ticketId;
    private int eventId;
    private String eventTitle;
    private String type;
    private BigDecimal price;
    private int quantity;

    public Ticket() {}

    public Ticket(int ticketId, int eventId, String type, BigDecimal price, int quantity) {
        this.ticketId = ticketId;
        this.eventId = eventId;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return type + " - $" + price + " (" + quantity + " available)";
    }
}

