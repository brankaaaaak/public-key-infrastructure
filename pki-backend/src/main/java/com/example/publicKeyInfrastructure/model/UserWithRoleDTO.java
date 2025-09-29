package com.example.publicKeyInfrastructure.model;

import com.example.publicKeyInfrastructure.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserWithRoleDTO {
    private Long id;
    private String name;
    private String lastName;
    private String email;
    private String organization;
    private boolean enabled;
    private String role;

    public UserWithRoleDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        if (user.getOrganization() != null)
            this.organization = user.getOrganization().getName();
        else
            this.organization = null;
        this.enabled = user.isEnabled();
        this.role = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse(null);
    }
}
