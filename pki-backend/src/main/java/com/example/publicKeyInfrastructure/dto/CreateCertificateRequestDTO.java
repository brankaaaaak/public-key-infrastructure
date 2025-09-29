package com.example.publicKeyInfrastructure.dto;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCertificateRequestDTO {
    private String issuer;
    private String certificateType;
    private int durationInDays;
    private List<String> keyUsages;
    private List<String> extendedKeyUsages;
    private String csrPem;
    private Long ownerId;
}
