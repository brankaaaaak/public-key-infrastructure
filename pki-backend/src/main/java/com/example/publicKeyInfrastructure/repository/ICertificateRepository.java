package com.example.publicKeyInfrastructure.repository;

import com.example.publicKeyInfrastructure.dto.CertificateDTO;
import com.example.publicKeyInfrastructure.enumeration.CertificateType;
import com.example.publicKeyInfrastructure.model.Certificate;
import com.example.publicKeyInfrastructure.model.CertificateRequest;
import com.example.publicKeyInfrastructure.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ICertificateRepository extends JpaRepository<Certificate, Long> {
    /*@Query("SELECT c FROM Certificate c WHERE c.serialNumber = :serialNumber")
    Optional<Certificate> findBySerialNumber(@Param("serialNumber") String serialNumber);*/

    boolean existsByCertificateType(CertificateType certificateType);

    // Intermediate sertifikati po organizaciji
    @Query("SELECT c " +
            "FROM Certificate c " +
            "LEFT JOIN FETCH c.organization " +
            "WHERE c.organization.id = :orgId " +
            "AND c.certificateType = 'INTERMEDIATE' " +
            "AND c.revoked = false " +
            "AND CURRENT_TIMESTAMP BETWEEN c.validFrom AND c.validTo")
    List<Certificate> findValidIntermediateByOrganization(@Param("orgId") Long orgId);

    // Root sertifikat (nema issuer i organizaciju, tip = ROOT, validan, nije revoked)
    @Query("SELECT c " +
            "FROM Certificate c " +
            "WHERE c.certificateType = 'ROOT' " +
            "AND c.revoked = false " +
            "AND CURRENT_TIMESTAMP BETWEEN c.validFrom AND c.validTo")
    Optional<Certificate> findValidRootCertificate();
    Optional<Certificate> findByRequest(CertificateRequest request);
    List<Certificate> findAllByOwner(User owner);
    List<Certificate> findByIssuerSerialNumber(String issuerSerialNumber);
    Optional<Certificate> findBySerialNumber(String serialNumber);

    // Svi validni intermediate sertifikati
    @Query("SELECT c " +
            "FROM Certificate c " +
            "LEFT JOIN FETCH c.organization " +
            "WHERE c.certificateType = 'INTERMEDIATE' " +
            "AND c.revoked = false " +
            "AND CURRENT_TIMESTAMP BETWEEN c.validFrom AND c.validTo")
    List<Certificate> findValidIntermediate();
}
