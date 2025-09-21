package com.example.publicKeyInfrastructure.repository;

import com.example.publicKeyInfrastructure.enumeration.CertificateType;
import com.example.publicKeyInfrastructure.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ICertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findBySerialNumber(String serialNumber);

    boolean existsByCertificateType(CertificateType certificateType);
}
