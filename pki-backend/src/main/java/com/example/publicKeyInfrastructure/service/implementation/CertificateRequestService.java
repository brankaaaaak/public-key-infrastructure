package com.example.publicKeyInfrastructure.service.implementation;

import com.example.publicKeyInfrastructure.dto.CertificateDTO;
import com.example.publicKeyInfrastructure.dto.CertificateRequestDTO;
import com.example.publicKeyInfrastructure.dto.CertificateRequestViewDTO;
import com.example.publicKeyInfrastructure.dto.CreateCertificateRequestDTO;
import com.example.publicKeyInfrastructure.enumeration.CertificateType;
import com.example.publicKeyInfrastructure.enumeration.RequestType;
import com.example.publicKeyInfrastructure.model.Certificate;
import com.example.publicKeyInfrastructure.model.CertificateRequest;
import com.example.publicKeyInfrastructure.model.User;
import com.example.publicKeyInfrastructure.repository.ICertificateRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CertificateRequestService implements ICertificateRequestService {

    @Autowired
    IUserRepository userRepository;

    @Autowired
    ICertificateRequestRepository certificateRequestRepository;

    @Autowired
    ICertificateService certificateService;

    @Autowired
    ICertificateRepository certificateRepository;

    @Override
    public CertificateRequestDTO createRequest(CreateCertificateRequestDTO request) throws Exception {
        CertificateRequest newRequest = new CertificateRequest(request);
        Certificate issuer;
        if(newRequest.getCertificateType() == CertificateType.ROOT){
            issuer = null;
        }else{
            issuer = certificateRepository.findBySerialNumber(request.getIssuer())
                    .orElseThrow(() -> new Exception("Certificate not found!"));
        }
        newRequest.setIssuer(issuer);
        User loggedUser=getLoggedUser();
        boolean isAdmin = loggedUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        boolean isCAUser=loggedUser.getRoles().stream()
                .anyMatch((r->r.getName().equals("ROLE_CA")));
        boolean isUser=loggedUser.getRoles().stream()
                .anyMatch((r->r.getName().equals("ROLE_USER")));
        User subject;
        if(isAdmin && CertificateType.valueOf(request.getCertificateType())!=CertificateType.ROOT )
            subject=userRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new Exception("User not found!"));
        else
            subject = getLoggedUser();
        newRequest.setSubject(subject);
        if(isAdmin){
            newRequest.setRequestType(RequestType.ACCEPTED);
            newRequest = certificateRequestRepository.save(newRequest);
            certificateService.createCertificate(newRequest);
        }
        else if(isCAUser) {
            if(newRequest.getCertificateType()==CertificateType.INTERMEDIATE || newRequest.getCertificateType()==CertificateType.END)
            {
                newRequest.setRequestType(RequestType.PENDING);
                newRequest = certificateRequestRepository.save(newRequest);
            }
        }
        else if(isUser){
            if(newRequest.getCertificateType()==CertificateType.END){
                newRequest.setRequestType(RequestType.PENDING);
                newRequest = certificateRequestRepository.save(newRequest);
            }
        }
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
    public List<CertificateRequestViewDTO> getPendingRequestsForLoggedOwner() throws Exception{
        Long ownerId = getLoggedUser().getId();
        List<CertificateRequest> requests = certificateRequestRepository
                .findPendingRequestsByOwnerCertificates(ownerId, RequestType.PENDING);

        return requests.stream()
                .map(cr -> new CertificateRequestViewDTO(cr))
                .toList();
    }
    public List<CertificateRequestViewDTO> getPendingRequestsForAdmin() throws Exception{
        List<CertificateRequest> requests = certificateRequestRepository
                .findPendingRequestsCertificates(RequestType.PENDING);

        return requests.stream()
                .map(cr -> new CertificateRequestViewDTO(cr))
                .toList();
    }
    @Override
    public CertificateRequestViewDTO denyRequest(Long id, String reason) {
        CertificateRequest request = certificateRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setRequestType(RequestType.REFUSED);
        request.setRefusalReason(reason);
        certificateRequestRepository.save(request);

        return new CertificateRequestViewDTO(request);
    }

    @Override
    public List<CertificateRequestViewDTO> getMineEECertificatesRequests() throws ChangeSetPersister.NotFoundException {
        User loggedUser = getLoggedUser();
        List<CertificateRequest> requests = this.certificateRequestRepository.findAllBySubjectAndCertificateType(loggedUser, CertificateType.END);
        return requests.stream()
                .map(CertificateRequestViewDTO::new)
                .collect(Collectors.toList());
    }
}
