package com.maintenance.database;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        Connection conn = dbManager.getConnection();

        try (Statement stmt = conn.createStatement()) {

            // TESTING: Uncomment to wipe db
//            stmt.execute("DROP TABLE IF EXISTS comments");
//            stmt.execute("DROP TABLE IF EXISTS photos");
//            stmt.execute("DROP TABLE IF EXISTS work_orders");
//            stmt.execute("DROP TABLE IF EXISTS maintenance_requests");
//            stmt.execute("DROP TABLE IF EXISTS apartments");
//            stmt.execute("DROP TABLE IF EXISTS tenants");
//            stmt.execute("DROP TABLE IF EXISTS maintenance_staff");
//            stmt.execute("DROP TABLE IF EXISTS buildings");
//            stmt.execute("DROP TABLE IF EXISTS building_managers");
//            stmt.execute("DROP TABLE IF EXISTS users");

            // Create Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id VARCHAR(50) PRIMARY KEY," +
                    "username VARCHAR(50) UNIQUE NOT NULL," +
                    "password VARCHAR(100) NOT NULL," +
                    "first_name VARCHAR(50)," +
                    "last_name VARCHAR(50)," +
                    "email VARCHAR(100)," +
                    "phone_number VARCHAR(20)," +
                    "user_type VARCHAR(20)," +
                    "date_created TIMESTAMP," +
                    "last_login TIMESTAMP," +
                    "is_active BOOLEAN)");

            // Create Tenants table
            stmt.execute("CREATE TABLE IF NOT EXISTS tenants (" +
                    "user_id VARCHAR(50) PRIMARY KEY," +
                    "apartment_number VARCHAR(20)," +
                    "lease_start_date DATE," +
                    "lease_end_date DATE," +
                    "emergency_contact VARCHAR(100)," +
                    "emergency_phone VARCHAR(20)," +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id))");

            // Create Maintenance Staff table
            stmt.execute("CREATE TABLE IF NOT EXISTS maintenance_staff (" +
                    "user_id VARCHAR(50) PRIMARY KEY," +
                    "staff_id VARCHAR(50) UNIQUE," +
                    "specializations VARCHAR(500)," +
                    "current_workload INT," +
                    "max_capacity INT," +
                    "is_available BOOLEAN," +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id))");

            // Create Building Managers table
            stmt.execute("CREATE TABLE IF NOT EXISTS building_managers (" +
                    "user_id VARCHAR(50) PRIMARY KEY," +
                    "employee_id VARCHAR(50) UNIQUE," +
                    "department VARCHAR(100)," +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id))");

            // Create Buildings table
            stmt.execute("CREATE TABLE IF NOT EXISTS buildings (" +
                    "building_id VARCHAR(50) PRIMARY KEY," +
                    "building_name VARCHAR(100)," +
                    "address VARCHAR(200)," +
                    "manager_id VARCHAR(50)," +
                    "total_units INT," +
                    "building_type VARCHAR(50)," +
                    "construction_year INT)");

            // Create Apartments table
            stmt.execute("CREATE TABLE IF NOT EXISTS apartments (" +
                    "apartment_id VARCHAR(50) PRIMARY KEY," +
                    "apartment_number VARCHAR(20)," +
                    "building_id VARCHAR(50)," +
                    "current_tenant_id VARCHAR(50)," +
                    "floor_plan VARCHAR(50)," +
                    "square_footage INT," +
                    "monthly_rent DECIMAL(10,2)," +
                    "lease_status VARCHAR(20)," +
                    "FOREIGN KEY (building_id) REFERENCES buildings(building_id))");

            // Create Maintenance Requests table
            stmt.execute("CREATE TABLE IF NOT EXISTS maintenance_requests (" +
                    "request_id VARCHAR(50) PRIMARY KEY," +
                    "tenant_id VARCHAR(50)," +
                    "apartment_number VARCHAR(20)," +
                    "description VARCHAR(1000)," +
                    "detailed_description VARCHAR(2000)," +
                    "category VARCHAR(50)," +
                    "priority VARCHAR(20)," +
                    "status VARCHAR(20)," +
                    "submission_date TIMESTAMP," +
                    "last_updated TIMESTAMP," +
                    "scheduled_date TIMESTAMP," +
                    "completion_date TIMESTAMP," +
                    "estimated_cost DECIMAL(10,2)," +
                    "actual_cost DECIMAL(10,2)," +
                    "assigned_staff_id VARCHAR(50)," +
                    "work_order_number VARCHAR(50)," +
                    "staff_update_notes VARCHAR(1000)," +
                    "resolution_notes VARCHAR(1000)," +
                    "tenant_archived BOOLEAN DEFAULT FALSE," +
                    "staff_archived BOOLEAN DEFAULT FALSE)");

            // Create Photos table
            stmt.execute("CREATE TABLE IF NOT EXISTS photos (" +
                    "photo_id VARCHAR(50) PRIMARY KEY," +
                    "request_id VARCHAR(50)," +
                    "file_name VARCHAR(200)," +
                    "file_path VARCHAR(500)," +
                    "file_size BIGINT," +
                    "upload_date TIMESTAMP," +
                    "description VARCHAR(500))");

            // Create Comments table
            stmt.execute("CREATE TABLE IF NOT EXISTS comments (" +
                    "comment_id VARCHAR(50) PRIMARY KEY," +
                    "request_id VARCHAR(50)," +
                    "user_id VARCHAR(50)," +
                    "comment_text VARCHAR(2000)," +
                    "timestamp TIMESTAMP," +
                    "is_internal BOOLEAN)");

            // Create Work Orders table
            stmt.execute("CREATE TABLE IF NOT EXISTS work_orders (" +
                    "work_order_id VARCHAR(50) PRIMARY KEY," +
                    "request_id VARCHAR(50)," +
                    "assigned_staff_id VARCHAR(50)," +
                    "scheduled_date_time TIMESTAMP," +
                    "estimated_duration INT," +
                    "parts_required VARCHAR(1000)," +
                    "instructions VARCHAR(2000)," +
                    "status VARCHAR(20))");

            // Insert default users
            insertDefaultUsers(stmt);

            System.out.println("✓ Database tables created successfully");

        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    private static void insertDefaultUsers(Statement stmt) {
        try {
            // Insert default admin
            stmt.execute("MERGE INTO users (user_id, username, password, first_name, last_name, " +
                    "email, phone_number, user_type, date_created, is_active) " +
                    "VALUES ('A001', 'admin1', 'pass123', 'System', 'Admin', 'admin@email.com', " +
                    "'555-0100', 'ADMIN', CURRENT_TIMESTAMP, true)");

            // Insert default tenant
//            stmt.execute("MERGE INTO users (user_id, username, password, first_name, last_name, " +
//                    "email, phone_number, user_type, date_created, is_active) " +
//                    "VALUES ('T001', 'tenant1', 'pass123', 'John', 'Doe', 'john@email.com', " +
//                    "'555-0101', 'TENANT', CURRENT_TIMESTAMP, true)");
//
//            stmt.execute("MERGE INTO tenants (user_id, apartment_number, lease_start_date, lease_end_date) " +
//                    "VALUES ('T001', 'A101', '2024-01-01', '2025-12-31')");

            // Insert default building manager
//            stmt.execute("MERGE INTO users (user_id, username, password, first_name, last_name, " +
//                    "email, phone_number, user_type, date_created, is_active) " +
//                    "VALUES ('M001', 'manager1', 'pass123', 'Jane', 'Smith', 'jane@email.com', " +
//                    "'555-0102', 'MANAGER', CURRENT_TIMESTAMP, true)");
//
//            stmt.execute("MERGE INTO building_managers (user_id, employee_id, department) " +
//                    "VALUES ('M001', 'EMP001', 'Operations')");

            // Insert default maintenance staff
//            stmt.execute("MERGE INTO users (user_id, username, password, first_name, last_name, " +
//                    "email, phone_number, user_type, date_created, is_active) " +
//                    "VALUES ('S001', 'staff1', 'pass123', 'Mike', 'Johnson', 'mike@email.com', " +
//                    "'555-0103', 'STAFF', CURRENT_TIMESTAMP, true)");
//
//            stmt.execute("MERGE INTO maintenance_staff (user_id, staff_id, specializations, " +
//                    "current_workload, max_capacity, is_available) " +
//                    "VALUES ('S001', 'STF001', 'Plumbing,Electrical', 0, 10, true)");

            // Insert default building
            stmt.execute("MERGE INTO buildings (building_id, building_name, address, manager_id, " +
                    "total_units, building_type) " +
                    "VALUES ('B001', 'Sunset Apartments', '123 Main St', NULL, 50, 'Residential')");

            // Insert default apartment
            stmt.execute("MERGE INTO apartments (apartment_id, apartment_number, building_id, " +
                    "current_tenant_id, square_footage, monthly_rent, lease_status) " +
                    "VALUES ('AP001', 'A101', 'B001', NULL, 850, 1200.00, 'OCCUPIED')");

            System.out.println("✓ Default users and data inserted");

        } catch (Exception e) {
            System.err.println("Error inserting default users: " + e.getMessage());
        }
    }
}
