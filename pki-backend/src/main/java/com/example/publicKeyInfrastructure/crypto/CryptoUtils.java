package com.example.publicKeyInfrastructure.crypto;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.io.pem.PemReader;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class CryptoUtils {

    public static SecretKey generateAESKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            return keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Error generating AES key", e);
        }
    }

    public static byte[] encryptAES(byte[] data, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    public static byte[] decryptAES(byte[] encryptedData, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }

    public static SecretKey hexToAesKey(String hex) {
        byte[] bytes = hexStringToByteArray(hex);
        return new SecretKeySpec(bytes, "AES");
    }

    public static SecretKey bytesToAesKey(byte[] bytes) {
        return new SecretKeySpec(bytes, "AES");
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
    public static SecretKey bytesToSecretKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes,"AES");
    }
    public static PrivateKey bytesToPrivateKey(byte[] keyBytes) throws Exception {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
    public static PublicKey bytesToPublicKey(byte[] keyBytes) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
    public static boolean isCA(X509Certificate cert) throws CertificateParsingException {
        // Čita BasicConstraints: ako je CA=true vraća true, inače false
        return cert.getBasicConstraints() != -1;
    }

        /**
         * Parsira PEM string CSR u PKCS10CertificationRequest objekat.
         */
        public static PKCS10CertificationRequest parseCsrFromPem(String pem) throws IOException {
            try (PemReader reader = new PemReader(new StringReader(pem))) {
                byte[] content = reader.readPemObject().getContent();
                return new PKCS10CertificationRequest(content);
            }
        }

        /**
         * Dobavlja javni ključ iz PKCS#10 CSR.
         * RSA ili EC ključ zavisi od generacije CSR-a.
         */
        public static PublicKey getPublicKeyFromCsr(PKCS10CertificationRequest csr) throws Exception {
            byte[] keyBytes = csr.getSubjectPublicKeyInfo().getEncoded();
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);

            // Ako koristiš RSA ključeve:
            KeyFactory kf = KeyFactory.getInstance("RSA");

            // Ako koristiš EC ključeve, promeni u: KeyFactory.getInstance("EC");
            return kf.generatePublic(spec);
        }

        /**
         * Dobavlja X500Name iz CSR-a.
         */
        public static X500Name getSubjectFromCsr(PKCS10CertificationRequest csr) {
            return csr.getSubject();
        }

}
