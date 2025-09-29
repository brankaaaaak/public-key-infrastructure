package com.example.publicKeyInfrastructure.dto;

import com.example.publicKeyInfrastructure.model.Organization;
import lombok.Data;

@Data
public class OrganizationDTO {
    private Long id;
    private String name;

    public OrganizationDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    public OrganizationDTO(Organization org) {
        this.id = org.getId();
        this.name = org.getName();
    }
}
