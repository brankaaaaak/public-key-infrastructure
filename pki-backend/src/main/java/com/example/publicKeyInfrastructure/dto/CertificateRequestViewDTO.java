package com.example.publicKeyInfrastructure.dto;

import com.example.publicKeyInfrastructure.enumeration.CertificateType;
import com.example.publicKeyInfrastructure.enumeration.RequestType;
import com.example.publicKeyInfrastructure.model.Certificate;
import com.example.publicKeyInfrastructure.model.CertificateRequest;
import com.example.publicKeyInfrastructure.model.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequestViewDTO {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private RequestType requestType;
    private String issuerSerialNumber;
    private CertificateType issuerType;
    private Date issuerValidFrom;
    private Date issuerValidTo;
    private CertificateType certificateType;
    private String subjectEmail;
    private String refusalReason;
    private int durationInDays;
    public CertificateRequestViewDTO(CertificateRequest cr){
        this.id=cr.getId();
        this.createdAt=cr.getCreatedAt();
        this.requestType=cr.getRequestType();
        this.issuerSerialNumber=cr.getIssuer().getSerialNumber();
        this.issuerType=cr.getIssuer().getCertificateType();
        this.issuerValidFrom=cr.getIssuer().getValidFrom();
        this.issuerValidTo=cr.getIssuer().getValidTo();
        this.certificateType=cr.getCertificateType();
        this.subjectEmail=cr.getSubject().getEmail();
        this.refusalReason=cr.getRefusalReason();
        this.durationInDays=cr.getDurationInDays();
    }
}
