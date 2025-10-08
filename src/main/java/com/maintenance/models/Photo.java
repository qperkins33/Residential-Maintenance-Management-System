package com.maintenance.models;

import com.maintenance.util.IDGenerator;
import java.io.File;
import java.time.LocalDateTime;

public class Photo {
    private String photoId;
    private String requestId;
    private String fileName;
    private String filePath;
    private long fileSize;
    private LocalDateTime uploadDate;
    private String description;

    public Photo() {
        this.photoId = IDGenerator.generatePhotoId();
        this.uploadDate = LocalDateTime.now();
    }

    public boolean uploadPhoto(File file) {
        if (file.exists()) {
            this.fileName = file.getName();
            this.filePath = file.getAbsolutePath();
            this.fileSize = file.length();
            return true;
        }
        return false;
    }

    public boolean deletePhoto() {
        File file = new File(filePath);
        return file.delete();
    }

    public String getPhotoUrl() {
        return "file://" + filePath;
    }

    // Getters and Setters
    public String getPhotoId() { return photoId; }
    public void setPhotoId(String photoId) { this.photoId = photoId; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
