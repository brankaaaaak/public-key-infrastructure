package com.example.publicKeyInfrastructure.service.implementation;

import com.example.publicKeyInfrastructure.model.Certificate;
import com.example.publicKeyInfrastructure.repository.ICertificateRepository;
import com.example.publicKeyInfrastructure.service.interfaces.ICertificateRevokeService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
public class CertificateRevokeService implements ICertificateRevokeService {
    @Autowired
    private ICertificateRepository certificateRepository;
    @Transactional
    @Override
    public void revokeCertificateAndDescendants(String serialNumber, String reason) {
        Certificate cert = certificateRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new RuntimeException("Certificate not found: " + serialNumber));

        revokeRecursive(cert, reason);
    }

    private void revokeRecursive(Certificate cert, String reason) {
        if (!cert.isRevoked()) {
            cert.setRevoked(true);
            cert.setRevocationReason(reason);
            certificateRepository.save(cert);
        }

        List<Certificate> children = certificateRepository.findByIssuerSerialNumber(cert.getSerialNumber());
        for (Certificate child : children) {
            revokeRecursive(child, "CACompromise"); // svi potomci se vode kao kompromitovani
        }
    }
}
