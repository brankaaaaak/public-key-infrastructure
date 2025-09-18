package com.example.publicKeyInfrastructure.repository;

import com.example.publicKeyInfrastructure.model.CertificateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICertificateRequestRepository extends JpaRepository<CertificateRequest, Long> {
}
