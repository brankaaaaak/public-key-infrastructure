package com.example.publicKeyInfrastructure.dto;

import com.example.publicKeyInfrastructure.model.Organization;
import com.example.publicKeyInfrastructure.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserTableViewDTO {
    private Long id;

    private String name;

    private String lastName;

    private String email;

    private Organization organization;

    private String role;

    private boolean enabled;


    public UserTableViewDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        if(user.getOrganization()!=null)
            this.organization = user.getOrganization();
        else
            this.organization=null;
        this.role=user.getRoles().iterator().next().getName();
        this.enabled = user.isEnabled();

    }

}
