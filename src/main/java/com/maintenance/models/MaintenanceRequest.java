package com.maintenance.models;

import com.maintenance.enums.CategoryType;
import com.maintenance.enums.PriorityLevel;
import com.maintenance.enums.RequestStatus;
import com.maintenance.util.IDGenerator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceRequest {
    private String requestId;
    private String tenantId;
    private String apartmentNumber;
    private String description;
    private String detailedDescription;
    private CategoryType category;
    private PriorityLevel priority;
    private RequestStatus status;
    private LocalDateTime submissionDate;
    private LocalDateTime lastUpdated;
    private LocalDateTime scheduledDate;
    private LocalDateTime completionDate;
    private double estimatedCost;
    private double actualCost;
    private String assignedStaffId;
    private String workOrderNumber;
    private String staffUpdateNotes;
    private String resolutionNotes;
    private List<Photo> photos;
    private WorkOrder workOrder;
    private boolean tenantArchived;
    private boolean staffArchived;

    public boolean isTenantArchived() {
        return tenantArchived;
    }

    public void setTenantArchived(boolean tenantArchived) {
        this.tenantArchived = tenantArchived;
    }

    public boolean isStaffArchived() {
        return staffArchived;
    }

    public void setStaffArchived(boolean staffArchived) {
        this.staffArchived = staffArchived;
    }

    public MaintenanceRequest() {
        this.requestId = IDGenerator.generateRequestId();
        this.submissionDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.status = RequestStatus.SUBMITTED;
        this.photos = new ArrayList<>();
    }

    public void createRequest(Tenant tenant, String description, CategoryType category) {
        this.tenantId = tenant.getUserId();
        this.apartmentNumber = tenant.getApartmentNumber();
        this.description = description;
        this.category = category;
        this.priority = calculatePriority();
        this.status = RequestStatus.SUBMITTED;
    }

    public void updateStatus(RequestStatus newStatus) {
        this.status = newStatus;
        this.lastUpdated = LocalDateTime.now();
    }

    public void assignToStaff(MaintenanceStaff staff) {
        this.assignedStaffId = staff.getStaffId();
        this.status = RequestStatus.ASSIGNED;
        this.lastUpdated = LocalDateTime.now();
        staff.assignRequest(this);
    }

    public void addPhoto(Photo photo) {
        photo.setRequestId(this.requestId);
        this.photos.add(photo);
    }

    public PriorityLevel calculatePriority() {
        if (category == CategoryType.EMERGENCY) {
            return PriorityLevel.EMERGENCY;
        } else if (category == CategoryType.ELECTRICAL || category == CategoryType.SAFETY_SECURITY) {
            return PriorityLevel.URGENT;
        } else if (category == CategoryType.PLUMBING || category == CategoryType.HVAC) {
            return PriorityLevel.HIGH;
        } else if (category == CategoryType.APPLIANCE) {
            return PriorityLevel.MEDIUM;
        }
        return PriorityLevel.LOW;
    }

    public WorkOrder generateWorkOrder() {
        if (this.workOrder == null) {
            this.workOrder = new WorkOrder();
            this.workOrder.generateWorkOrder(this);
            this.workOrderNumber = this.workOrder.getWorkOrderId();
        }
        return this.workOrder;
    }

    public void close(String resolution) {
        this.resolutionNotes = resolution;
        this.status = RequestStatus.COMPLETED;
        this.completionDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getApartmentNumber() { return apartmentNumber; }
    public void setApartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDetailedDescription() { return detailedDescription; }
    public void setDetailedDescription(String detailedDescription) { this.detailedDescription = detailedDescription; }

    public CategoryType getCategory() { return category; }
    public void setCategory(CategoryType category) { this.category = category; }

    public PriorityLevel getPriority() { return priority; }
    public void setPriority(PriorityLevel priority) { this.priority = priority; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public LocalDateTime getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDateTime scheduledDate) { this.scheduledDate = scheduledDate; }

    public LocalDateTime getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDateTime completionDate) { this.completionDate = completionDate; }

    public double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(double estimatedCost) { this.estimatedCost = estimatedCost; }

    public double getActualCost() { return actualCost; }
    public void setActualCost(double actualCost) { this.actualCost = actualCost; }

    public String getAssignedStaffId() { return assignedStaffId; }
    public void setAssignedStaffId(String assignedStaffId) { this.assignedStaffId = assignedStaffId; }

    public String getWorkOrderNumber() { return workOrderNumber; }
    public void setWorkOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; }

    public String getStaffUpdateNotes() { return staffUpdateNotes; }
    public void setStaffUpdateNotes(String staffUpdateNotes) { this.staffUpdateNotes = staffUpdateNotes; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public List<Photo> getPhotos() { return photos; }
    public void setPhotos(List<Photo> photos) { this.photos = photos; }

    public WorkOrder getWorkOrder() { return workOrder; }
    public void setWorkOrder(WorkOrder workOrder) { this.workOrder = workOrder; }
}
