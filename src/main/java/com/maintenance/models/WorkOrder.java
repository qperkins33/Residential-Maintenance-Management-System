package com.maintenance.models;

import com.maintenance.enums.WorkOrderStatus;
import com.maintenance.util.IDGenerator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WorkOrder {
    private String workOrderId;
    private String requestId;
    private String assignedStaffId;
    private LocalDateTime scheduledDateTime;
    private int estimatedDuration;
    private List<String> partsRequired;
    private String instructions;
    private WorkOrderStatus status;

    public WorkOrder() {
        this.workOrderId = IDGenerator.generateWorkOrderId();
        this.partsRequired = new ArrayList<>();
        this.status = WorkOrderStatus.CREATED;
    }

    public void generateWorkOrder(MaintenanceRequest request) {
        this.requestId = request.getRequestId();
        this.assignedStaffId = request.getAssignedStaffId();
        this.scheduledDateTime = request.getScheduledDate();
        this.instructions = request.getDescription();
        this.status = WorkOrderStatus.CREATED;
    }

    public void updateSchedule(LocalDateTime newDateTime) {
        this.scheduledDateTime = newDateTime;
        this.status = WorkOrderStatus.SCHEDULED;
    }

    public void addParts(List<String> parts) {
        this.partsRequired.addAll(parts);
    }

    public void markComplete() {
        this.status = WorkOrderStatus.COMPLETED;
    }

    // Getters and Setters
    public String getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(String workOrderId) { this.workOrderId = workOrderId; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getAssignedStaffId() { return assignedStaffId; }
    public void setAssignedStaffId(String assignedStaffId) { this.assignedStaffId = assignedStaffId; }

    public LocalDateTime getScheduledDateTime() { return scheduledDateTime; }
    public void setScheduledDateTime(LocalDateTime scheduledDateTime) { this.scheduledDateTime = scheduledDateTime; }

    public int getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(int estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public List<String> getPartsRequired() { return partsRequired; }
    public void setPartsRequired(List<String> partsRequired) { this.partsRequired = partsRequired; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public WorkOrderStatus getStatus() { return status; }
    public void setStatus(WorkOrderStatus status) { this.status = status; }
}
