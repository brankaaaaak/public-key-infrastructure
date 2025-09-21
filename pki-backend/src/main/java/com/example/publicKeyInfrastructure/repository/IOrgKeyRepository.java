package com.example.publicKeyInfrastructure.repository;

import com.example.publicKeyInfrastructure.model.OrgKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IOrgKeyRepository extends JpaRepository<OrgKey,Long> {
    @Query("SELECT o FROM OrgKey o WHERE o.organization.id = :orgId")
    OrgKey findByOrgId(@Param("orgId") Long orgId);

}
