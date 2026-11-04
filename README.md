# 🎟️ Event Management & Ticketing System (EMS Web v2.0)

A modern, responsive **Web Application** for event registration, multi-tier ticketing, attendee management, and real-time analytics built with **Java (Embedded Web Server & REST API)**, **MySQL**, and **Tailwind CSS**.

---

## 🌟 Key Web Application Features

- 🌐 **Responsive Web Interface**: Modern, mobile-ready UI built with **Tailwind CSS** and **FontAwesome**, rendering in Chrome, Safari, Firefox, and Edge.
- 🔍 **Live Event Catalog & Search**: Multi-keyword instant search across titles, locations, and descriptions, plus genre filters (Technology, Workshops, Concerts, Conferences, Networking).
- 🎟️ **Interactive Ticket Checkout**: Dynamic ticket tier selection (VIP, General Admission) with real-time subtotal calculation (`$Price × Qty = $Total`).
- 📱 **Digital Boarding Pass with QR Code**: Generates official admission passes with simulated 2D QR matrix, booking references (`#EMS-XXXX`), and 1-click browser printing (`Ctrl+P` / `Cmd+P`).
- ⚡ **ACID Transaction-Safe Bookings**: Strict database row-locking (`SELECT ... FOR UPDATE`), atomic inventory decrement, and automatic rollback on failure to prevent overselling.
- 📊 **Executive Admin Control Center**: Real-time KPI metric cards for Gross Revenue ($), Total Bookings, and Active/Pending Events, plus 1-click event approval and organizer onboarding.
- 🧑‍💻 **Organizer Studio**: Event creation wizard with schedules and venues, ticket tier management, and attendee sales roster.
- 🔐 **Secure Role-Based Authentication**: SHA-256 password hashing with 1-click demo credential chips for fast evaluation.

---

## 👥 User Roles & Portals

| Role | Web Portal & Capabilities |
| :--- | :--- |
| 👨‍💼 **Admin** | **Executive Center**: Track platform revenue, review pending event submissions, approve/reject events, register organizers, and audit all transactions. |
| 🧑‍💻 **Organizer** | **Organizer Studio**: Publish events, set multi-tier ticket prices/quantities, monitor revenue, and view attendee sales rosters. |
| 🎟️ **Attendee / Guest** | **Catalog & Checkout**: Explore approved events, book passes with live price calculation, and look up tickets by Booking ID. |

---

## 📁 Project Structure

```text
event-management-system/
│── database/
│   └── EMSDB.sql                         # MySQL database schema & sample seed data
│── src/
│   └── main/
│       ├── java/com/ems/
│       │   ├── Main.java                 # Web server entry point
│       │   ├── web/
│       │   │   ├── WebServer.java        # Embedded HTTP Web Server & REST API router
│       │   │   └── JsonUtil.java         # Lightweight JSON parser & serializer
│       │   ├── config/
│       │   │   └── DBConnection.java     # Database connection & ACID transaction helper
│       │   ├── model/                    # Domain POJOs (User, Event, Ticket, Booking, etc.)
│       │   ├── dao/                      # Data Access Objects (UserDAO, EventDAO, TicketDAO, BookingDAO)
│       │   └── util/                     # Security (PasswordUtil) & Themes
│       └── resources/
│           ├── db.properties             # Database credentials
│           └── web/
│               └── index.html            # Responsive Tailwind CSS Web Application
│── run.sh                                # One-click startup script (starts server & opens browser)
│── Run-EMS.command                       # macOS double-clickable launcher
│── pom.xml                               # Maven build configuration
└── README.md
```

---

## 🗄️ Database Setup

1. Make sure your **MySQL Server** is running.
2. In **MySQL Workbench** or Terminal, execute [database/EMSDB.sql](database/EMSDB.sql):
   ```bash
   mysql -u root -p < database/EMSDB.sql
   ```
3. Verify your database password in [`src/main/resources/db.properties`](src/main/resources/db.properties):
   ```properties
   db.host=localhost
   db.port=3306
   db.name=EMSDB
   db.user=root
   db.password=Milo-098
   ```

---

## 🔑 Default Demo Accounts

| Role | Email | Password | Access Level |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin@ems.com` | `admin123` | Executive Control Center |
| **Organizer** | `organizer@ems.com` | `organizer123` | Organizer Studio |
| **Organizer 2** | `david@ems.com` | `organizer123` | Organizer Studio |
| **Attendee** | `guest@ems.com` | `guest123` | Events Catalog & Checkout |

*Tip: In the web login modal, you can simply click any of the 1-click demo chips to fill in the credentials instantly!*

---

## ▶️ How to Run the Web Application

### Method 1: Terminal (Mac / Linux)
```bash
./run.sh
```
*The script compiles the project, starts the embedded HTTP server on port 8080, and automatically opens `http://localhost:8080` in your default browser.*

### Method 2: Double-Click on Mac (No Terminal Required)
Double-click [`Run-EMS.command`](Run-EMS.command) in **Finder**.

### Method 3: Open in Browser Manually
If the server is running, navigate to:  
👉 **`http://localhost:8080`**

---

## 📤 How to Submit on Google Drive

1. Right-click the `event-management-system` folder in Finder.
2. Select **Compress "event-management-system"** to create a `.zip` archive.
3. Upload the `.zip` file directly to your Google Drive or university submission link!

When the evaluator unzips your folder, all they need to do is run `./run.sh` (or double-click `Run-EMS.command`), and the web application will open on their screen!

---

## 👨‍💻 Author & Academic Project

- **Author**: Fares Aldeeb
- **Department**: AI Student @ UPM (University of Prince Mugrin)
- **Course**: CS351 - Database & Object-Oriented Software Systems
