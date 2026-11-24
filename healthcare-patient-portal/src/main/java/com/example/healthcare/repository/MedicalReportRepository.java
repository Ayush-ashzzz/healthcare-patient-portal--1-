package com.example.healthcare.repository;

import com.example.healthcare.entity.MedicalReport;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalReportRepository extends JpaRepository<MedicalReport, Long> {
    List<MedicalReport> findAllByPatientIdOrderByUploadedAtDesc(Long patientId);
    Optional<MedicalReport> findByIdAndPatientId(Long id, Long patientId);
}
