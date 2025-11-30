package com.maintenance.dao;

import com.maintenance.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public class PhotoDAO {

    private final DatabaseManager dbManager;

    public PhotoDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Save a photo record for a maintenance request.
     * IMPORTANT: does NOT close the shared Connection.
     */
    public void savePhotoForRequest(String requestId,
                                    String fileName,
                                    String filePath,
                                    long fileSize) {
        String sql = "INSERT INTO photos (" +
                "photo_id, request_id, file_name, file_path, file_size, upload_date" +
                ") VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        String photoId = "PHO-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();

        try {
            Connection conn = dbManager.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, photoId);
                ps.setString(2, requestId);
                ps.setString(3, fileName);
                ps.setString(4, filePath);
                ps.setLong(5, fileSize);
            }
        } catch (Exception e) {
            System.err.println("Error saving photo metadata: " + e.getMessage());
        }
    }

    /**
     * Get the most recent photo path for a request, or null if none.
     */
    public String getLatestPhotoPathForRequest(String requestId) {
        String sql = "SELECT file_path " +
                "FROM photos " +
                "WHERE request_id = ? " +
                "ORDER BY upload_date DESC " +
                "LIMIT 1";

        try {
            Connection conn = dbManager.getConnection(); // do NOT close
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, requestId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("file_path");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading photo metadata: " + e.getMessage());
        }
        return null;
    }
}
