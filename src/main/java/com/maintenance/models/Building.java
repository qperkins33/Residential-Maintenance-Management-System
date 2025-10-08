package com.maintenance.models;

import com.maintenance.util.IDGenerator;
import java.util.ArrayList;
import java.util.List;

public class Building {
    private String buildingId;
    private String buildingName;
    private String address;
    private String managerId;
    private int totalUnits;
    private String buildingType;
    private int constructionYear;
    private List<Apartment> apartments;
    private List<MaintenanceRequest> maintenanceHistory;

    public Building() {
        this.buildingId = IDGenerator.generateBuildingId();
        this.apartments = new ArrayList<>();
        this.maintenanceHistory = new ArrayList<>();
    }

    public Building(String buildingName, String address, String managerId, int totalUnits) {
        this();
        this.buildingName = buildingName;
        this.address = address;
        this.managerId = managerId;
        this.totalUnits = totalUnits;
    }

    public void addApartment(Apartment apartment) {
        apartment.setBuildingId(this.buildingId);
        this.apartments.add(apartment);
    }

    public List<Apartment> getApartments() {
        return new ArrayList<>(apartments);
    }

    public List<MaintenanceRequest> getMaintenanceHistory() {
        return new ArrayList<>(maintenanceHistory);
    }

    public void addMaintenanceRequest(MaintenanceRequest request) {
        this.maintenanceHistory.add(request);
    }

    // Getters and Setters
    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }

    public int getTotalUnits() { return totalUnits; }
    public void setTotalUnits(int totalUnits) { this.totalUnits = totalUnits; }

    public String getBuildingType() { return buildingType; }
    public void setBuildingType(String buildingType) { this.buildingType = buildingType; }

    public int getConstructionYear() { return constructionYear; }
    public void setConstructionYear(int constructionYear) { this.constructionYear = constructionYear; }
}
