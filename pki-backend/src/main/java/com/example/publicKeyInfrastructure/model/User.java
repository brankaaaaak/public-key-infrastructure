package com.example.publicKeyInfrastructure.model;

import com.example.publicKeyInfrastructure.dto.RegisterDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "app_user")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable=false)
    private String name;
    @Column(name = "last_name", nullable=false)
    private String lastName;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = true)
    private String organization;
    @Column(name = "enabled", nullable = false)
    private boolean enabled;
    @JsonIgnore
    @Column(name = "password", nullable=false)
    private String password;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Set<Role> roles;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }
    @Override
    public String getPassword() {
        return password;
    }
    @Override
    public String getUsername() {
        return email;
    }

    public User(RegisterDTO dto){
        this.name = dto.getName();
        this.lastName = dto.getLastName();
        //pripaziti na orgnizaciju koja je izmenjena
        this.organization = dto.getOrganization();
        this.email = dto.getEmail();
    }
}
