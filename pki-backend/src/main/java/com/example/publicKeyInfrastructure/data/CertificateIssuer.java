package com.example.publicKeyInfrastructure.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bouncycastle.asn1.x500.X500Name;

import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateIssuer {
    private X500Name x500name;
    private PrivateKey privateKey;
    private PublicKey publicKey;
}
