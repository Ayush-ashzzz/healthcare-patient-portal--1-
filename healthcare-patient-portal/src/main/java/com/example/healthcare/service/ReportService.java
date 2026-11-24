package com.example.healthcare.service;

import com.example.healthcare.entity.MedicalReport;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.repository.MedicalReportRepository;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReportService {
    private static final Set<String> ALLOWED_TYPES = Set.of("application/pdf", "image/jpeg", "image/png");
    private final MedicalReportRepository reports;
    private final Path uploadRoot;

    public ReportService(MedicalReportRepository reports, @Value("${app.upload-dir:./uploads}") String uploadDir) {
        this.reports = reports;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try { Files.createDirectories(uploadRoot); }
        catch (IOException e) { throw new IllegalStateException("Cannot initialize upload directory", e); }
    }

    public MedicalReport upload(Patient patient, MultipartFile file, String description) {
        if (file == null || file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a file to upload");
        if (file.getSize() > 10L * 1024 * 1024) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Maximum upload size is 10 MB");
        String type = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!ALLOWED_TYPES.contains(type)) throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only PDF, JPEG and PNG files are allowed");
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());
        if (original.contains("..")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        String stored = UUID.randomUUID().toString();
        try {
            Path target = uploadRoot.resolve(stored).normalize();
            if (!target.startsWith(uploadRoot)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
            file.transferTo(target);
            return reports.save(new MedicalReport(patient, original, stored, type, file.getSize(), description));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to store uploaded file");
        }
    }

    public List<MedicalReport> list(Patient p) {
        return reports.findAllByPatientIdOrderByUploadedAtDesc(p.getId());
    }

    public Path getFile(Patient p, Long id) {
        MedicalReport report = reports.findByIdAndPatientId(id, p.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        Path file = uploadRoot.resolve(report.getStoredFileName()).normalize();
        if (!file.startsWith(uploadRoot) || !Files.exists(file))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report file not found");
        return file;
    }

    public MedicalReport getMetadata(Patient p, Long id) {
        return reports.findByIdAndPatientId(id, p.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
    }
}
