package com.maintenance.models;

import java.time.LocalDateTime;

public class Notification {
    private String notificationId;
    private String recipientId;
    private String message;
    private String notificationType; // align with DB values
    private boolean isRead;
    private LocalDateTime sentDate;

    public Notification() {}

    public Notification(String notificationId, String recipientId, String message,
                        String notificationType, boolean isRead, LocalDateTime sentDate) {
        this.notificationId = notificationId;
        this.recipientId = recipientId;
        this.message = message;
        this.notificationType = notificationType;
        this.isRead = isRead;
        this.sentDate = sentDate;
    }

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getSentDate() { return sentDate; }
    public void setSentDate(LocalDateTime sentDate) { this.sentDate = sentDate; }
}
