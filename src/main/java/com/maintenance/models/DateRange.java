package com.maintenance.models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateRange {
    private LocalDate startDate;
    private LocalDate endDate;

    public DateRange() {
    }

    public DateRange(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean isWithinRange(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public int getDurationInDays() {
        return (int) ChronoUnit.DAYS.between(startDate, endDate);
    }

    // Getters and Setters
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
