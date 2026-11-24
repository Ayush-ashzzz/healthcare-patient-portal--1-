package com.example.healthcare.controller;

import com.example.healthcare.dto.ReportResponse;
import com.example.healthcare.entity.MedicalReport;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.service.PatientService;
import com.example.healthcare.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Medical reports", description = "Upload, list, and download the authenticated patient's reports")
@SecurityRequirement(name = "sessionCookie")
public class ReportController {
    private final ReportService reports;
    private final PatientService patients;
    public ReportController(ReportService reports, PatientService patients) {
        this.reports = reports; this.patients = patients;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload a medical report")
    public ReportResponse upload(Authentication auth, @RequestPart("file") MultipartFile file,
                                 @RequestPart(value="description", required=false) String description) {
        Patient p = patients.getByEmail(auth.getName());
        return ReportResponse.from(reports.upload(p, file, description));
    }

    @GetMapping
    @Operation(summary = "List the current patient's medical reports")
    public List<ReportResponse> list(Authentication auth) {
        return reports.list(patients.getByEmail(auth.getName())).stream().map(ReportResponse::from).toList();
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download a medical report")
    public ResponseEntity<Resource> download(Authentication auth, @PathVariable Long id) throws IOException {
        Patient p = patients.getByEmail(auth.getName());
        MedicalReport metadata = reports.getMetadata(p, id);
        Path path = reports.getFile(p, id);
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(metadata.getOriginalFileName()).build().toString())
                .body(resource);
    }
}
