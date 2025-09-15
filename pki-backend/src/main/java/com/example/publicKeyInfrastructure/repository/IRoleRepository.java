package com.example.publicKeyInfrastructure.repository;

import com.example.publicKeyInfrastructure.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRoleRepository extends JpaRepository<Role, Long> {
}
