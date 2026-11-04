-- =======================================================
-- Event Management System (EMS) Database Schema
-- Version: 2.0 (Enhanced with Password Auth & Analytics)
-- =======================================================

CREATE DATABASE IF NOT EXISTS EMSDB;
USE EMSDB;

-- 1. Users Table (Supports Admin, Organizer, Attendee with Password)
CREATE TABLE IF NOT EXISTS Users (
  UserID INT AUTO_INCREMENT PRIMARY KEY,
  Name VARCHAR(100) NOT NULL,
  Email VARCHAR(150) NOT NULL UNIQUE,
  Password VARCHAR(255) NOT NULL DEFAULT 'password123',
  Role ENUM('Admin','Organizer','Attendee') NOT NULL,
  CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Events Table (Includes Category and Description)
CREATE TABLE IF NOT EXISTS Events (
  EventID INT AUTO_INCREMENT PRIMARY KEY,
  OrganizerID INT NOT NULL,
  Title VARCHAR(200) NOT NULL,
  Description TEXT,
  Category VARCHAR(50) NOT NULL DEFAULT 'General',
  EventDate DATE NOT NULL,
  EventTime TIME NOT NULL,
  Location VARCHAR(200),
  ApprovalStatus ENUM('Pending','Approved','Rejected') DEFAULT 'Pending',
  CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (OrganizerID) REFERENCES Users(UserID) ON DELETE CASCADE
);

-- 3. Tickets Table (Tiers: VIP, General, Early Bird, etc.)
CREATE TABLE IF NOT EXISTS Tickets (
  TicketID INT AUTO_INCREMENT PRIMARY KEY,
  EventID INT NOT NULL,
  Type VARCHAR(50) NOT NULL,
  Price DECIMAL(10,2) NOT NULL,
  Quantity INT NOT NULL,
  CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (EventID) REFERENCES Events(EventID) ON DELETE CASCADE
);

-- 4. Bookings Table (Stores user, total amount, status, date)
CREATE TABLE IF NOT EXISTS Bookings (
  BookingID INT AUTO_INCREMENT PRIMARY KEY,
  UserID INT NOT NULL,
  BookingDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  TotalAmount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  Status ENUM('Pending','Paid','Failed','Cancelled') DEFAULT 'Paid',
  FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE
);

-- 5. BookingItems Table (Details per ticket type purchased)
CREATE TABLE IF NOT EXISTS BookingItems (
  BookingItemID INT AUTO_INCREMENT PRIMARY KEY,
  BookingID INT NOT NULL,
  TicketID INT NOT NULL,
  Quantity INT NOT NULL,
  UnitPrice DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  FOREIGN KEY (BookingID) REFERENCES Bookings(BookingID) ON DELETE CASCADE,
  FOREIGN KEY (TicketID) REFERENCES Tickets(TicketID) ON DELETE CASCADE
);

-- =======================================================
-- Sample Seed Data for Testing & Course Demonstration
-- Passwords:
-- Admin: admin123
-- Organizer: organizer123
-- Guest: guest123
-- =======================================================

INSERT IGNORE INTO Users (Name, Email, Password, Role) VALUES
('Admin User', 'admin@ems.com', 'admin123', 'Admin'),
('Sarah Connor (Tech Lead)', 'organizer@ems.com', 'organizer123', 'Organizer'),
('David Miller (Music Prod)', 'david@ems.com', 'organizer123', 'Organizer'),
('John Doe', 'guest@ems.com', 'guest123', 'Attendee'),
('Emma Watson', 'emma@ems.com', 'guest123', 'Attendee');

-- Sample Events
INSERT IGNORE INTO Events (EventID, OrganizerID, Title, Description, Category, EventDate, EventTime, Location, ApprovalStatus) VALUES
(1, 2, 'Global AI & Cloud Summit 2026', 'Annual conference exploring cutting-edge AI breakthroughs, LLMs, and cloud infrastructure.', 'Technology', '2026-11-20', '09:30:00', 'Convention Center, Hall A', 'Approved'),
(2, 2, 'Full-Stack Java Masterclass', 'Hands-on intensive workshop building production-grade enterprise software with modern Java.', 'Workshop', '2026-11-25', '14:00:00', 'Tech Park Innovation Lab', 'Approved'),
(3, 3, 'Symphony Under the Stars', 'An enchanting evening of classical and contemporary orchestra under the open sky.', 'Concert', '2026-12-05', '19:00:00', 'Grand Amphitheater', 'Approved'),
(4, 3, 'Startup Pitch Night & Networking', 'Pitch your startup idea to top angel investors and venture capitalists.', 'Networking', '2026-12-12', '18:00:00', 'Skyline Co-working Lounge', 'Pending');

-- Sample Tickets
INSERT IGNORE INTO Tickets (TicketID, EventID, Type, Price, Quantity) VALUES
(1, 1, 'General Admission', 49.99, 150),
(2, 1, 'VIP Pass (Includes Lunch & Front Row)', 129.99, 40),
(3, 2, 'Standard Attendee', 29.00, 50),
(4, 2, 'Student Discount', 15.00, 30),
(5, 3, 'Tier 1 Seating', 75.00, 80),
(6, 3, 'Balcony View', 35.00, 100);

-- Sample Booking for demo
INSERT IGNORE INTO Bookings (BookingID, UserID, BookingDate, TotalAmount, Status) VALUES
(1001, 4, '2026-11-01 10:15:00', 49.99, 'Paid');

INSERT IGNORE INTO BookingItems (BookingItemID, BookingID, TicketID, Quantity, UnitPrice) VALUES
(1, 1001, 1, 1, 49.99);
