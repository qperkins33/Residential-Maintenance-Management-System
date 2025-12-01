package com.maintenance.dao;

import com.maintenance.database.DatabaseManager;
import com.maintenance.models.*;

import java.sql.*;
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
        return switch (userType) {
            case "TENANT" -> loadTenant(userId);
            case "MANAGER" -> loadManager(userId);
            case "STAFF" -> loadStaff(userId);
            case "ADMIN" -> loadAdmin(userId);
            default -> null;
        };
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

    private Admin loadAdmin(String userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Admin admin = new Admin();
                populateUserFields(admin, rs);
                return admin;
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

    public List<MaintenanceStaff> getAllActiveStaff() {
        List<MaintenanceStaff> staffList = new ArrayList<>();

        String sql = "SELECT u.*, s.* " +
                "FROM users u " +
                "JOIN maintenance_staff s ON u.user_id = s.user_id " +
                "WHERE u.is_active = TRUE";

        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                MaintenanceStaff staff = new MaintenanceStaff();
                populateUserFields(staff, rs);  // fills common user fields

                staff.setStaffId(rs.getString("staff_id"));
                staff.setCurrentWorkload(rs.getInt("current_workload"));
                staff.setMaxCapacity(rs.getInt("max_capacity"));
                staff.setAvailable(rs.getBoolean("is_available"));

                staffList.add(staff);
            }
        } catch (SQLException e) {
            System.err.println("Error loading active staff: " + e.getMessage());
        }

        return staffList;
    }

    public Tenant getTenantById(String userId) {
        try {
            return loadTenant(userId);
        } catch (SQLException e) {
            System.err.println("Error loading tenant by id: " + e.getMessage());
            return null;
        }
    }

    public MaintenanceStaff getStaffByStaffId(String staffId) {
        String sql = "SELECT u.*, s.* FROM users u " +
                "JOIN maintenance_staff s ON u.user_id = s.user_id WHERE s.staff_id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, staffId);
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
        } catch (SQLException e) {
            System.err.println("Error loading staff by staff_id: " + e.getMessage());
        }
        return null;
    }

}
