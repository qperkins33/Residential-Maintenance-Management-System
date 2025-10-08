package com.maintenance.models;

import com.maintenance.util.IDGenerator;
import java.time.LocalDateTime;

public class Comment {
    private String commentId;
    private String requestId;
    private String userId;
    private String commentText;
    private LocalDateTime timestamp;
    private boolean isInternal;

    public Comment() {
        this.commentId = IDGenerator.generateCommentId();
        this.timestamp = LocalDateTime.now();
        this.isInternal = false;
    }

    public void addComment(User user, String text) {
        this.userId = user.getUserId();
        this.commentText = text;
        this.timestamp = LocalDateTime.now();
    }

    public void editComment(String newText) {
        this.commentText = newText;
        this.timestamp = LocalDateTime.now();
    }

    public void deleteComment() {
        this.commentText = "[Comment deleted]";
    }

    // Getters and Setters
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isInternal() { return isInternal; }
    public void setInternal(boolean internal) { isInternal = internal; }
}
