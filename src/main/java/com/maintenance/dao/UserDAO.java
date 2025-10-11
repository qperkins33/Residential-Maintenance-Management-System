package com.maintenance.dao;

import com.maintenance.database.DatabaseManager;
import com.maintenance.models.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final DatabaseManager dbManager;

    public UserDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = true";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String userType = rs.getString("user_type");
                String userId = rs.getString("user_id");

                // Update last login
                updateLastLogin(userId);

                return loadUserByType(userId, userType);
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }

        return null;
    }

    private User loadUserByType(String userId, String userType) throws SQLException {
        switch (userType) {
            case "TENANT":
                return loadTenant(userId);
            case "MANAGER":
                return loadManager(userId);
            case "STAFF":
                return loadStaff(userId);
            default:
                return null;
        }
    }

    private Tenant loadTenant(String userId) throws SQLException {
        String sql = "SELECT u.*, t.* FROM users u " +
                "JOIN tenants t ON u.user_id = t.user_id WHERE u.user_id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Tenant tenant = new Tenant();
                populateUserFields(tenant, rs);
                tenant.setApartmentNumber(rs.getString("apartment_number"));
                return tenant;
            }
        }
        return null;
    }

    private BuildingManager loadManager(String userId) throws SQLException {
        String sql = "SELECT u.*, m.* FROM users u " +
                "JOIN building_managers m ON u.user_id = m.user_id WHERE u.user_id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                BuildingManager manager = new BuildingManager();
                populateUserFields(manager, rs);
                manager.setEmployeeId(rs.getString("employee_id"));
                manager.setDepartment(rs.getString("department"));
                return manager;
            }
        }
        return null;
    }

    private MaintenanceStaff loadStaff(String userId) throws SQLException {
        String sql = "SELECT u.*, s.* FROM users u " +
                "JOIN maintenance_staff s ON u.user_id = s.user_id WHERE u.user_id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                MaintenanceStaff staff = new MaintenanceStaff();
                populateUserFields(staff, rs);
                staff.setStaffId(rs.getString("staff_id"));
                staff.setCurrentWorkload(rs.getInt("current_workload"));
                staff.setMaxCapacity(rs.getInt("max_capacity"));
                staff.setAvailable(rs.getBoolean("is_available"));
                return staff;
            }
        }
        return null;
    }

    private void populateUserFields(User user, ResultSet rs) throws SQLException {
        user.setUserId(rs.getString("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setActive(rs.getBoolean("is_active"));
    }

    private void updateLastLogin(String userId) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
        }
    }

    public boolean isUsernameTaken(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
        }
        return false;
    }

    public boolean registerUser(UserRegistrationData data) {
        Connection conn = dbManager.getConnection();
        if (conn == null) {
            return false;
        }

        try {
            conn.setAutoCommit(false);

            String userId = generateNextUserId(conn, data.getUserType());
            if (userId == null) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement userStmt = conn.prepareStatement(
                    "INSERT INTO users (user_id, username, password, first_name, last_name, email, phone_number, " +
                            "user_type, date_created, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

                userStmt.setString(1, userId);
                userStmt.setString(2, data.getUsername());
                userStmt.setString(3, data.getPassword());
                userStmt.setString(4, data.getFirstName());
                userStmt.setString(5, data.getLastName());
                userStmt.setString(6, data.getEmail());
                userStmt.setString(7, data.getPhoneNumber());
                userStmt.setString(8, data.getUserType());
                userStmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
                userStmt.setBoolean(10, true);

                userStmt.executeUpdate();
            }

            switch (data.getUserType()) {
                case "TENANT":
                    registerTenant(conn, userId, data);
                    break;
                case "MANAGER":
                    registerManager(conn, userId, data);
                    break;
                case "STAFF":
                    registerStaff(conn, userId, data);
                    break;
                default:
                    throw new SQLException("Unsupported user type: " + data.getUserType());
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
        return false;
    }

    private void registerTenant(Connection conn, String userId, UserRegistrationData data) throws SQLException {
        try (PreparedStatement tenantStmt = conn.prepareStatement(
                "INSERT INTO tenants (user_id, apartment_number, lease_start_date, lease_end_date, emergency_contact, emergency_phone) " +
                        "VALUES (?, ?, ?, ?, ?, ?)")) {
            tenantStmt.setString(1, userId);
            tenantStmt.setString(2, data.getApartmentNumber());

            LocalDate leaseStart = data.getLeaseStartDate();
            LocalDate leaseEnd = data.getLeaseEndDate();
            if (leaseStart != null) {
                tenantStmt.setDate(3, Date.valueOf(leaseStart));
            } else {
                tenantStmt.setNull(3, Types.DATE);
            }

            if (leaseEnd != null) {
                tenantStmt.setDate(4, Date.valueOf(leaseEnd));
            } else {
                tenantStmt.setNull(4, Types.DATE);
            }

            tenantStmt.setString(5, data.getEmergencyContact());
            tenantStmt.setString(6, data.getEmergencyPhone());
            tenantStmt.executeUpdate();
        }
    }

    private void registerManager(Connection conn, String userId, UserRegistrationData data) throws SQLException {
        try (PreparedStatement managerStmt = conn.prepareStatement(
                "INSERT INTO building_managers (user_id, employee_id, department, access_level) VALUES (?, ?, ?, ?)")) {
            managerStmt.setString(1, userId);
            managerStmt.setString(2, data.getEmployeeId());
            managerStmt.setString(3, data.getDepartment());
            managerStmt.setString(4, "MANAGER");
            managerStmt.executeUpdate();
        }
    }

    private void registerStaff(Connection conn, String userId, UserRegistrationData data) throws SQLException {
        try (PreparedStatement staffStmt = conn.prepareStatement(
                "INSERT INTO maintenance_staff (user_id, staff_id, specializations, current_workload, max_capacity, is_available) " +
                        "VALUES (?, ?, ?, ?, ?, ?)")) {
            staffStmt.setString(1, userId);
            staffStmt.setString(2, data.getStaffId());
            staffStmt.setString(3, data.getSpecializations());
            staffStmt.setInt(4, 0);
            staffStmt.setInt(5, data.getMaxCapacity() != null ? data.getMaxCapacity() : 10);
            staffStmt.setBoolean(6, true);
            staffStmt.executeUpdate();
        }
    }

    private String generateNextUserId(Connection conn, String userType) throws SQLException {
        String prefix;
        switch (userType) {
            case "TENANT":
                prefix = "T";
                break;
            case "MANAGER":
                prefix = "M";
                break;
            case "STAFF":
                prefix = "S";
                break;
            default:
                return null;
        }

        String sql = "SELECT user_id FROM users WHERE user_id LIKE ? ORDER BY user_id DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prefix + "%");
            ResultSet rs = pstmt.executeQuery();
            int nextNumber = 1;
            if (rs.next()) {
                String lastId = rs.getString("user_id");
                String numberPart = lastId.substring(1);
                try {
                    nextNumber = Integer.parseInt(numberPart) + 1;
                } catch (NumberFormatException e) {
                    nextNumber = 1;
                }
            }
            return prefix + String.format("%03d", nextNumber);
        }
    }

    public List<MaintenanceStaff> getAllAvailableStaff() {
        List<MaintenanceStaff> staffList = new ArrayList<>();
        String sql = "SELECT u.*, s.* FROM users u " +
                "JOIN maintenance_staff s ON u.user_id = s.user_id " +
                "WHERE s.is_available = true";

        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                MaintenanceStaff staff = new MaintenanceStaff();
                populateUserFields(staff, rs);
                staff.setStaffId(rs.getString("staff_id"));
                staff.setCurrentWorkload(rs.getInt("current_workload"));
                staff.setMaxCapacity(rs.getInt("max_capacity"));
                staff.setAvailable(rs.getBoolean("is_available"));
                staffList.add(staff);
            }
        } catch (SQLException e) {
            System.err.println("Error loading staff: " + e.getMessage());
        }

        return staffList;
    }
}
