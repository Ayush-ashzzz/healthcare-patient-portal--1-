package com.example.healthcare.controller;

import com.example.healthcare.dto.PatientProfile;
import com.example.healthcare.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@Tag(name = "Patients", description = "Patient profile operations")
public class PatientController {
    private final PatientService patients;
    public PatientController(PatientService patients) { this.patients = patients; }

    @GetMapping("/me")
    @Operation(summary = "Get the current patient's profile")
    @SecurityRequirement(name = "sessionCookie")
    public PatientProfile profile(Authentication authentication) {
        return PatientProfile.from(patients.getByEmail(authentication.getName()));
    }
}
