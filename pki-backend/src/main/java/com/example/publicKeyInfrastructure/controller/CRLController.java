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

    @GetMapping("/{caIdentifier}/{alias}")
    public ResponseEntity<byte[]> getCRL(@PathVariable String caIdentifier,@PathVariable String alias) throws Exception {
            boolean isRoot = caIdentifier.equalsIgnoreCase("root"); // ili provera po nazivu JKS
            byte[] crlBytes = crlService.getCRLBytes(caIdentifier, isRoot,alias);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/pkix-crl")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"crl_" + caIdentifier + ".crl\"")
                    .body(crlBytes);

    }
}
