package com.maintenance.models;

import com.maintenance.enums.ReportType;
import com.maintenance.util.IDGenerator;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Report {
    private String reportId;
    private ReportType reportType;
    private String generatedBy;
    private LocalDateTime generationDate;
    private DateRange dateRange;
    private String data;

    public Report() {
        this.reportId = IDGenerator.generateReportId();
        this.generationDate = LocalDateTime.now();
    }

    public void generateMaintenanceReport(LocalDate startDate, LocalDate endDate) {
        this.reportType = ReportType.MAINTENANCE_SUMMARY;
        this.dateRange = new DateRange(startDate, endDate);
        // Report generation logic
    }

    public void generateCostReport(String buildingId) {
        this.reportType = ReportType.COST_ANALYSIS;
        // Cost analysis logic
    }

    public void generatePerformanceReport(String staffId) {
        this.reportType = ReportType.STAFF_PERFORMANCE;
        // Performance analysis logic
    }

    public File exportToCSV() {
        // CSV export logic
        return new File("report_" + reportId + ".csv");
    }

    public File exportToPDF() {
        // PDF export logic
        return new File("report_" + reportId + ".pdf");
    }

    // Getters and Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public LocalDateTime getGenerationDate() { return generationDate; }
    public void setGenerationDate(LocalDateTime generationDate) { this.generationDate = generationDate; }

    public DateRange getDateRange() { return dateRange; }
    public void setDateRange(DateRange dateRange) { this.dateRange = dateRange; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}
