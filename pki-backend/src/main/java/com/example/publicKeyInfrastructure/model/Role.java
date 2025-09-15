package com.example.publicKeyInfrastructure.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
public class Role implements GrantedAuthority {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name="name", unique = true, nullable = false)
    String name;
    @Override
    public String getAuthority() {
        return name;
    }
}
