package com.ems.model;

import java.sql.Date;
import java.sql.Time;

public class Event {
    private int eventId;
    private int organizerId;
    private String organizerName;
    private String title;
    private String description;
    private String category;
    private Date eventDate;
    private Time eventTime;
    private String location;
    private String approvalStatus; // Pending, Approved, Rejected

    public Event() {}

    public Event(int eventId, int organizerId, String title, String description, String category,
                 Date eventDate, Time eventTime, String location, String approvalStatus) {
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.location = location;
        this.approvalStatus = approvalStatus;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(int organizerId) {
        this.organizerId = organizerId;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public Time getEventTime() {
        return eventTime;
    }

    public void setEventTime(Time eventTime) {
        this.eventTime = eventTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    @Override
    public String toString() {
        return eventId + ": " + title + " (" + (eventDate != null ? eventDate.toString() : "TBD") + ")";
    }
}

