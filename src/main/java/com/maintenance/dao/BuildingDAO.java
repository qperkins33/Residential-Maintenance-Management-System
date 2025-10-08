package com.maintenance.dao;

import com.maintenance.database.DatabaseManager;
import com.maintenance.models.Building;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BuildingDAO {
    private final DatabaseManager dbManager;

    public BuildingDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public List<Building> getAllBuildings() {
        List<Building> buildings = new ArrayList<>();
        String sql = "SELECT * FROM buildings";

        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Building building = new Building();
                building.setBuildingId(rs.getString("building_id"));
                building.setBuildingName(rs.getString("building_name"));
                building.setAddress(rs.getString("address"));
                building.setManagerId(rs.getString("manager_id"));
                building.setTotalUnits(rs.getInt("total_units"));
                building.setBuildingType(rs.getString("building_type"));
                building.setConstructionYear(rs.getInt("construction_year"));
                buildings.add(building);
            }
        } catch (SQLException e) {
            System.err.println("Error loading buildings: " + e.getMessage());
        }

        return buildings;
    }
}
