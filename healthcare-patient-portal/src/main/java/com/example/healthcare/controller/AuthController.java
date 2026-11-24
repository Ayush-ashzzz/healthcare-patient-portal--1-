package com.example.healthcare.controller;

import com.example.healthcare.dto.LoginRequest;
import com.example.healthcare.dto.PatientProfile;
import com.example.healthcare.dto.RegisterRequest;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.repository.PatientRepository;
import com.example.healthcare.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Patient registration, login, and session management")
public class AuthController {
    private final PatientService patientService;
    private final PatientRepository patients;
    private final AuthenticationManager authenticationManager;

    public AuthController(PatientService patientService, PatientRepository patients,
                          AuthenticationManager authenticationManager) {
        this.patientService = patientService; this.patients = patients; this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a patient account")
    public PatientProfile register(@Valid @RequestBody RegisterRequest request) {
        return PatientProfile.from(patientService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Log in and create a session")
    public PatientProfile login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        SecurityContextHolder.getContext().setAuthentication(auth);
        httpRequest.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
        Patient patient = patients.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Patient account not found"));
        return PatientProfile.from(patient);
    }

    @GetMapping("/me")
    @Operation(summary = "Get the current authenticated patient")
    @SecurityRequirement(name = "sessionCookie")
    public PatientProfile me(Authentication authentication) {
        return PatientProfile.from(patientService.getByEmail(authentication.getName()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Log out and invalidate the current session")
    @SecurityRequirement(name = "sessionCookie")
    public void logout() {
        // Spring Security's configured /api/auth/logout filter handles this URL before MVC.
    }
}
