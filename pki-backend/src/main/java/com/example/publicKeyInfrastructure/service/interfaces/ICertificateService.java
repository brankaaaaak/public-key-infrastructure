package com.example.publicKeyInfrastructure.service.interfaces;

import com.example.publicKeyInfrastructure.dto.CertificateDTO;
import com.example.publicKeyInfrastructure.dto.CertificateDetailsDTO;
import com.example.publicKeyInfrastructure.dto.CertificateTreeDTO;
import com.example.publicKeyInfrastructure.model.CertificateRequest;
import org.springframework.data.crossstore.ChangeSetPersister;

import java.util.List;

public interface ICertificateService {
    CertificateDTO createCertificate(CertificateRequest certificateRequest);
    public Boolean checkingValidation(String serialNumber) throws ChangeSetPersister.NotFoundException;
    List<CertificateDTO> getValidIntermediateCertificatesByOrganization() throws Exception;
    public CertificateDTO getValidRootCertificate();
    public byte[] generateKeystore(Long id) throws Exception;
    List<CertificateDTO> getMineEECertificates() throws ChangeSetPersister.NotFoundException;


    List<CertificateTreeDTO> getCertificateChainForLoggedUser() throws Exception;
    CertificateDetailsDTO getCertificateDetails(String serialNumber) ;
    public List<CertificateDTO> getAllValidCertificates() ;
    public List<CertificateDTO> getAllValidCertificatesWithoutRoot() ;
    public List<CertificateDTO> getDownloadTable() throws ChangeSetPersister.NotFoundException;

}
