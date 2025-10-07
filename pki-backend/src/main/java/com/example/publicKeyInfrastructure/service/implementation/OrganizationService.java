package com.example.publicKeyInfrastructure.service.implementation;

import com.example.publicKeyInfrastructure.repository.IOrganizationRepository;
import com.example.publicKeyInfrastructure.service.interfaces.IOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationService implements IOrganizationService {

    @Autowired
    IOrganizationRepository organizationRepository;

    @Override
    public List<String> getAllOrganizationNames() {
        return organizationRepository.findAllOrganizationNames();
    }
}
