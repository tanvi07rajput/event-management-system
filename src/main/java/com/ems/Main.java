package com.ems;

import com.ems.config.DBConnection;
import com.ems.web.WebServer;

public class Main {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("🎟️ Event Management System (EMS Web v2.0)");
        System.out.println("=================================================");

        StringBuilder status = new StringBuilder();
        if (DBConnection.testConnection(status)) {
            System.out.println("✓ Database Connection: OK (" + DBConnection.getDbUrl() + ")");
        } else {
            System.err.println("⚠️ Database Connection Warning: " + status);
            System.err.println("Please ensure MySQL is running and verify src/main/resources/db.properties");
        }

        // Launch the Web Application Server (Embedded, opens browser automatically)
        WebServer.main(args);
    }
}
