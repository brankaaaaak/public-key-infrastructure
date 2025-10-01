package com.example.publicKeyInfrastructure.crypto;

import com.example.publicKeyInfrastructure.data.CertificateIssuer;
import com.example.publicKeyInfrastructure.repository.ICertificateRepository;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
@Component
public class KeystoreManager {

    private final char[] password;

    public KeystoreManager(@Value("${KEYSTORE_PASSWORD}") String passwordEnv) {
        if (passwordEnv == null || passwordEnv.isEmpty()) {
            throw new RuntimeException("Environment variable KEYSTORE_PASSWORD not set");
        }
        this.password = passwordEnv.toCharArray();
    }

    /** =================== HELPERS =================== */
    private char[] getPassword() {
        return password;
    }

    private String getOrgKeyStoreFile(Long organizationId) {
        String folder = "keystores";
        File dir = new File(folder);
        if (!dir.exists()) dir.mkdirs();
        return folder + File.separator + "org_" + organizationId + ".jks";
    }

    private String getRootKeyStoreFile() {
        String folder = "keystores";
        File dir = new File(folder);
        if (!dir.exists()) dir.mkdirs();
        return folder + File.separator + "root.jks";
    }

    private KeyStore loadKeyStore(String keyStoreFile) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        File file = new File(keyStoreFile);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                ks.load(fis, getPassword());
            }
        } else {
            ks.load(null, getPassword()); // novi prazni keystore
        }
        return ks;
    }

    /** =================== WRITE =================== */
    private void write(String keyStoreFile, String alias, PrivateKey privateKey, X509Certificate[] chain) {
        try {
            KeyStore ks = loadKeyStore(keyStoreFile);
            ks.setKeyEntry(alias, privateKey, getPassword(), chain);
            try (FileOutputStream fos = new FileOutputStream(keyStoreFile)) {
                ks.store(fos, getPassword());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to write to KeyStore", e);
        }
    }

    public void writeForOrganization(Long orgId, String alias, PrivateKey privateKey, X509Certificate[] chain) {
        write(getOrgKeyStoreFile(orgId), alias, privateKey, chain);
    }

    private void writeCertificateOnly(String keyStoreFile, String alias, X509Certificate certificate) {
        try {
            KeyStore ks = loadKeyStore(keyStoreFile);
            ks.setCertificateEntry(alias, certificate);
            try (FileOutputStream fos = new FileOutputStream(keyStoreFile)) {
                ks.store(fos, getPassword());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to write certificate to KeyStore", e);
        }
    }

    public void writeCertificateForOrganization(Long orgId, String alias, X509Certificate certificate) {
        writeCertificateOnly(getOrgKeyStoreFile(orgId), alias, certificate);
    }

    public void writeRoot(String alias, PrivateKey privateKey, X509Certificate certificate) {
        write(getRootKeyStoreFile(), alias, privateKey, new X509Certificate[]{certificate});
    }

    /** =================== READ =================== */
    private Certificate readCertificate(String keyStoreFile, String alias) {
        try {
            KeyStore ks = loadKeyStore(keyStoreFile);
            Certificate cert = ks.getCertificate(alias);
            if (cert == null)
                throw new RuntimeException("Certificate not found in KeyStore for alias: " + alias);
            if (!(cert instanceof X509Certificate))
                throw new RuntimeException("Certificate is not X509 type for alias: " + alias);
            return cert;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read certificate from KeyStore: " + alias, e);
        }
    }

    private PrivateKey readPrivateKey(String keyStoreFile, String alias) {
        try {
            KeyStore ks = loadKeyStore(keyStoreFile);
            Key key = ks.getKey(alias, getPassword());
            if (!(key instanceof PrivateKey privateKey))
                throw new RuntimeException("Key is not a private key for alias: " + alias);
            return privateKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read private key from KeyStore: " + alias, e);
        }
    }

    public X509Certificate readOrgCertificate(Long orgId, String alias) {
        return (X509Certificate) readCertificate(getOrgKeyStoreFile(orgId), alias);
    }

    public PrivateKey readOrgPrivateKey(Long orgId, String alias) {
        return readPrivateKey(getOrgKeyStoreFile(orgId), alias);
    }

    public CertificateIssuer readOrgIssuer(Long orgId, String alias) {
        try {
            KeyStore ks = loadKeyStore(getOrgKeyStoreFile(orgId));
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            if (cert == null) throw new RuntimeException("Certificate not found for alias: " + alias);
            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, getPassword());
            if (privateKey == null) throw new RuntimeException("Private key not found for alias: " + alias);
            X500Name issuerName = new JcaX509CertificateHolder(cert).getSubject();
            return new CertificateIssuer(issuerName, privateKey, cert.getPublicKey());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read issuer from KeyStore", e);
        }
    }

    public X509Certificate readRootCertificate(String alias) {
        return (X509Certificate) readCertificate(getRootKeyStoreFile(), alias);
    }

    public PrivateKey readRootPrivateKey(String alias) {
        return readPrivateKey(getRootKeyStoreFile(), alias);
    }

    public CertificateIssuer readRootIssuer(String alias) {
        try {
            KeyStore ks = loadKeyStore(getRootKeyStoreFile());
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            if (cert == null) throw new RuntimeException("Certificate not found for alias: " + alias);
            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, getPassword());
            if (privateKey == null) throw new RuntimeException("Private key not found for alias: " + alias);
            X500Name issuerName = new JcaX509CertificateHolder(cert).getSubject();
            return new CertificateIssuer(issuerName, privateKey, cert.getPublicKey());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read issuer from KeyStore", e);
        }
    }

    /** =================== DOWNLOAD =================== */
    public void downloadCertificate(Certificate certificate, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(certificate.getEncoded());
        } catch (CertificateEncodingException | IOException e) {
            throw new RuntimeException("Failed to download certificate", e);
        }
    }
}
