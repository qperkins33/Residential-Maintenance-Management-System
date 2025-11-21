package com.maintenance.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class IDGenerator {
    private static final AtomicInteger buildingCounter = new AtomicInteger(1000);

    public static String generateRequestId() {
        return "REQ" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generateUserId() {
        return "USR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generateBuildingId() {
        return "BLD" + buildingCounter.incrementAndGet();
    }

    public static String generateApartmentId() {
        return "APT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generateWorkOrderId() {
        return "WO" + System.currentTimeMillis();
    }

    public static String generatePhotoId() {
        return "PHT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generateCommentId() {
        return "CMT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generateNotificationId() {
        return "NTF" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generateMessageId() {
        return "MSG" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generateReportId() {
        return "RPT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generateSystemId() {
        return "SYS" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
