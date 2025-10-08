package com.maintenance.notification;

import com.maintenance.enums.NotificationType;
import com.maintenance.models.MaintenanceRequest;
import com.maintenance.models.User;
import com.maintenance.util.IDGenerator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationSystem {
    private String notificationId;
    private String recipientId;
    private String message;
    private NotificationType notificationType;
    private boolean isRead;
    private LocalDateTime sentDate;

    private static List<NotificationSystem> allNotifications = new ArrayList<>();

    public NotificationSystem() {
        this.notificationId = IDGenerator.generateNotificationId();
        this.sentDate = LocalDateTime.now();
        this.isRead = false;
    }

    public void sendNotification(User recipient, String message, NotificationType type) {
        this.recipientId = recipient.getUserId();
        this.message = message;
        this.notificationType = type;
        this.sentDate = LocalDateTime.now();
        allNotifications.add(this);
    }

    public void sendStatusUpdate(MaintenanceRequest request) {
        String msg = "Request #" + request.getRequestId() + " status updated to: " +
                request.getStatus().getDisplayName();
        this.message = msg;
        this.notificationType = NotificationType.STATUS_UPDATE;
        allNotifications.add(this);
    }

    public void sendReminder(MaintenanceRequest request) {
        String msg = "Reminder: Request #" + request.getRequestId() + " is pending";
        this.message = msg;
        this.notificationType = NotificationType.REMINDER;
        allNotifications.add(this);
    }

    public void markAsRead(String notificationId) {
        for (NotificationSystem notification : allNotifications) {
            if (notification.getNotificationId().equals(notificationId)) {
                notification.setRead(true);
                break;
            }
        }
    }

    public static List<NotificationSystem> getUnreadNotifications(String userId) {
        return allNotifications.stream()
                .filter(n -> n.getRecipientId().equals(userId) && !n.isRead())
                .collect(Collectors.toList());
    }

    // Getters and Setters
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getSentDate() { return sentDate; }
    public void setSentDate(LocalDateTime sentDate) { this.sentDate = sentDate; }
}
