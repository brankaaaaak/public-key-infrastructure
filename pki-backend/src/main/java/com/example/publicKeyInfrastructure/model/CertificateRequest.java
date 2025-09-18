package com.example.publicKeyInfrastructure.model;

import com.example.publicKeyInfrastructure.enumeration.CertificateType;
import com.example.publicKeyInfrastructure.enumeration.RequestType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
}
