package com.ems.model;

import java.math.BigDecimal;

public class DashboardStats {
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private int totalBookings = 0;
    private int totalEvents = 0;
    private int approvedEvents = 0;
    private int pendingEvents = 0;
    private int totalOrganizers = 0;
    private int totalAttendees = 0;
    private int ticketsSold = 0;

    public DashboardStats() {}

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public int getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
    }

    public int getApprovedEvents() {
        return approvedEvents;
    }

    public void setApprovedEvents(int approvedEvents) {
        this.approvedEvents = approvedEvents;
    }

    public int getPendingEvents() {
        return pendingEvents;
    }

    public void setPendingEvents(int pendingEvents) {
        this.pendingEvents = pendingEvents;
    }

    public int getTotalOrganizers() {
        return totalOrganizers;
    }

    public void setTotalOrganizers(int totalOrganizers) {
        this.totalOrganizers = totalOrganizers;
    }

    public int getTotalAttendees() {
        return totalAttendees;
    }

    public void setTotalAttendees(int totalAttendees) {
        this.totalAttendees = totalAttendees;
    }

    public int getTicketsSold() {
        return ticketsSold;
    }

    public void setTicketsSold(int ticketsSold) {
        this.ticketsSold = ticketsSold;
    }
}

