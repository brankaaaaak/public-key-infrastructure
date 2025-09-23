package com.example.publicKeyInfrastructure.service.interfaces;

import com.example.publicKeyInfrastructure.dto.CertificateDTO;
import com.example.publicKeyInfrastructure.dto.CertificateRequestDTO;
import com.example.publicKeyInfrastructure.dto.CertificateRequestViewDTO;
import com.example.publicKeyInfrastructure.dto.CreateCertificateRequestDTO;
import org.springframework.data.crossstore.ChangeSetPersister;

import java.util.List;

public interface ICertificateRequestService {
    CertificateRequestDTO createRequest(CreateCertificateRequestDTO request) throws Exception;
    public CertificateDTO acceptRequest(Long id) throws ChangeSetPersister.NotFoundException;
    List<CertificateRequestViewDTO> getPendingRequestsForLoggedOwner() throws Exception;
    List<CertificateRequestViewDTO> getPendingRequestsForAdmin() throws Exception;
    CertificateRequestViewDTO denyRequest(Long id, String reason);
}
