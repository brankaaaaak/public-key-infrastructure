package com.example.publicKeyInfrastructure.dto;

import com.example.publicKeyInfrastructure.enumeration.CertificateType;
import com.example.publicKeyInfrastructure.model.CertificateRequest;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequestDTO {
    private Long id;

    private String date;

    private String requestType;

    private String issuer;

    private String certificateType;

    private SubjectDTO subject;

    private String refusalReason;

    public CertificateRequestDTO(CertificateRequest request) {
        this.id = request.getId();
        this.date = request.getCreatedAt().toString();
        this.requestType = request.getRequestType().toString();
        if (request.getCertificateType() != CertificateType.ROOT)
            this.issuer = request.getIssuer().getSerialNumber();
        this.certificateType = request.getCertificateType().toString();
        this.refusalReason = request.getRefusalReason();
        this.subject = new SubjectDTO(request.getSubject());
    }
}
