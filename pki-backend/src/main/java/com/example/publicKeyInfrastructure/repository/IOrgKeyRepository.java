package com.example.publicKeyInfrastructure.repository;

import com.example.publicKeyInfrastructure.model.OrgKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IOrgKeyRepository extends JpaRepository<OrgKey,Long> {
    OrgKey findByOrgId(Long orgId);
}
