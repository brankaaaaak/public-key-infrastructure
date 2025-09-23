package com.example.publicKeyInfrastructure.repository;

import com.example.publicKeyInfrastructure.enumeration.RequestType;
import com.example.publicKeyInfrastructure.model.CertificateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ICertificateRequestRepository extends JpaRepository<CertificateRequest, Long> {
    @Query("SELECT cr FROM CertificateRequest cr " +
            "WHERE cr.issuer.owner.id = :ownerId " +
            "AND requestType = :status")
    List<CertificateRequest> findPendingRequestsByOwnerCertificates(@Param("ownerId") Long ownerId,
                                                                    @Param("status") RequestType status);
    @Query("SELECT cr FROM CertificateRequest cr " +
            "WHERE cr.requestType = :status")
    List<CertificateRequest> findPendingRequestsCertificates(@Param("status") RequestType status);
}
