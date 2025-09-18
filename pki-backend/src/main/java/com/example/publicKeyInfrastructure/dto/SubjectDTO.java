package com.example.publicKeyInfrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SubjectDTO {
    private String name;
    private String lastName;
    private String email;
    private String organization;
}

