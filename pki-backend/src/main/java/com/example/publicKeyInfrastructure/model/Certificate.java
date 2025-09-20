package com.example.publicKeyInfrastructure.model;

import com.example.publicKeyInfrastructure.enumeration.CertificateType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "certificate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificateType certificateType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    @Column(name = "issuer_serial_number")
    private String issuerSerialNumber;

    @Column(name = "valid_from", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date validFrom;

    @Column(name = "valid_to", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date validTo;

    @Column(name = "is_revoked", nullable = false)
    private boolean revoked;

    @Column(name = "revocation_reason")
    private String revocationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Lob
    @Column(name="private_key",nullable = false)
    private byte[] encryptedPrivateKey;

    public boolean isValid() {
        Date now = new Date();
        return !now.before(validFrom) && now.before(validTo);
    }


}
