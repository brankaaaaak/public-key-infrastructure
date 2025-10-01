package com.example.publicKeyInfrastructure.service.interfaces;

public interface ICertificateRevokeService {
    public void revokeCertificateAndDescendants(String serialNumber, String reason);
    public byte[] getCRLBytes(String caIdentifier,boolean isRoot) throws Exception;
}
