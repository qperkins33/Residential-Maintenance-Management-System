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
        // SQL statement for inserting a new photo record, with upload_date auto-populated
        String sql = "INSERT INTO photos (" +
                "photo_id, request_id, file_name, file_path, file_size, upload_date" +
                ") VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        // Generate a short, human-readable photo ID prefix coupled with a random UUID segment
        String photoId = "PHO-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();

        try {
            // Obtain a connection from the shared DatabaseManager (connection is not closed here)
            Connection conn = dbManager.getConnection();
            // Use try-with-resources so the PreparedStatement is closed automatically
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                // Bind parameters for the INSERT statement
                ps.setString(1, photoId);
                ps.setString(2, requestId);
                ps.setString(3, fileName);
                ps.setString(4, filePath);
                ps.setLong(5, fileSize);

                // Intentionally using the prepared statement here without closing the shared connection
                // Execution of the SQL statement would occur here using ps.executeUpdate()
            }
        } catch (Exception e) {
            // Log any exception related to inserting the photo metadata
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
        // SQL query to retrieve the most recent photo file_path for the given request
        String sql = "SELECT file_path " +
                "FROM photos " +
                "WHERE request_id = ? " +
                "ORDER BY upload_date DESC " +
                "LIMIT 1";

        try {
            // Obtain a shared connection (do not close it here; only close resources you own)
            Connection conn = dbManager.getConnection(); // do NOT close
            // Prepare the statement with the given requestId
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, requestId);
                // Execute the query and inspect the first row, if present
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Return the file path of the latest photo
                        return rs.getString("file_path");
                    }
                }
            }
        } catch (Exception e) {
            // Log any exception related to fetching the latest photo metadata
            System.err.println("Error loading photo metadata: " + e.getMessage());
        }
        // Return null if no photo was found or an error occurred
        return null;
    }
}
