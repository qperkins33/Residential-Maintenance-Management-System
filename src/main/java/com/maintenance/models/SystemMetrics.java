package com.maintenance.models;

public class SystemMetrics {
    private int totalRequests;
    private double averageResponseTime;
    private double completionRate;
    private double tenantSatisfactionScore;

    public SystemMetrics() {
    }

    public void calculateMetrics() {
        // Metrics calculation logic
    }

    public Report getPerformanceReport() {
        Report report = new Report();
        report.setData("System Performance Metrics");
        return report;
    }

    // Getters and Setters
    public int getTotalRequests() { return totalRequests; }
    public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }

    public double getAverageResponseTime() { return averageResponseTime; }
    public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }

    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }

    public double getTenantSatisfactionScore() { return tenantSatisfactionScore; }
    public void setTenantSatisfactionScore(double tenantSatisfactionScore) { this.tenantSatisfactionScore = tenantSatisfactionScore; }
}
