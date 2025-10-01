package com.example.publicKeyInfrastructure.controller;

import com.example.publicKeyInfrastructure.service.interfaces.ICertificateRevokeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crl")
public class CRLController {

    @Autowired
    private ICertificateRevokeService crlService;

    @GetMapping("/{orgId}/{caIdentifier}")
    public ResponseEntity<byte[]> getCRL(@PathVariable Long orgId,@PathVariable String caIdentifier) throws Exception {
            boolean isRoot = orgId==0;
            byte[] crlBytes = crlService.getCRLBytes(caIdentifier, isRoot,orgId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/pkix-crl")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"crl_" + caIdentifier + ".crl\"")
                    .body(crlBytes);

    }
}
