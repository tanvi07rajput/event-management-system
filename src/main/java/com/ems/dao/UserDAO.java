package com.ems.dao;

import com.ems.config.DBConnection;
import com.ems.model.User;
import com.ems.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User authenticate(String email, String password) throws SQLException {
        String sql = "SELECT UserID, Name, Email, Password, Role FROM Users WHERE LOWER(Email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPass = rs.getString("Password");
                    // Verify password (plain or SHA-256)
                    if (PasswordUtil.verify(password, storedPass)) {
                        return new User(
                                rs.getInt("UserID"),
                                rs.getString("Name"),
                                rs.getString("Email"),
                                storedPass,
                                rs.getString("Role")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            // Graceful demo fallback if database is offline (e.g. initial cloud deploy)
            if ("admin@ems.com".equalsIgnoreCase(email) && "admin123".equals(password)) {
                return new User(1, "Admin User", "admin@ems.com", "admin123", "Admin");
            } else if ("organizer@ems.com".equalsIgnoreCase(email) && "organizer123".equals(password)) {
                return new User(2, "Sarah Connor", "organizer@ems.com", "organizer123", "Organizer");
            } else if ("guest@ems.com".equalsIgnoreCase(email) && "guest123".equals(password)) {
                return new User(4, "John Doe", "guest@ems.com", "guest123", "Attendee");
            }
            throw e;
        }
        return null;
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT UserID, Name, Email, Password, Role FROM Users WHERE LOWER(Email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("UserID"),
                            rs.getString("Name"),
                            rs.getString("Email"),
                            rs.getString("Password"),
                            rs.getString("Role")
                    );
                }
            }
        }
        return null;
    }

    public User findById(int userId) throws SQLException {
        String sql = "SELECT UserID, Name, Email, Password, Role FROM Users WHERE UserID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("UserID"),
                            rs.getString("Name"),
                            rs.getString("Email"),
                            rs.getString("Password"),
                            rs.getString("Role")
                    );
                }
            }
        }
        return null;
    }

    public int getOrCreateAttendee(Connection conn, String name, String email) throws SQLException {
        String checkSql = "SELECT UserID FROM Users WHERE LOWER(Email) = LOWER(?)";
        try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
            psCheck.setString(1, email.trim());
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("UserID");
                }
            }
        }

        String insertSql = "INSERT INTO Users (Name, Email, Password, Role) VALUES (?, ?, ?, 'Attendee')";
        try (PreparedStatement psIns = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            psIns.setString(1, name.trim().isEmpty() ? "Guest User" : name.trim());
            psIns.setString(2, email.trim());
            psIns.setString(3, PasswordUtil.hashPassword("guest123"));
            psIns.executeUpdate();
            try (ResultSet rsKey = psIns.getGeneratedKeys()) {
                if (rsKey.next()) {
                    return rsKey.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create attendee account.");
    }

    public boolean addOrganizer(String name, String email, String password) throws SQLException {
        String sql = "INSERT INTO Users (Name, Email, Password, Role) VALUES (?, ?, ?, 'Organizer')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, PasswordUtil.hashPassword(password));
            return ps.executeUpdate() > 0;
        }
    }

    public List<User> findAllOrganizers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT UserID, Name, Email, Password, Role FROM Users WHERE Role='Organizer' ORDER BY UserID DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new User(
                        rs.getInt("UserID"),
                        rs.getString("Name"),
                        rs.getString("Email"),
                        rs.getString("Password"),
                        rs.getString("Role")
                ));
            }
        }
        return list;
    }

    public int countByRole(String role) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE Role = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }
}

