package com.example.healthcare.dto;

import com.example.healthcare.entity.Patient;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientProfile(Long id, String fullName, String email, LocalDate dateOfBirth,
                             String phone, String gender, String address, LocalDateTime createdAt) {
    public static PatientProfile from(Patient p) {
        return new PatientProfile(p.getId(), p.getFullName(), p.getEmail(), p.getDateOfBirth(),
                p.getPhone(), p.getGender(), p.getAddress(), p.getCreatedAt());
    }
}
