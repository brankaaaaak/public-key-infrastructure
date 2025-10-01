package com.example.publicKeyInfrastructure.service.implementation;

import com.example.publicKeyInfrastructure.crypto.KeystoreManager;
import com.example.publicKeyInfrastructure.data.CertificateIssuer;
import com.example.publicKeyInfrastructure.model.Certificate;
import com.example.publicKeyInfrastructure.repository.ICertificateRepository;
import com.example.publicKeyInfrastructure.service.interfaces.ICertificateRevokeService;
import jakarta.transaction.Transactional;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

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
    public List<Certificate> findAllRevokedDescendants(String issuerSerialNumber) {
        List<Certificate> result = new ArrayList<>();
        collectRevokedRecursive(issuerSerialNumber, result);
        return result;
    }

    private void collectRevokedRecursive(String issuerSerialNumber, List<Certificate> accumulator) {
        // Direktni potomci
        List<Certificate> children = certificateRepository.findByIssuerSerialNumber(issuerSerialNumber);

        for (Certificate child : children) {
            if (child.isRevoked()) {
                accumulator.add(child);
            }
            // Rekurzivno prolazimo sve potomke
            collectRevokedRecursive(child.getSerialNumber(), accumulator);
        }
    }
    @Autowired
    private KeystoreManager keystoreManager; // servis koji daje privatni ključ CA

    private Map<String, X509CRL> crlCache = new HashMap<>();

    // Generiše CRL za dati CA
    public X509CRL generateCRL(String caIdentifier,boolean isRoot) throws Exception {
        CertificateIssuer issuer = isRoot ?
                keystoreManager.readRootIssuer(caIdentifier) :
                keystoreManager.readOrgIssuer(Long.parseLong(caIdentifier.split("_")[1]), caIdentifier);

        X509Certificate caCert = isRoot ?
                keystoreManager.readRootCertificate(caIdentifier) :
                keystoreManager.readOrgCertificate(Long.parseLong(caIdentifier.split("_")[1]), caIdentifier);

        PrivateKey caPrivateKey = issuer.getPrivateKey();

        X509v2CRLBuilder crlBuilder = new X509v2CRLBuilder(
                new X500Name(caCert.getSubjectX500Principal().getName()),
                new Date()
        );
        crlBuilder.setNextUpdate(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)); // next update za 24h

        // Svi revoked sertifikati koje je ovaj CA izdao
        List<Certificate> revokedCerts = findAllRevokedDescendants(caIdentifier);
        for (Certificate revoked : revokedCerts) {
            int reasonValue = mapReasonStringToCRLReason(revoked.getRevocationReason());
            crlBuilder.addCRLEntry(
                    new BigInteger(revoked.getSerialNumber()),
                    Date.from(revoked.getRevocationDate().atZone(ZoneId.systemDefault()).toInstant()),
                    reasonValue
            );
        }

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").setProvider("BC").build(caPrivateKey);
        X509CRLHolder crlHolder = crlBuilder.build(signer);
        X509CRL crl = new JcaX509CRLConverter().setProvider("BC").getCRL(crlHolder);

        crlCache.put(caIdentifier, crl);
        return crl;
    }
    private int mapReasonStringToCRLReason(String reason) {
        return switch (reason) {
            case "unspecified" -> CRLReason.unspecified;
            case "keyCompromise" -> CRLReason.keyCompromise;
            case "cACompromise" -> CRLReason.cACompromise;
            case "affiliationChanged" -> CRLReason.affiliationChanged;
            case "superseded" -> CRLReason.superseded;
            case "cessationOfOperation" -> CRLReason.cessationOfOperation;
            case "certificateHold" -> CRLReason.certificateHold;
            case "privilegeWithdrawn" -> CRLReason.privilegeWithdrawn;
            case "aACompromise" -> CRLReason.aACompromise;
            default -> CRLReason.unspecified;
        };
    }


    public byte[] getCRLBytes(String caIdentifier,boolean isRoot) throws Exception {
        X509CRL crl = crlCache.getOrDefault(caIdentifier, generateCRL(caIdentifier,isRoot));
        return crl.getEncoded();
    }
}
