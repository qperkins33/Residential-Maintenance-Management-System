package com.maintenance.dao;

import com.maintenance.database.DatabaseManager;
import com.maintenance.enums.LeaseStatus;
import com.maintenance.models.Apartment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApartmentDAO {
    private final DatabaseManager dbManager;

    public ApartmentDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public List<Apartment> getApartmentsByBuilding(String buildingId) {
        List<Apartment> apartments = new ArrayList<>();
        String sql = "SELECT * FROM apartments WHERE building_id = ?";

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, buildingId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Apartment apartment = new Apartment();
                apartment.setApartmentId(rs.getString("apartment_id"));
                apartment.setApartmentNumber(rs.getString("apartment_number"));
                apartment.setBuildingId(rs.getString("building_id"));
                apartment.setCurrentTenantId(rs.getString("current_tenant_id"));
                apartment.setFloorPlan(rs.getString("floor_plan"));
                apartment.setSquareFootage(rs.getInt("square_footage"));
                apartment.setMonthlyRent(rs.getDouble("monthly_rent"));
                apartment.setLeaseStatus(LeaseStatus.valueOf(rs.getString("lease_status")));
                apartments.add(apartment);
            }
        } catch (SQLException e) {
            System.err.println("Error loading apartments: " + e.getMessage());
        }

        return apartments;
    }
}
