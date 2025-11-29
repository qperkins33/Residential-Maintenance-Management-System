package com.maintenance.dao;

import com.maintenance.database.DatabaseManager;
import com.maintenance.models.Notification;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NotificationDAO {

    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    public String insert(String recipientUserId, String type, String message) {
        String id = "NTF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String sql = """
            INSERT INTO notifications
              (notification_id, recipient_id, message, notification_type, is_read, sent_date)
            VALUES (?, ?, ?, ?, false, CURRENT_TIMESTAMP)
        """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, recipientUserId);
            ps.setString(3, message);
            ps.setString(4, type);
            ps.executeUpdate();
            return id;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert notification", e);
        }
    }

    public void markRead(String notificationId) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE notifications SET is_read = true WHERE notification_id = ?")) {
            ps.setString(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark read", e);
        }
    }

    public Optional<Notification> findById(String id) {
        String sql = "SELECT notification_id, recipient_id, message, notification_type, is_read, sent_date FROM notifications WHERE notification_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load notification", e);
        }
    }

    public List<Notification> listUnread(String userId, int limit) {
        String sql = """
            SELECT notification_id, recipient_id, message, notification_type, is_read, sent_date
            FROM notifications
            WHERE recipient_id = ? AND is_read = false
            ORDER BY sent_date DESC
            LIMIT ?
        """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                List<Notification> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list unread notifications", e);
        }
    }

    private static Notification map(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getString(1));
        n.setRecipientId(rs.getString(2));
        n.setMessage(rs.getString(3));
        n.setNotificationType(rs.getString(4));
        n.setRead(rs.getBoolean(5));
        Timestamp ts = rs.getTimestamp(6);
        n.setSentDate(ts == null ? null : ts.toLocalDateTime());
        return n;
    }
}
