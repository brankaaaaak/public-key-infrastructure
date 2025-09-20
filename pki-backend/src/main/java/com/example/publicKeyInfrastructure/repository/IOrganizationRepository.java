package com.example.publicKeyInfrastructure.repository;

import com.example.publicKeyInfrastructure.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IOrganizationRepository extends JpaRepository<Organization,Long> {
}
