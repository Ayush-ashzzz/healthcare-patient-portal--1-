package com.example.healthcare.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record RegisterRequest(
    @NotBlank @Size(max=100) String fullName,
    @NotBlank @Email @Size(max=190) String email,
    @NotBlank @Size(min=8, max=72) String password,
    @Past LocalDate dateOfBirth,
    @Size(max=30) String phone,
    @Size(max=20) String gender,
    @Size(max=500) String address
) {}
