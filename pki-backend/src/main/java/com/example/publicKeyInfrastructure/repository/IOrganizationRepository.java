package com.example.publicKeyInfrastructure.repository;

import com.example.publicKeyInfrastructure.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IOrganizationRepository extends JpaRepository<Organization,Long> {
    @Query("SELECT o.name FROM Organization o")
    List<String> findAllOrganizationNames();
    Optional<Organization> findByName(String name);

}
