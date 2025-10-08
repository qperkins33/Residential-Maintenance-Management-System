package com.maintenance.models;

import com.maintenance.enums.LeaseStatus;
import com.maintenance.util.IDGenerator;
import java.util.ArrayList;
import java.util.List;

public class Apartment {
    private String apartmentId;
    private String apartmentNumber;
    private String buildingId;
    private String currentTenantId;
    private String floorPlan;
    private int squareFootage;
    private double monthlyRent;
    private LeaseStatus leaseStatus;
    private List<MaintenanceRequest> maintenanceHistory;

    public Apartment() {
        this.apartmentId = IDGenerator.generateApartmentId();
        this.leaseStatus = LeaseStatus.AVAILABLE;
        this.maintenanceHistory = new ArrayList<>();
    }

    public Apartment(String apartmentNumber, String buildingId) {
        this();
        this.apartmentNumber = apartmentNumber;
        this.buildingId = buildingId;
    }

    public void assignTenant(Tenant tenant) {
        // Now getUserId() will work since User class has public getter
        this.currentTenantId = tenant.getUserId();
        this.leaseStatus = LeaseStatus.OCCUPIED;
        tenant.setApartmentNumber(this.apartmentNumber);
    }

    public List<MaintenanceRequest> getMaintenanceHistory() {
        return new ArrayList<>(maintenanceHistory);
    }

    public void updateLeaseStatus(LeaseStatus status) {
        this.leaseStatus = status;
    }

    public void addMaintenanceRequest(MaintenanceRequest request) {
        this.maintenanceHistory.add(request);
    }

    // Getters and Setters
    public String getApartmentId() { return apartmentId; }
    public void setApartmentId(String apartmentId) { this.apartmentId = apartmentId; }

    public String getApartmentNumber() { return apartmentNumber; }
    public void setApartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getCurrentTenantId() { return currentTenantId; }
    public void setCurrentTenantId(String currentTenantId) { this.currentTenantId = currentTenantId; }

    public String getFloorPlan() { return floorPlan; }
    public void setFloorPlan(String floorPlan) { this.floorPlan = floorPlan; }

    public int getSquareFootage() { return squareFootage; }
    public void setSquareFootage(int squareFootage) { this.squareFootage = squareFootage; }

    public double getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(double monthlyRent) { this.monthlyRent = monthlyRent; }

    public LeaseStatus getLeaseStatus() { return leaseStatus; }
    public void setLeaseStatus(LeaseStatus leaseStatus) { this.leaseStatus = leaseStatus; }
}
