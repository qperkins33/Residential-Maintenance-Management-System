package com.maintenance.dao;

import com.maintenance.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

/**
 * Data Access Object for handling photo metadata associated with maintenance requests.
 * This DAO is responsible only for inserting and querying photo records in the database.
 */
public class PhotoDAO {
    /**
     * Shared database manager used to obtain JDBC connections.
     */
    private final DatabaseManager dbManager;

    /**
     * Default constructor that retrieves the singleton DatabaseManager instance.
     */
    public PhotoDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Save a photo record for a maintenance request.
     * IMPORTANT: does NOT close the shared Connection.
     *
     * @param requestId ID of the maintenance request the photo is linked to
     * @param fileName  original file name of the uploaded photo
     * @param filePath  resolved path on disk or storage where the photo is stored
     * @param fileSize  size of the file in bytes
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
            Connection conn = dbManager.getConnection(); // do NOT close
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, photoId);
                ps.setString(2, requestId);
                ps.setString(3, fileName);
                ps.setString(4, filePath);
                ps.setLong(5, fileSize);

                // THIS WAS MISSING
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    System.err.println("PhotoDAO.savePhotoForRequest: insert affected 0 rows for requestId=" + requestId);
                } else {
                    System.out.println("PhotoDAO.savePhotoForRequest: saved photo " + photoId +
                            " for requestId=" + requestId + " at " + filePath);
                }

                // If you have disabled auto-commit somewhere, you may also need:
                // conn.commit();
            }
        } catch (Exception e) {
            System.err.println("Error saving photo metadata: " + e.getMessage());
        }
    }

    /**
     * Get the most recent photo path for a request, or null if none.
     *
     * @param requestId ID of the maintenance request whose latest photo path is requested
     * @return String file path of the most recently uploaded photo or null if none exist
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
                        String path = rs.getString("file_path");
                        System.out.println("PhotoDAO.getLatestPhotoPathForRequest: found path=" + path +
                                " for requestId=" + requestId);
                        return path;
                    } else {
                        System.out.println("PhotoDAO.getLatestPhotoPathForRequest: no photo for requestId=" + requestId);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading photo metadata: " + e.getMessage());
        }
        return null;
    }
}
