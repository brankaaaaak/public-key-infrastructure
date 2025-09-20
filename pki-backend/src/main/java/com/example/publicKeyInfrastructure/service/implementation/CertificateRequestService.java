package com.example.publicKeyInfrastructure.service.implementation;

import com.example.publicKeyInfrastructure.dto.CertificateDTO;
import com.example.publicKeyInfrastructure.dto.CertificateRequestDTO;
import com.example.publicKeyInfrastructure.dto.CreateCertificateRequestDTO;
import com.example.publicKeyInfrastructure.enumeration.CertificateType;
import com.example.publicKeyInfrastructure.enumeration.RequestType;
import com.example.publicKeyInfrastructure.model.Certificate;
import com.example.publicKeyInfrastructure.model.CertificateRequest;
import com.example.publicKeyInfrastructure.model.User;
import com.example.publicKeyInfrastructure.repository.ICertificateRequestRepository;
import com.example.publicKeyInfrastructure.repository.IUserRepository;
import com.example.publicKeyInfrastructure.service.interfaces.ICertificateRequestService;
import com.example.publicKeyInfrastructure.service.interfaces.ICertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CertificateRequestService implements ICertificateRequestService {

    @Autowired
    IUserRepository userRepository;

    @Autowired
    ICertificateRequestRepository certificateRequestRepository;

    @Autowired
    ICertificateService certificateService;

    @Override
    public CertificateRequestDTO createRequest(CreateCertificateRequestDTO request) throws ChangeSetPersister.NotFoundException {
        CertificateRequest newRequest = new CertificateRequest(request);
        Certificate issuer;
        if(newRequest.getCertificateType() == CertificateType.ROOT){
            issuer = null;
        }else{
            //to do -> when i or ee
            issuer = null;
        }
        newRequest.setIssuer(issuer);
        User subject = getLoggedUser();
        newRequest.setSubject(subject);
        boolean isAdmin = subject.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        if(isAdmin){
            newRequest.setRequestType(RequestType.ACCEPTED);
            //to do -> create certificate
        }
        newRequest = certificateRequestRepository.save(newRequest);
        return new CertificateRequestDTO(newRequest);
    }

    @Override
    public CertificateDTO acceptRequest(Long id) throws ChangeSetPersister.NotFoundException {
        CertificateRequest request = certificateRequestRepository.findById(id).orElseThrow(() ->
                new ChangeSetPersister.NotFoundException());
        //provjera validnosti ?
        if (!request.getRequestType().equals(RequestType.PENDING)){
            throw new IllegalArgumentException("Request type must be PENDING.");
        }
        if (request.getCertificateType() != CertificateType.ROOT && !certificateService.checkingValidation(request.getIssuer().getSerialNumber())){
            throw new IllegalArgumentException("Issuer certificate is not valid! This request cannot be accepted.");
        }
        request.setRequestType(RequestType.ACCEPTED);
        certificateRequestRepository.save(request);
        return certificateService.createCertificate(request);
    }

    private User getLoggedUser() throws ChangeSetPersister.NotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)     //for owner
                .orElseThrow(() -> new ChangeSetPersister.NotFoundException());
    }

}
