package com.maintenance.service;

import com.maintenance.notification.Email;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class NotificationService {

    /** Email only, async. Call after the DB status update succeeds. */
    public CompletionStage<Void> statusEmailAsync(String toEmail, String requestId, String newStatus) {
        String subject = "Request " + requestId + " updated to " + newStatus;
        String body = "Status for request " + requestId + " is now " + newStatus + ".";
        return CompletableFuture.runAsync(() -> Email.send(toEmail, subject, body));
    }
}
