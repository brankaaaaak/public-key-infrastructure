package com.example.publicKeyInfrastructure.repository;

import com.example.publicKeyInfrastructure.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

}
