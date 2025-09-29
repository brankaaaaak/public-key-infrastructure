package com.example.publicKeyInfrastructure.service.interfaces;

import com.example.publicKeyInfrastructure.dto.CertificateDTO;
import com.example.publicKeyInfrastructure.dto.CertificateRequestViewDTO;
import com.example.publicKeyInfrastructure.model.CertificateRequest;
import org.springframework.data.crossstore.ChangeSetPersister;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;

public interface ICertificateService {
    CertificateDTO createCertificate(CertificateRequest certificateRequest);
    public Boolean checkingValidation(String serialNumber) throws ChangeSetPersister.NotFoundException;
    List<CertificateDTO> getValidIntermediateCertificatesByOrganization() throws Exception;
    public CertificateDTO getValidRootCertificate();
    public byte[] generateKeystore(Long id) throws Exception;
    List<CertificateDTO> getMineEECertificates() throws ChangeSetPersister.NotFoundException;

    public List<CertificateDTO> getAllValidCertificates() ;
    public List<CertificateDTO> getAllValidCertificatesWithoutRoot() ;
}
