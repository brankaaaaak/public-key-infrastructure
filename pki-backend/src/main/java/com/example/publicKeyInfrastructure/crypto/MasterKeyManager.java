package com.example.publicKeyInfrastructure.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class MasterKeyManager {
    private final SecretKey rootKey;

    public MasterKeyManager(@Value("${MASTER_KEY}") String rootKeyHex) {
        this.rootKey = CryptoUtils.hexToAesKey(rootKeyHex);
    }

    public byte[] encryptWithRoot(byte[] data) {
        return CryptoUtils.encryptAES(data, rootKey);
    }

    public byte[] decryptWithRoot(byte[] data) {
        return CryptoUtils.decryptAES(data, rootKey);
    }
}

