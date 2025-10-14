package com.maintenance.database;

import java.sql.*;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private final String connectionString = "jdbc:h2:./data/maintenance_db";
    private boolean isConnected;

    private DatabaseManager() {
        connect();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public boolean connect() {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(connectionString, "sa", "");
            isConnected = true;
            System.out.println("✓ Database connected successfully");
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            isConnected = false;
            return false;
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                isConnected = false;
                System.out.println("✓ Database disconnected");
            }
        } catch (SQLException e) {
            System.err.println("Error disconnecting database: " + e.getMessage());
        }
    }

    public boolean saveRequest(com.maintenance.models.MaintenanceRequest request) {
        String sql = "INSERT INTO maintenance_requests (request_id, tenant_id, apartment_number, " +
                "description, category, priority, status, submission_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, request.getRequestId());
            pstmt.setString(2, request.getTenantId());
            pstmt.setString(3, request.getApartmentNumber());
            pstmt.setString(4, request.getDescription());
            pstmt.setString(5, request.getCategory().name());
            pstmt.setString(6, request.getPriority().name());
            pstmt.setString(7, request.getStatus().name());
            pstmt.setTimestamp(8, Timestamp.valueOf(request.getSubmissionDate()));
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving request: " + e.getMessage());
            return false;
        }
    }

    public boolean updateRequest(com.maintenance.models.MaintenanceRequest request) {
        String sql = "UPDATE maintenance_requests SET description = ?, category = ?, priority = ?, " +
                "status = ?, last_updated = ?, assigned_staff_id = ?, scheduled_date = ?, completion_date = ?, " +
                "resolution_notes = ? WHERE request_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, request.getDescription());
            pstmt.setString(2, request.getCategory().name());
            pstmt.setString(3, request.getPriority().name());
            pstmt.setString(4, request.getStatus().name());
            pstmt.setTimestamp(5, Timestamp.valueOf(request.getLastUpdated()));
            pstmt.setString(6, request.getAssignedStaffId());
            pstmt.setTimestamp(7, request.getScheduledDate() != null ?
                    Timestamp.valueOf(request.getScheduledDate()) : null);
            pstmt.setTimestamp(8, request.getCompletionDate() != null ?
                    Timestamp.valueOf(request.getCompletionDate()) : null);
            pstmt.setString(9, request.getResolutionNotes());
            pstmt.setString(10, request.getRequestId());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating request: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteRequest(String requestId) {
        String sql = "DELETE FROM maintenance_requests WHERE request_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, requestId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting request: " + e.getMessage());
            return false;
        }
    }

    public ResultSet executeQuery(String query) {
        try {
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            return null;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
