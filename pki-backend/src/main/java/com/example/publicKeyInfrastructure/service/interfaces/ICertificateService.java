package com.example.publicKeyInfrastructure.service.interfaces;

import com.example.publicKeyInfrastructure.dto.CertificateDTO;
import com.example.publicKeyInfrastructure.model.CertificateRequest;
import org.springframework.data.crossstore.ChangeSetPersister;

import java.util.List;

public interface ICertificateService {
    CertificateDTO createCertificate(CertificateRequest certificateRequest);
    public Boolean checkingValidation(String serialNumber) throws ChangeSetPersister.NotFoundException;
    List<CertificateDTO> getValidIntermediateCertificatesByOrganization(Long orgId);
    public CertificateDTO getValidRootCertificate();


}
