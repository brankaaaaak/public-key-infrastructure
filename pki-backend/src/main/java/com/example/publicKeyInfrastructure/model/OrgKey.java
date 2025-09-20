package com.example.publicKeyInfrastructure.model;

import jakarta.persistence.*;

@Entity
@Table(name = "organization_keys")
public class OrgKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // enkriptovan ključ (blob u bazi)
    @Lob
    @Column(nullable = false)
    private byte[] encryptedKey;

    // 1:1 veza ka Organization
    @OneToOne
    @JoinColumn(name = "organization_id", nullable = false, unique = true)
    private Organization organization;

}

