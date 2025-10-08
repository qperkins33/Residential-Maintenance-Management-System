package com.maintenance.service;

import com.maintenance.dao.MaintenanceRequestDAO;
import com.maintenance.enums.PriorityLevel;
import com.maintenance.enums.RequestStatus;
import com.maintenance.models.MaintenanceRequest;
import com.maintenance.models.SystemMetrics;
import com.maintenance.util.IDGenerator;
import java.util.List;
import java.util.stream.Collectors;

public class TicketingSystem {
    private String systemId;
    private int totalRequests;
    private int activeRequests;
    private int completedRequests;
    private final MaintenanceRequestDAO requestDAO;

    public TicketingSystem() {
        this.systemId = IDGenerator.generateSystemId();
        this.requestDAO = new MaintenanceRequestDAO();
    }

    public String submitRequest(MaintenanceRequest request) {
        if (requestDAO.saveRequest(request)) {
            totalRequests++;
            activeRequests++;
            return request.getRequestId();
        }
        return null;
    }

    public MaintenanceRequest getRequestById(String requestId) {
        List<MaintenanceRequest> allRequests = requestDAO.getAllRequests();
        return allRequests.stream()
                .filter(r -> r.getRequestId().equals(requestId))
                .findFirst()
                .orElse(null);
    }

    public List<MaintenanceRequest> getAllRequests() {
        return requestDAO.getAllRequests();
    }

    public List<MaintenanceRequest> getRequestsByStatus(RequestStatus status) {
        return requestDAO.getRequestsByStatus(status);
    }

    public List<MaintenanceRequest> getRequestsByPriority(PriorityLevel priority) {
        return requestDAO.getAllRequests().stream()
                .filter(r -> r.getPriority() == priority)
                .collect(Collectors.toList());
    }

    public String generateTicketNumber() {
        return IDGenerator.generateRequestId();
    }

    public SystemMetrics calculateSystemMetrics() {
        SystemMetrics metrics = new SystemMetrics();
        List<MaintenanceRequest> allRequests = requestDAO.getAllRequests();

        metrics.setTotalRequests(allRequests.size());

        long completed = allRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED ||
                        r.getStatus() == RequestStatus.CLOSED)
                .count();

        if (allRequests.size() > 0) {
            metrics.setCompletionRate((double) completed / allRequests.size() * 100);
        }

        return metrics;
    }

    // Getters and Setters
    public String getSystemId() { return systemId; }
    public int getTotalRequests() { return totalRequests; }
    public int getActiveRequests() { return activeRequests; }
    public int getCompletedRequests() { return completedRequests; }
}
