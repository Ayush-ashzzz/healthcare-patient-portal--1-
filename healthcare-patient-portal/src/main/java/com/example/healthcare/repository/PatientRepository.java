package com.example.healthcare.repository;

import com.example.healthcare.entity.Patient;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
