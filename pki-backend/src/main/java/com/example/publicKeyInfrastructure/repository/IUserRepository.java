package com.example.publicKeyInfrastructure.repository;

import com.example.publicKeyInfrastructure.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    @Query("SELECT u FROM User u JOIN u.roles r LEFT JOIN FETCH u.organization " +
            "WHERE u.enabled = true AND r.name <> 'ROLE_ADMIN'")
    List<User> findAllEnabledUsers();

    @Query("SELECT u FROM User u JOIN u.roles r LEFT JOIN FETCH u.organization " +
            "WHERE u.enabled = true AND r.name <> 'ROLE_ADMIN' AND u.organization.id = :orgId")
    List<User> findAllEnabledUsersByOrganization(@Param("orgId") Long orgId);
}
