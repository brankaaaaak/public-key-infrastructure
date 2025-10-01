package com.example.publicKeyInfrastructure.service.interfaces;

public interface ICertificateRevokeService {
    public void revokeCertificateAndDescendants(String serialNumber, String reason);
}
