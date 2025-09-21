package com.example.publicKeyInfrastructure.crypto;

import com.example.publicKeyInfrastructure.data.CertificateIssuer;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Component
public class KeystoreManager {
    private final KeyStore keyStore;
    private final char[] password;

    public KeystoreManager(@Value("${KEYSTORE_PASSWORD}") String passwordEnv) {
        if (passwordEnv == null || passwordEnv.isEmpty()) {
            throw new RuntimeException("Environment variable KEYSTORE_PASSWORD not set");
        }
        this.password = passwordEnv.toCharArray();

        try {
            this.keyStore = KeyStore.getInstance("JKS"); // inicijalizuje KeyStore objekat
            this.keyStore.load(null, password); // prazni KeyStore sa lozinkom
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new RuntimeException("Failed to initialize KeyStore", e);
        }
    }

    /** =================== HELPERS =================== */
    private char[] getPassword() {
        return password;
    }

    /** =================== ORG KEYSTORE FILE =================== */
    private String getOrgKeyStoreFile(Long organizationId) {
        String folder = "keystores"; // ili iz konfiguracije
        File dir = new File(folder);
        if (!dir.exists()) dir.mkdirs(); // kreira folder ako ne postoji
        return folder + File.separator + "org_" + organizationId + ".jks";
    }

    /** =================== WRITER =================== */
    public void write(String keyStoreFile, String alias, PrivateKey privateKey, Certificate certificate) {
        try {
            // load ili kreiraj novi keystore
            keyStore.load(keyStoreFile != null && new File(keyStoreFile).exists() ?
                    new FileInputStream(keyStoreFile) : null, getPassword());

            keyStore.setKeyEntry(alias, privateKey, getPassword(), new Certificate[]{certificate});
            keyStore.store(new FileOutputStream(keyStoreFile), getPassword());
        } catch (Exception e) {
            throw new RuntimeException("Failed to write to KeyStore", e);
        }
    }

    /** =================== WRITER ZA ORG =================== */
    public void writeForOrganization(Long orgId, String alias, PrivateKey privateKey, Certificate certificate) {
        String keyStoreFile = getOrgKeyStoreFile(orgId);
        write(keyStoreFile, alias, privateKey, certificate);
    }

    /** =================== READER =================== */
    public CertificateIssuer readIssuer(String keyStoreFile, String alias) {
        try {
            keyStore.load(new FileInputStream(keyStoreFile), getPassword());

            Certificate cert = keyStore.getCertificate(alias);
            if (cert == null) throw new RuntimeException("Certificate not found for alias: " + alias);

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, getPassword());
            if (privateKey == null) throw new RuntimeException("Private key not found for alias: " + alias);

            X500Name issuerName = new JcaX509CertificateHolder((X509Certificate) cert).getSubject();
            return new CertificateIssuer(issuerName,privateKey, cert.getPublicKey());

        } catch (Exception e) {
            throw new RuntimeException("Failed to read issuer from KeyStore", e);
        }
    }

    public CertificateIssuer readIssuerForOrg(Long orgId, String alias) {
        String keyStoreFile = getOrgKeyStoreFile(orgId);
        return readIssuer(keyStoreFile, alias);
    }

    public Certificate readCertificate(String keyStoreFile, String alias) {
        try {
            keyStore.load(new FileInputStream(keyStoreFile), getPassword());
            Certificate cert = keyStore.getCertificate(alias);
            if (cert == null) throw new RuntimeException("Certificate not found for alias: " + alias);
            return cert;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read certificate from KeyStore", e);
        }
    }

    public Certificate readCertificateForOrg(Long orgId, String alias) {
        String keyStoreFile = getOrgKeyStoreFile(orgId);
        return readCertificate(keyStoreFile, alias);
    }

    public PrivateKey readPrivateKey(String keyStoreFile, String alias) {
        try {
            keyStore.load(new FileInputStream(keyStoreFile), getPassword());
            Key key = keyStore.getKey(alias, getPassword());
            if (!(key instanceof PrivateKey privateKey))
                throw new RuntimeException("Key is not a private key");
            return privateKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read private key from KeyStore", e);
        }
    }

    public PrivateKey readPrivateKeyForOrg(Long orgId, String alias) {
        String keyStoreFile = getOrgKeyStoreFile(orgId);
        return readPrivateKey(keyStoreFile, alias);
    }

    /** =================== DOWNLOAD CERTIFICATE =================== */
    public void downloadCertificate(Certificate certificate, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(certificate.getEncoded());
        } catch (CertificateEncodingException | IOException e) {
            throw new RuntimeException("Failed to download certificate", e);
        }
    }

    /** =================== WRITER ZA ROOT =================== */
    private String getRootKeyStoreFile() {
        String folder = "keystores"; // relativno u odnosu na radni folder JVM-a
        File dir = new File(folder);
        if (!dir.exists()) dir.mkdirs();
        return folder + File.separator + "root.jks";
    }

    public void writeRoot(String alias, PrivateKey privateKey, Certificate certificate) {
        String keyStoreFile = getRootKeyStoreFile();
        write(keyStoreFile, alias, privateKey, certificate);
    }


}
