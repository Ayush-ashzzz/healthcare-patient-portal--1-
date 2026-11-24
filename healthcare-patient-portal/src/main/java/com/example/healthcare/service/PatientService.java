package com.example.healthcare.service;

import com.example.healthcare.dto.RegisterRequest;
import com.example.healthcare.entity.Patient;
import com.example.healthcare.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PatientService {
    private final PatientRepository patients;
    private final PasswordEncoder encoder;
    public PatientService(PatientRepository patients, PasswordEncoder encoder) {
        this.patients = patients; this.encoder = encoder;
    }
    public Patient register(RegisterRequest r) {
        if (patients.existsByEmailIgnoreCase(r.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        Patient p = new Patient(r.fullName().trim(), r.email().trim().toLowerCase(),
                encoder.encode(r.password()), r.dateOfBirth(), r.phone(), r.gender(), r.address());
        return patients.save(p);
    }
    public Patient getByEmail(String email) {
        return patients.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated patient not found"));
    }
}
