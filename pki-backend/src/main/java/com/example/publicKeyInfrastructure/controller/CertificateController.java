package com.example.publicKeyInfrastructure.controller;

import com.example.publicKeyInfrastructure.dto.CertificateDTO;
import com.example.publicKeyInfrastructure.dto.CertificateRequestDTO;
import com.example.publicKeyInfrastructure.dto.CertificateRequestViewDTO;
import com.example.publicKeyInfrastructure.dto.CreateCertificateRequestDTO;
import com.example.publicKeyInfrastructure.service.interfaces.ICertificateRequestService;
import com.example.publicKeyInfrastructure.service.interfaces.ICertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/certificate")
@CrossOrigin(origins = "https://localhost:4200")
public class CertificateController {

    @Autowired
    private ICertificateRequestService requestService;

    @Autowired
    private ICertificateService certificateService;

    @PostMapping(value="/request", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CertificateRequestDTO> createRequest(@RequestBody CreateCertificateRequestDTO dto) throws Exception {
        CertificateRequestDTO created = requestService.createRequest(dto);
        return ResponseEntity.ok(created);
    }
    @GetMapping("/organization/intermediate")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CA')")
    public ResponseEntity<List<CertificateDTO>> getValidIntermediateCertificates() {
        try{
            return ResponseEntity.ok(certificateService.getValidIntermediateCertificatesByOrganization());
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    // GET valid root certificate
    @GetMapping("/root")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CA')")
    public ResponseEntity<CertificateDTO> getValidRootCertificate() {
        return ResponseEntity.ok(certificateService.getValidRootCertificate());
    }
    @GetMapping("/admin-requests/pending")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<CertificateRequestViewDTO>> getPendingRequestsAdmin() {
        try {
            List<CertificateRequestViewDTO> dtos = requestService.getPendingRequestsForAdmin();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
    @GetMapping("/ca-requests/pending")
    @PreAuthorize("hasRole('ROLE_CA')")
    public ResponseEntity<List<CertificateRequestViewDTO>> getPendingRequestsCA() {
        try {
            List<CertificateRequestViewDTO> dtos = requestService.getPendingRequestsForLoggedOwner();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.emptyList());
        }

    }
}
