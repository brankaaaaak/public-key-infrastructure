package com.example.publicKeyInfrastructure.model;

import com.example.publicKeyInfrastructure.dto.CertificateRequestDTO;
import com.example.publicKeyInfrastructure.dto.CreateCertificateRequestDTO;
import com.example.publicKeyInfrastructure.enumeration.CertificateType;
import com.example.publicKeyInfrastructure.enumeration.RequestType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "status", nullable = false)
    private RequestType requestType;

    @ManyToOne(fetch = FetchType.EAGER)
    private Certificate issuer;

    @Column(name = "type", nullable = false)
    private CertificateType certificateType;

    @ManyToOne
    private User subject;

    @Column(name = "refusal_reason")
    private String refusalReason;

    @Column(name = "duration_in_days", nullable = false)
    private int durationInDays;

    @Column(name="key_usages",nullable=true)
    private List<String> keyUsages;

    @Column(name="extended_key_usages",nullable = true)
    private List<String> extendedKeyUsages;

    @Column(name="csr_pem_file",nullable=true,columnDefinition = "TEXT")
    private String csrPem;


    public CertificateRequest(CreateCertificateRequestDTO dto){
        this.certificateType = dto.getCertificateType() != null
                ? CertificateType.valueOf(dto.getCertificateType())
                : null;
        this.durationInDays = dto.getDurationInDays();
        this.requestType = RequestType.PENDING;
        this.createdAt = LocalDateTime.now();
        this.keyUsages=dto.getKeyUsages();
        this.extendedKeyUsages=dto.getExtendedKeyUsages();
        this.csrPem=dto.getCsrPem();
    }

}
