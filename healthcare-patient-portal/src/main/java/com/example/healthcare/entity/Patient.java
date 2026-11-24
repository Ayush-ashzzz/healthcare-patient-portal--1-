package com.example.healthcare.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Patient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100) private String fullName;
    @Column(nullable = false, unique = true, length = 190) private String email;
    @Column(nullable = false) private String passwordHash;
    private LocalDate dateOfBirth;
    @Column(length = 30) private String phone;
    @Column(length = 20) private String gender;
    @Column(length = 500) private String address;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();

    protected Patient() {}
    public Patient(String fullName, String email, String passwordHash, LocalDate dateOfBirth,
                   String phone, String gender, String address) {
        this.fullName = fullName; this.email = email; this.passwordHash = passwordHash;
        this.dateOfBirth = dateOfBirth; this.phone = phone; this.gender = gender; this.address = address;
        this.createdAt = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getPhone() { return phone; }
    public String getGender() { return gender; }
    public String getAddress() { return address; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
