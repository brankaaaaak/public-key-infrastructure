package com.example.publicKeyInfrastructure.service.interfaces;

import com.example.publicKeyInfrastructure.dto.CertificateDTO;
import com.example.publicKeyInfrastructure.dto.CertificateRequestDTO;
import com.example.publicKeyInfrastructure.dto.CreateCertificateRequestDTO;
import org.springframework.data.crossstore.ChangeSetPersister;

public interface ICertificateRequestService {
    CertificateRequestDTO createRequest(CreateCertificateRequestDTO request) throws ChangeSetPersister.NotFoundException;
    public CertificateDTO acceptRequest(Long id) throws ChangeSetPersister.NotFoundException;
}
