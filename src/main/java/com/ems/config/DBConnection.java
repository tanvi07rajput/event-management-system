package com.ems.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static String dbUrl = "jdbc:mysql://localhost:3306/EMSDB?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String dbUser = "root";
    private static String dbPassword = "Milo-098";

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        // 1. First, check properties file
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                if (props.containsKey("db.url")) dbUrl = props.getProperty("db.url").trim();
                if (props.containsKey("db.user")) dbUser = props.getProperty("db.user").trim();
                if (props.containsKey("db.password")) dbPassword = props.getProperty("db.password").trim();
            }
        } catch (Exception e) {
            System.err.println("Could not load db.properties, using defaults: " + e.getMessage());
        }

        // 2. Override with Cloud Environment Variables (Railway, Render, Heroku)
        String envDbUrl = System.getenv("MYSQL_URL");
        if (envDbUrl == null) envDbUrl = System.getenv("DATABASE_URL");
        if (envDbUrl == null) envDbUrl = System.getenv("DB_URL");

        if (envDbUrl != null && !envDbUrl.isBlank()) {
            if (!envDbUrl.startsWith("jdbc:mysql://") && envDbUrl.startsWith("mysql://")) {
                envDbUrl = "jdbc:" + envDbUrl;
            }
            dbUrl = envDbUrl;
        }

        String envUser = System.getenv("MYSQLUSER");
        if (envUser == null) envUser = System.getenv("DB_USER");
        if (envUser != null && !envUser.isBlank()) dbUser = envUser;

        String envPass = System.getenv("MYSQLPASSWORD");
        if (envPass == null) envPass = System.getenv("DB_PASSWORD");
        if (envPass != null && !envPass.isBlank()) dbPassword = envPass;

        // Individual host/port/database variables
        String envHost = System.getenv("MYSQLHOST");
        String envPort = System.getenv("MYSQLPORT");
        String envDb = System.getenv("MYSQLDATABASE");
        if (envHost != null && envPort != null && envDb != null) {
            dbUrl = "jdbc:mysql://" + envHost + ":" + envPort + "/" + envDb + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        }

        // Test driver registration
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ignored) {
                System.err.println("MySQL JDBC Driver not detected in classpath.");
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    public static String getDbUser() {
        return dbUser;
    }

    public static void setCredentials(String url, String user, String pass) {
        dbUrl = url;
        dbUser = user;
        dbPassword = pass;
    }

    public static boolean testConnection(StringBuilder outMessage) {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                if (outMessage != null) outMessage.append("Connected to MySQL successfully!");
                return true;
            }
        } catch (SQLException e) {
            if (outMessage != null) {
                outMessage.append(e.getMessage());
            }
        }
        return false;
    }

    public static void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {}
        }
    }
}
