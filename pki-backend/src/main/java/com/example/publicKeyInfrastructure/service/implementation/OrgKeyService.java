package com.example.publicKeyInfrastructure.service.implementation;

import com.example.publicKeyInfrastructure.crypto.CryptoUtils;
import com.example.publicKeyInfrastructure.crypto.MasterKeyManager;
import com.example.publicKeyInfrastructure.model.OrgKey;
import com.example.publicKeyInfrastructure.model.Organization;
import com.example.publicKeyInfrastructure.repository.IOrgKeyRepository;
import com.example.publicKeyInfrastructure.repository.IOrganizationRepository;
import com.example.publicKeyInfrastructure.service.interfaces.IOrgKeyService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.SecretKey;

public class OrgKeyService implements IOrgKeyService {
    private final MasterKeyManager masterKeyManager;

    @Autowired
    private IOrgKeyRepository repository;

    @Autowired
    private IOrganizationRepository organizationRepository;

    public OrgKeyService(MasterKeyManager masterKeyManager, IOrgKeyRepository repository) {
        this.masterKeyManager = masterKeyManager;
        this.repository = repository;
    }

    public void createOrgKey(Long orgId) {
        SecretKey perOrgKey = CryptoUtils.generateAESKey();

        byte[] encrypted = masterKeyManager.encryptWithRoot(perOrgKey.getEncoded());

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with id: " + orgId));

        repository.save(new OrgKey(org,encrypted));
    }

    public SecretKey getOrgKey(Long orgId) {
        OrgKey entity = repository.findByOrgId(orgId);

        // 1. uzmemo enkriptovan per-org key iz baze
        byte[] encrypted = entity.getEncryptedKey();

        // 2. dekriptujemo ga master key-em
        byte[] decrypted = masterKeyManager.decryptWithRoot(encrypted);

        // 3. pretvorimo u SecretKey
        return CryptoUtils.bytesToAesKey(decrypted);
    }
}
