package com.maintenance.dao;

import com.maintenance.database.DatabaseManager;
import com.maintenance.enums.*;
import com.maintenance.models.MaintenanceRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaintenanceRequestDAO {

    public Optional<String> findTenantEmailByRequestId(String requestId) {
        String sql = """
        SELECT u.email
        FROM maintenance_requests mr
        JOIN users u ON u.user_id = mr.tenant_id
        WHERE mr.request_id = ?
    """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.ofNullable(rs.getString(1));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch tenant email for request " + requestId, e);
        }
    }

    private final DatabaseManager dbManager;

    public MaintenanceRequestDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public boolean saveRequest(MaintenanceRequest request) {
        String sql = "INSERT INTO maintenance_requests (request_id, tenant_id, apartment_number, " +
                "description, category, priority, status, submission_date, last_updated) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, request.getRequestId());
            pstmt.setString(2, request.getTenantId());
            pstmt.setString(3, request.getApartmentNumber());
            pstmt.setString(4, request.getDescription());
            pstmt.setString(5, request.getCategory().name());
            pstmt.setString(6, request.getPriority().name());
            pstmt.setString(7, request.getStatus().name());
            pstmt.setTimestamp(8, Timestamp.valueOf(request.getSubmissionDate()));
            pstmt.setTimestamp(9, Timestamp.valueOf(request.getLastUpdated()));

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving request: " + e.getMessage());
            return false;
        }
    }

    public boolean updateRequest(MaintenanceRequest request) {
        String sql = "UPDATE maintenance_requests SET description = ?, category = ?, priority = ?, " +
                "status = ?, last_updated = ?, assigned_staff_id = ?, scheduled_date = ?, completion_date = ?, " +
                "staff_update_notes = ?, resolution_notes = ? WHERE request_id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
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
            pstmt.setString(9, request.getStaffUpdateNotes());
            pstmt.setString(10, request.getResolutionNotes());
            pstmt.setString(11, request.getRequestId());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating request: " + e.getMessage());
            return false;
        }
    }

    public List<MaintenanceRequest> getAllRequests() {
        List<MaintenanceRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM maintenance_requests ORDER BY submission_date DESC";

        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                requests.add(mapResultSetToRequest(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading requests: " + e.getMessage());
        }

        return requests;
    }

    public List<MaintenanceRequest> getRequestsByTenant(String tenantId) {
        List<MaintenanceRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM maintenance_requests WHERE tenant_id = ? ORDER BY submission_date DESC";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, tenantId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                requests.add(mapResultSetToRequest(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading tenant requests: " + e.getMessage());
        }

        return requests;
    }

    public List<MaintenanceRequest> getRequestsByStaff(String staffId) {
        List<MaintenanceRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM maintenance_requests WHERE assigned_staff_id = ? ORDER BY priority DESC";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, staffId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                requests.add(mapResultSetToRequest(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading staff requests: " + e.getMessage());
        }

        return requests;
    }

    public List<MaintenanceRequest> getRequestsByStatus(RequestStatus status) {
        List<MaintenanceRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM maintenance_requests WHERE status = ? ORDER BY submission_date DESC";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                requests.add(mapResultSetToRequest(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading requests by status: " + e.getMessage());
        }

        return requests;
    }

    private MaintenanceRequest mapResultSetToRequest(ResultSet rs) throws SQLException {
        MaintenanceRequest request = new MaintenanceRequest();
        request.setRequestId(rs.getString("request_id"));
        request.setTenantId(rs.getString("tenant_id"));
        request.setApartmentNumber(rs.getString("apartment_number"));
        request.setDescription(rs.getString("description"));
        request.setDetailedDescription(rs.getString("detailed_description"));
        request.setCategory(CategoryType.valueOf(rs.getString("category")));
        request.setPriority(PriorityLevel.valueOf(rs.getString("priority")));
        request.setStatus(RequestStatus.valueOf(rs.getString("status")));
        request.setSubmissionDate(rs.getTimestamp("submission_date").toLocalDateTime());
        request.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());

        Timestamp scheduled = rs.getTimestamp("scheduled_date");
        if (scheduled != null) request.setScheduledDate(scheduled.toLocalDateTime());

        Timestamp completed = rs.getTimestamp("completion_date");
        if (completed != null) request.setCompletionDate(completed.toLocalDateTime());

        request.setEstimatedCost(rs.getDouble("estimated_cost"));
        request.setActualCost(rs.getDouble("actual_cost"));
        request.setAssignedStaffId(rs.getString("assigned_staff_id"));
        request.setWorkOrderNumber(rs.getString("work_order_number"));
        request.setStaffUpdateNotes(rs.getString("staff_update_notes"));
        request.setResolutionNotes(rs.getString("resolution_notes"));

        return request;
    }
}
