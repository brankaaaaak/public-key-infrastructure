package com.example.publicKeyInfrastructure.repository;

import com.example.publicKeyInfrastructure.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRepository extends JpaRepository<User, Long> {
}
