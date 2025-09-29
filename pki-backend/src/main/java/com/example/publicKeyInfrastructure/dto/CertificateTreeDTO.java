package com.example.publicKeyInfrastructure.dto;

import com.example.publicKeyInfrastructure.enumeration.CertificateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateTreeDTO {
    private String serialNumber;
    private CertificateType type;
    private String ownerEmail;
    private boolean revoked;
    private List<CertificateTreeDTO> children = new ArrayList<>();
}