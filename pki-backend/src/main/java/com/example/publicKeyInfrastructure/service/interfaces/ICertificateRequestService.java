package com.example.publicKeyInfrastructure.service.interfaces;

import com.example.publicKeyInfrastructure.dto.CertificateRequestDTO;

public interface ICertificateRequestService {
    CertificateRequestDTO createRequest(CertificateRequestDTO request);
}
