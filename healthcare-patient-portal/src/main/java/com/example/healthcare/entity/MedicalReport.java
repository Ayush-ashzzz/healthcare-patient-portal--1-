package com.example.healthcare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_reports")
public class MedicalReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    @Column(nullable = false, length = 255) private String originalFileName;
    @Column(nullable = false, length = 500) private String storedFileName;
    @Column(nullable = false, length = 100) private String contentType;
    @Column(nullable = false) private long fileSize;
    @Column(length = 500) private String description;
    @Column(nullable = false) private LocalDateTime uploadedAt = LocalDateTime.now();

    protected MedicalReport() {}
    public MedicalReport(Patient patient, String originalFileName, String storedFileName,
                         String contentType, long fileSize, String description) {
        this.patient = patient; this.originalFileName = originalFileName; this.storedFileName = storedFileName;
        this.contentType = contentType; this.fileSize = fileSize; this.description = description;
        this.uploadedAt = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public Patient getPatient() { return patient; }
    public String getOriginalFileName() { return originalFileName; }
    public String getStoredFileName() { return storedFileName; }
    public String getContentType() { return contentType; }
    public long getFileSize() { return fileSize; }
    public String getDescription() { return description; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
}
