package com.example.publicKeyInfrastructure.dto;

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
}
