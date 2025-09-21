package com.example.publicKeyInfrastructure.controller;

import com.example.publicKeyInfrastructure.dto.CertificateRequestDTO;
import com.example.publicKeyInfrastructure.dto.CreateCertificateRequestDTO;
import com.example.publicKeyInfrastructure.service.interfaces.ICertificateRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificate")
@CrossOrigin(origins = "http://localhost:4200")
public class CertificateController {

    @Autowired
    private ICertificateRequestService requestService;

    @PostMapping(value="/request", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CertificateRequestDTO> createRequest(@RequestBody CreateCertificateRequestDTO dto) throws Exception {
        CertificateRequestDTO created = requestService.createRequest(dto);
        return ResponseEntity.ok(created);
    }

}
