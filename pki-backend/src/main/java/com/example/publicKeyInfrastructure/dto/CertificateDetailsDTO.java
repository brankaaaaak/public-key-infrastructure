package com.example.publicKeyInfrastructure.dto;

import com.example.publicKeyInfrastructure.enumeration.CertificateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateDetailsDTO {
    private String serialNumber;
    private CertificateType type;
    private String ownerEmail;
    private String issuerSerialNumber;
    private String validFrom;
    private String validTo;
    private boolean revoked;
}
