package com.maintenance.dao;

import com.maintenance.database.DatabaseManager;
import com.maintenance.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object responsible for user-related operations.
 * Handles authentication and loading of specific user-role objects from the database.
 */
public class UserDAO {
    /**
     * Shared DatabaseManager used to obtain JDBC connections.
     */
    private final DatabaseManager dbManager;

    /**
     * Default constructor that obtains the singleton DatabaseManager instance.
     */
    public UserDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Authenticate a user by username and password.
     * Only active users (is_active = true) are allowed to authenticate.
     * On successful authentication, the method:
     *  - Updates the last_login timestamp.
     *  - Loads and returns the appropriate User subtype (Tenant, Manager, Staff, Admin).
     *
     * @param username username provided at login
     * @param password password provided at login
     * @return a concrete User subtype instance if credentials are valid and user is active, otherwise null
     */
    public User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = true";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String userType = rs.getString("user_type");
                String userId = rs.getString("user_id");

                // Update last login timestamp for the authenticated user
                updateLastLogin(userId);

                // Load and return the correct user subtype (Tenant, Manager, Staff, Admin)
                return loadUserByType(userId, userType);
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }

        // Authentication failed or error occurred
        return null;
    }

    /**
     * Dispatch helper that loads and returns the correct user subtype based on user_type.
     *
     * @param userId   ID of the user
     * @param userType string indicating user role (TENANT, MANAGER, STAFF, ADMIN)
     * @return specific User subtype or null if userType does not match any known role
     * @throws SQLException if the underlying load methods encounter database issues
     */
    private User loadUserByType(String userId, String userType) throws SQLException {
        return switch (userType) {
            case "TENANT" -> loadTenant(userId);
            case "MANAGER" -> loadManager(userId);
            case "STAFF" -> loadStaff(userId);
            case "ADMIN" -> loadAdmin(userId);
            default -> null;
        };
    }

    /**
     * Load a Tenant user by ID, joining the base users table with the tenants table.
     *
     * @param userId ID of the tenant user
     * @return Tenant object or null if not found
     * @throws SQLException if any JDBC error occurs
     */
    private Tenant loadTenant(String userId) throws SQLException {
        String sql = "SELECT u.*, t.* FROM users u " +
                "JOIN tenants t ON u.user_id = t.user_id WHERE u.user_id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Tenant tenant = new Tenant();
                // Populate generic user fields from the result set
                populateUserFields(tenant, rs);
                // Populate tenant-specific fields
                tenant.setApartmentNumber(rs.getString("apartment_number"));
                return tenant;
            }
        }
        return null;
    }

    /**
     * Load a BuildingManager user by ID, joining the base users table with the building_managers table.
     *
     * @param userId ID of the building manager user
     * @return BuildingManager object or null if not found
     * @throws SQLException if any JDBC error occurs
     */
    private BuildingManager loadManager(String userId) throws SQLException {
        String sql = "SELECT u.*, m.* FROM users u " +
                "JOIN building_managers m ON u.user_id = m.user_id WHERE u.user_id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                BuildingManager manager = new BuildingManager();
                // Populate generic user fields
                populateUserFields(manager, rs);
                // Populate manager-specific fields
                manager.setEmployeeId(rs.getString("employee_id"));
                manager.setDepartment(rs.getString("department"));
                return manager;
            }
        }
        return null;
    }

    /**
     * Load a MaintenanceStaff user by ID, joining the base users table with the maintenance_staff table.
     *
     * @param userId ID of the maintenance staff user
     * @return MaintenanceStaff object or null if not found
     * @throws SQLException if any JDBC error occurs
     */
    private MaintenanceStaff loadStaff(String userId) throws SQLException {
        String sql = "SELECT u.*, s.* FROM users u " +
                "JOIN maintenance_staff s ON u.user_id = s.user_id WHERE u.user_id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                MaintenanceStaff staff = new MaintenanceStaff();
                // Populate shared user fields
                populateUserFields(staff, rs);
                // Populate staff-specific fields
                staff.setStaffId(rs.getString("staff_id"));
                staff.setCurrentWorkload(rs.getInt("current_workload"));
                staff.setMaxCapacity(rs.getInt("max_capacity"));
                staff.setAvailable(rs.getBoolean("is_available"));
                return staff;
            }
        }
        return null;
    }

    /**
     * Load an Admin user by ID from the base users table only.
     *
     * @param userId ID of the admin user
     * @return Admin object or null if not found
     * @throws SQLException if any JDBC error occurs
     */
    private Admin loadAdmin(String userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Admin admin = new Admin();
                // Populate all standard user fields
                populateUserFields(admin, rs);
                return admin;
            }
        }
        return null;
    }

    /**
     * Utility method to populate common user fields from a ResultSet into a User object.
     * Applies to all subclasses of User (Tenant, Manager, Staff, Admin).
     *
     * @param user User object to populate
     * @param rs   ResultSet containing the user record
     * @throws SQLException if any column access fails
     */
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

    /**
     * Update the last_login column for a given user to the current timestamp.
     * Called after a successful authentication.
     *
     * @param userId ID of the user whose last_login should be updated
     */
    private void updateLastLogin(String userId) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
        }
    }

    /**
     * Retrieve a list of all active maintenance staff.
     * This joins the users table with maintenance_staff and filters by is_active = TRUE.
     *
     * @return list of MaintenanceStaff objects representing active staff members
     */
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
                // Populate shared user fields
                populateUserFields(staff, rs);  // fills common user fields

                // Populate staff-specific fields
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

    /**
     * Convenience method to load a Tenant by user ID, wrapping SQLExceptions with logging.
     *
     * @param userId ID of the tenant user
     * @return Tenant object or null if an error occurs or tenant not found
     */
    public Tenant getTenantById(String userId) {
        try {
            return loadTenant(userId);
        } catch (SQLException e) {
            System.err.println("Error loading tenant by id: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieve MaintenanceStaff by staffId instead of userId.
     * Useful when the application tracks or displays staff using their staff_id field.
     *
     * @param staffId staff-specific identifier
     * @return MaintenanceStaff object or null if not found or error occurs
     */
    public MaintenanceStaff getStaffByStaffId(String staffId) {
        String sql = "SELECT u.*, s.* FROM users u " +
                "JOIN maintenance_staff s ON u.user_id = s.user_id WHERE s.staff_id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, staffId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                MaintenanceStaff staff = new MaintenanceStaff();
                // Populate shared user fields
                populateUserFields(staff, rs);
                // Populate staff-specific fields
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
