package com.example.healthcare.dto;

import com.example.healthcare.entity.MedicalReport;
import java.time.LocalDateTime;

public record ReportResponse(Long id, String originalFileName, String contentType, long fileSize,
                             String description, LocalDateTime uploadedAt, String downloadUrl) {
    public static ReportResponse from(MedicalReport r) {
        return new ReportResponse(r.getId(), r.getOriginalFileName(), r.getContentType(), r.getFileSize(),
                r.getDescription(), r.getUploadedAt(), "/api/reports/" + r.getId() + "/download");
    }
}
