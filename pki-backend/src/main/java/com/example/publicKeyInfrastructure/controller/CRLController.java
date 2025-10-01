package com.example.publicKeyInfrastructure.controller;

import com.example.publicKeyInfrastructure.service.interfaces.ICertificateRevokeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crl")
@CrossOrigin(origins = "*")
public class CRLController {

    @Autowired
    private ICertificateRevokeService crlService;

    @GetMapping("/{caIdentifier}")
    public ResponseEntity<byte[]> getCRL(@PathVariable String caIdentifier) throws Exception {
            boolean isRoot = caIdentifier.equalsIgnoreCase("root"); // ili provera po nazivu JKS
            byte[] crlBytes = crlService.getCRLBytes(caIdentifier, isRoot);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/pkix-crl")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"crl_" + caIdentifier + ".crl\"")
                    .body(crlBytes);

    }
}
