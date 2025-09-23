package com.example.publicKeyInfrastructure.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bouncycastle.asn1.x500.X500Name;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateSubject {
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private X500Name x500name;
    private String serialNumber;
    private Date startDate;
    private Date endDate;
}
