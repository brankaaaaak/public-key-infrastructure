package com.example.publicKeyInfrastructure.repository;

import com.example.publicKeyInfrastructure.dto.CertificateDTO;
import com.example.publicKeyInfrastructure.enumeration.CertificateType;
import com.example.publicKeyInfrastructure.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ICertificateRepository extends JpaRepository<Certificate, Long> {
    @Query("SELECT c FROM Certificate c WHERE c.serialNumber = :serialNumber")
    Optional<Certificate> findBySerialNumber(@Param("serialNumber") String serialNumber);

    boolean existsByCertificateType(CertificateType certificateType);

    @Query("SELECT new com.example.publicKeyInfrastructure.dto.CertificateDTO(" +
            "            c.id," +
            "            c.certificateType," +
            "            c.serialNumber," +
            "            c.owner.email," +
            "            c.organization.name," +
            "            c.validFrom," +
            "            c.validTo" +
            "        ) FROM Certificate c " +
            "JOIN c.owner o " +
            "LEFT JOIN c.organization org "+
            "WHERE c.organization.id = :orgId " +
            "AND c.certificateType = 'INTERMEDIATE' " +
            "AND c.revoked = false " +
            "AND CURRENT_TIMESTAMP BETWEEN c.validFrom AND c.validTo")
    List<CertificateDTO> findValidIntermediateByOrganization(Long orgId);

    // Root sertifikat (nema issuer i organizaciju, tip = ROOT, validan, nije revoked)
    @Query("SELECT new com.example.publicKeyInfrastructure.dto.CertificateDTO(" +
            "            c.id," +
            "            c.certificateType," +
            "            c.serialNumber," +
            "            c.owner.email," +
            "            c.organization.name," +
            "            c.validFrom," +
            "            c.validTo" +
            "        ) FROM Certificate c "+
            "JOIN c.owner o " +
            "LEFT JOIN c.organization org "+
            "WHERE c.certificateType = 'ROOT'" +
            "AND revoked=false " +
            "AND CURRENT_TIMESTAMP BETWEEN c.validFrom AND c.validTo" )
    Optional<CertificateDTO> findValidRootCertificate();
}
