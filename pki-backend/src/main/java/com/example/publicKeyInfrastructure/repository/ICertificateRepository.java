package com.example.publicKeyInfrastructure.repository;

import com.example.publicKeyInfrastructure.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICertificateRepository extends JpaRepository<Certificate, Long> {
}
