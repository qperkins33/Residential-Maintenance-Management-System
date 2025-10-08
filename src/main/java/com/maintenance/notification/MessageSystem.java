package com.maintenance.notification;

import com.maintenance.models.User;
import com.maintenance.util.IDGenerator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MessageSystem {
    private String messageId;
    private String senderId;
    private String receiverId;
    private String requestId;
    private String messageContent;
    private LocalDateTime timestamp;
    private boolean isRead;

    private static List<MessageSystem> allMessages = new ArrayList<>();

    public MessageSystem() {
        this.messageId = IDGenerator.generateMessageId();
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }

    public void sendMessage(User sender, User receiver, String content) {
        this.senderId = sender.getUserId();
        this.receiverId = receiver.getUserId();
        this.messageContent = content;
        this.timestamp = LocalDateTime.now();
        allMessages.add(this);
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public static List<MessageSystem> getConversation(String requestId) {
        return allMessages.stream()
                .filter(m -> m.getRequestId() != null && m.getRequestId().equals(requestId))
                .collect(Collectors.toList());
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getMessageContent() { return messageContent; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
