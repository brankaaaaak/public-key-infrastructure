package com.example.publicKeyInfrastructure.dto;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCertificateRequestDTO {
    private String issuer;
    private String certificateType;
    private int durationInDays;
}
