package com.example.publicKeyInfrastructure.dto;

import com.example.publicKeyInfrastructure.enumeration.CertificateType;
import com.example.publicKeyInfrastructure.model.Certificate;
import com.example.publicKeyInfrastructure.model.Organization;
import com.example.publicKeyInfrastructure.model.User;
import lombok.*;

import java.util.Date;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDTO {
    private Long id;
    private CertificateType certificateType;
    private String serialNumber;
    private String owner;
    private String organization;
    private Date validFrom;
    private Date validTo;
    public CertificateDTO(Certificate certificate){
        this.id=certificate.getId();
        this.certificateType=certificate.getCertificateType();
        this.serialNumber=certificate.getSerialNumber();
        this.owner=certificate.getOwner().getEmail();
        if(certificate.getOrganization()!=null)
            this.organization=certificate.getOrganization().getName();
        else
            this.organization=null;
        this.validFrom=certificate.getValidFrom();
        this.validTo=certificate.getValidTo();
    }

}
