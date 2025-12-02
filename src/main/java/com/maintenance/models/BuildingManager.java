package com.maintenance.models;

import com.maintenance.enums.PriorityLevel;
import com.maintenance.enums.RequestStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BuildingManager extends User {
    private String employeeId;
    private List<Building> managedBuildings;
    private String department;

    public BuildingManager() {
        super();
        this.managedBuildings = new ArrayList<>();
    }

    public BuildingManager(String username, String password, String firstName, String lastName,
                           String email, String phoneNumber, String employeeId) {
        super(username, password, firstName, lastName, email, phoneNumber);
        this.employeeId = employeeId;
        this.managedBuildings = new ArrayList<>();
    }

    public List<MaintenanceRequest> viewAllRequests() {
        List<MaintenanceRequest> allRequests = new ArrayList<>();
        for (Building building : managedBuildings) {
            allRequests.addAll(building.getMaintenanceHistory());
        }
        return allRequests;
    }

    public List<MaintenanceRequest> viewRequestsByStatus(RequestStatus status) {
        return viewAllRequests().stream()
                .filter(r -> r.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<MaintenanceRequest> viewRequestsByPriority(PriorityLevel priority) {
        return viewAllRequests().stream()
                .filter(r -> r.getPriority() == priority)
                .collect(Collectors.toList());
    }

    public void assignRequest(String requestId, MaintenanceStaff maintenanceStaff) {
        for (MaintenanceRequest request : viewAllRequests()) {
            if (request.getRequestId().equals(requestId)) {
                request.assignToStaff(maintenanceStaff);
                break;
            }
        }
    }

    public void updateRequestStatus(String requestId, RequestStatus status) {
        for (MaintenanceRequest request : viewAllRequests()) {
            if (request.getRequestId().equals(requestId)) {
                request.updateStatus(status);
                break;
            }
        }
    }

    // PUBLIC Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public List<Building> getManagedBuildings() {
        return managedBuildings;
    }

    public void setManagedBuildings(List<Building> managedBuildings) {
        this.managedBuildings = managedBuildings;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
