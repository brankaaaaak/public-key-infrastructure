package com.example.publicKeyInfrastructure.dto;

import com.example.publicKeyInfrastructure.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserViewDTO {
    private Long id;

    private String name;

    private String lastName;

    private String email;

    private String organization;

    private String role;

    private boolean enabled;

    public UserViewDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        if(user.getOrganization()!=null)
            this.organization = user.getOrganization().getName();
        else
            this.organization=null;
        this.role=user.getRoles().iterator().next().getName();
        this.enabled = user.isEnabled();
    }

}