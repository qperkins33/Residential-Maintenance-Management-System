package com.maintenance.models;

import com.maintenance.enums.CategoryType;
import com.maintenance.enums.PriorityLevel;
import com.maintenance.enums.RequestStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Tenant extends User {
    private String apartmentNumber;
    private LocalDate leaseStartDate;
    private LocalDate leaseEndDate;
    private String emergencyContact;
    private String emergencyPhone;
    private List<MaintenanceRequest> myRequests;

    public Tenant() {
        super();
        this.myRequests = new ArrayList<>();
    }

    public Tenant(String username, String password, String firstName, String lastName,
                  String email, String phoneNumber, String apartmentNumber) {
        super(username, password, firstName, lastName, email, phoneNumber);
        this.apartmentNumber = apartmentNumber;
        this.myRequests = new ArrayList<>();
    }

    public String submitMaintenanceRequest(String description, CategoryType category, PriorityLevel priority) {
        MaintenanceRequest request = new MaintenanceRequest();
        request.setTenantId(this.userId);  // Using inherited protected field
        request.setApartmentNumber(this.apartmentNumber);
        request.setDescription(description);
        request.setCategory(category);
        request.setPriority(priority);
        request.setStatus(RequestStatus.SUBMITTED);
        myRequests.add(request);
        return request.getRequestId();
    }

    public void uploadPhotos(String requestId, List<Photo> photos) {
        for (MaintenanceRequest request : myRequests) {
            if (request.getRequestId().equals(requestId)) {
                for (Photo photo : photos) {
                    request.addPhoto(photo);
                }
                break;
            }
        }
    }

    public List<MaintenanceRequest> viewMyRequests() {
        return new ArrayList<>(myRequests);
    }

    public RequestStatus viewRequestStatus(String requestId) {
        for (MaintenanceRequest request : myRequests) {
            if (request.getRequestId().equals(requestId)) {
                return request.getStatus();
            }
        }
        return null;
    }

    // Getters and Setters for Tenant-specific fields
    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }

    public LocalDate getLeaseStartDate() {
        return leaseStartDate;
    }

    public void setLeaseStartDate(LocalDate leaseStartDate) {
        this.leaseStartDate = leaseStartDate;
    }

    public LocalDate getLeaseEndDate() {
        return leaseEndDate;
    }

    public void setLeaseEndDate(LocalDate leaseEndDate) {
        this.leaseEndDate = leaseEndDate;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }

    public List<MaintenanceRequest> getMyRequests() {
        return myRequests;
    }

    public void setMyRequests(List<MaintenanceRequest> myRequests) {
        this.myRequests = myRequests;
    }

    // Note: getUserId() is inherited from User class
}
