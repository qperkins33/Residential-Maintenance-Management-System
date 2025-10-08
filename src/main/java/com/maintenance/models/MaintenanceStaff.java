package com.maintenance.models;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceStaff extends User {
    private String staffId;
    private List<String> specializations;
    private int currentWorkload;
    private int maxCapacity;
    private boolean isAvailable;
    private List<MaintenanceRequest> assignedRequests;

    public MaintenanceStaff() {
        super();
        this.specializations = new ArrayList<>();
        this.assignedRequests = new ArrayList<>();
        this.maxCapacity = 10;
        this.isAvailable = true;
        this.currentWorkload = 0;
    }

    public MaintenanceStaff(String username, String password, String firstName, String lastName,
                            String email, String phoneNumber, String staffId) {
        super(username, password, firstName, lastName, email, phoneNumber);
        this.staffId = staffId;
        this.specializations = new ArrayList<>();
        this.assignedRequests = new ArrayList<>();
        this.maxCapacity = 10;
        this.isAvailable = true;
        this.currentWorkload = 0;
    }

    public List<MaintenanceRequest> viewAssignedRequests() {
        return new ArrayList<>(assignedRequests);
    }

    public void updateWorkProgress(String requestId, String progress) {
        for (MaintenanceRequest request : assignedRequests) {
            if (request.getRequestId().equals(requestId)) {
                Comment comment = new Comment();
                comment.setUserId(this.userId);
                comment.setCommentText("Progress Update: " + progress);
                request.addComment(comment);
                break;
            }
        }
    }

    public void markRequestComplete(String requestId, String resolution) {
        for (MaintenanceRequest request : assignedRequests) {
            if (request.getRequestId().equals(requestId)) {
                request.close(resolution);
                currentWorkload--;
                if (currentWorkload < 0) currentWorkload = 0;
                break;
            }
        }
    }

    public void requestParts(String requestId, List<String> partsList) {
        for (MaintenanceRequest request : assignedRequests) {
            if (request.getRequestId().equals(requestId)) {
                WorkOrder workOrder = request.generateWorkOrder();
                workOrder.addParts(partsList);
                break;
            }
        }
    }

    public void updateAvailability(boolean available) {
        this.isAvailable = available;
    }

    public void assignRequest(MaintenanceRequest request) {
        if (currentWorkload < maxCapacity) {
            assignedRequests.add(request);
            currentWorkload++;
        }
    }

    // PUBLIC Getters and Setters
    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public List<String> getSpecializations() {
        return specializations;
    }

    public void setSpecializations(List<String> specializations) {
        this.specializations = specializations;
    }

    public int getCurrentWorkload() {
        return currentWorkload;
    }

    public void setCurrentWorkload(int currentWorkload) {
        this.currentWorkload = currentWorkload;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public List<MaintenanceRequest> getAssignedRequests() {
        return assignedRequests;
    }

    public void setAssignedRequests(List<MaintenanceRequest> assignedRequests) {
        this.assignedRequests = assignedRequests;
    }
}
