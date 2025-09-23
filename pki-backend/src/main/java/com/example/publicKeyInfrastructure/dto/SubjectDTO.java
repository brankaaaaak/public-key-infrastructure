package com.example.publicKeyInfrastructure.dto;

import com.example.publicKeyInfrastructure.model.Organization;
import com.example.publicKeyInfrastructure.model.User;
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

    public SubjectDTO(User user){
        this.name = user.getName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        if(user.getOrganization()==null)
            this.organization="";
        else
            this.organization = user.getOrganization().getName();
    }
}

