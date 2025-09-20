package com.example.publicKeyInfrastructure.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "organization_keys")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrgKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // enkriptovan ključ (blob u bazi)
    @Lob
    @Column(name="organization_key",nullable = false)
    private byte[] encryptedKey;

    // 1:1 veza ka Organization
    @OneToOne
    @JoinColumn(name = "organization_id", nullable = false, unique = true)
    private Organization organization;

    public OrgKey(Organization org, byte[] encrypted) {
        this.organization=org;
        this.encryptedKey=encrypted;
    }
}

