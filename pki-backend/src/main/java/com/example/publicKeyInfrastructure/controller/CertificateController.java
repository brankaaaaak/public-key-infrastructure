package com.example.publicKeyInfrastructure.controller;

import com.example.publicKeyInfrastructure.dto.*;
import com.example.publicKeyInfrastructure.service.interfaces.ICertificateRequestService;
import com.example.publicKeyInfrastructure.service.interfaces.ICertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CA') or hasRole('ROLE_USER')")
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
    @PutMapping("/request/{id}/approve")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CA')")
    public ResponseEntity<?> approveRequest(@PathVariable Long id) {
        try{
            CertificateDTO dto = requestService.acceptRequest(id);
            return ResponseEntity.ok(dto);
        }
        catch (ChangeSetPersister.NotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Certificate request not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    @PutMapping("/request/{id}/deny")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CA')")
    public ResponseEntity<CertificateRequestViewDTO> denyRequest(@PathVariable Long id,
                                                                 @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        CertificateRequestViewDTO dto = requestService.denyRequest(id, reason);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/my-chain")
    public ResponseEntity<?> getMyCertificateChain() {
        try {
            CertificateTreeDTO tree = certificateService.getCertificateChainForLoggedCA();
            return ResponseEntity.ok(tree);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal server error"));
        }
    }

}
