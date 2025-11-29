package com.maintenance.notification;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public final class Email {
    private Email() {}

    private static Session session() {
        final String user = must("SMTP_USERNAME");
        final String pass = must("SMTP_PASSWORD");

        Properties p = new Properties();
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.host", must("SMTP_HOST")); // e.g. smtp.gmail.com
        p.put("mail.smtp.port", must("SMTP_PORT")); // e.g. 587

        return Session.getInstance(p, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });
    }

    public static void send(String to, String subject, String textBody) {
        try {
            Message msg = new MimeMessage(session());
            msg.setFrom(new InternetAddress(must("SMTP_FROM"))); // e.g. "RMMS Notifications <rmms.noreply@gmail.com>"
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));

            String replyTo = System.getenv("SMTP_REPLY_TO");
            if (replyTo != null && !replyTo.isBlank()) {
                msg.setReplyTo(InternetAddress.parse(replyTo, false));
            }

            msg.setSubject(subject);
            msg.setText(textBody == null ? "" : textBody);
            Transport.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException("Email send failed", e);
        }
    }

    private static String must(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing env var: " + key);
        }
        return v;
    }
}
