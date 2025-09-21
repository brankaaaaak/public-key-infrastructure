package com.example.publicKeyInfrastructure.service.implementation;

import com.example.publicKeyInfrastructure.crypto.CryptoUtils;
import com.example.publicKeyInfrastructure.crypto.MasterKeyManager;
import com.example.publicKeyInfrastructure.data.CertificateIssuer;
import com.example.publicKeyInfrastructure.data.CertificateSubject;
import com.example.publicKeyInfrastructure.dto.CertificateDTO;
import com.example.publicKeyInfrastructure.enumeration.CertificateType;
import com.example.publicKeyInfrastructure.model.*;
import com.example.publicKeyInfrastructure.model.Certificate;
import com.example.publicKeyInfrastructure.repository.ICertificateRepository;
import com.example.publicKeyInfrastructure.repository.IOrgKeyRepository;
import com.example.publicKeyInfrastructure.service.interfaces.ICertificateService;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Random;

@Service
public class CertificateService implements ICertificateService {

    @Autowired
    ICertificateRepository certificateRepository;

    @Autowired
    private IOrgKeyRepository orgKeyRepository;

    @Autowired
    private MasterKeyManager masterKeyManager;

    @Override
    public CertificateDTO createCertificate(CertificateRequest certificateRequest) {
        CertificateSubject certificateSubject = createCertificateSubject(certificateRequest);
        CertificateIssuer certificateIssuer;
        byte[] encryptedPrivateKey;
        Certificate issuerCert;
        if(certificateRequest.getIssuer() ==null || certificateRequest.getCertificateType()==CertificateType.ROOT){
            certificateIssuer = new CertificateIssuer(
                    certificateSubject.getX500name(),
                    certificateSubject.getPrivateKey(),
                    certificateSubject.getPublicKey());
            encryptedPrivateKey = masterKeyManager.encryptWithRoot(certificateSubject.getPrivateKey().getEncoded());
            saveCertificateToDb(certificateRequest,certificateSubject,certificateIssuer,encryptedPrivateKey);
        }else{
            //to do i or ee
            // Uzima se issuer sertifikat iz baze
            issuerCert= certificateRepository.findById(certificateRequest.getIssuer().getId())
                    .orElseThrow(() -> new RuntimeException("Issuer certificate not found"));

            // Provera validnosti issuer-a
            if (!issuerCert.isValid() || issuerCert.isRevoked()) {
                throw new RuntimeException("Issuer certificate is not valid");
            }

            // Uzimamo OrgKey za organizaciju korisnika koji prima sertifikat
            OrgKey orgKey = orgKeyRepository.findByOrgId(issuerCert.getOrganization().getId());

            byte[] orgKeyBytes = masterKeyManager.decryptWithRoot(orgKey.getEncryptedKey());
            SecretKey decryptedOrgKey = CryptoUtils.bytesToSecretKey(orgKeyBytes);

            // Kreiramo CertificateIssuer objekat
            try {
                certificateIssuer = buildIssuerFromCertificate(issuerCert, decryptedOrgKey);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // Šifrujemo privatni ključ novog sertifikata sa org key
            encryptedPrivateKey = CryptoUtils.encryptAES(certificateSubject.getPrivateKey().getEncoded(), decryptedOrgKey);
            saveCertificateToDb(certificateRequest,certificateSubject,certificateIssuer,encryptedPrivateKey);
        }
        return null;
    }
    private Certificate saveCertificateToDb(
            CertificateRequest request,
            CertificateSubject subject,
            CertificateIssuer issuer,
            byte[] encryptedPrivateKey) {
        try {
            X509Certificate x509Certificate = createCertificateContent(subject, issuer, request.getCertificateType());

            // Root sertifikat nema issuer u bazi
            Certificate issuerCert = null;
            String issuerSerialNumber = null;
            User owner = null;
            Organization organization = null;

            if (request.getCertificateType() == CertificateType.ROOT) {
                // Root -> issuer = null, owner može biti null ili iz request-a
                issuerSerialNumber = null;
                owner = null;
                organization = null;
            } else {
                // Intermediate / End-Entity -> issuer info
                issuerCert = certificateRepository.findById(request.getIssuer().getId())
                        .orElseThrow(() -> new RuntimeException("Issuer certificate not found"));

                issuerSerialNumber = issuerCert.getSerialNumber();
                owner = issuerCert.getOwner();
                organization = issuerCert.getOrganization();
            }

            Certificate certificate = new Certificate(
                    request.getCertificateType(),
                    owner,
                    x509Certificate.getSerialNumber().toString(),
                    issuerSerialNumber,
                    x509Certificate.getNotBefore(),
                    x509Certificate.getNotAfter(),
                    false,
                    null,
                    organization,
                    encryptedPrivateKey,
                    subject.getPublicKey().getEncoded()
            );

            certificateRepository.save(certificate);

            // Opcionalno: upis u key store možeš dodati ovde

            return certificate;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create/save certificate", e);
        }
    }

    public CertificateIssuer buildIssuerFromCertificate(Certificate issuerCert, SecretKey orgDecryptedKey) throws Exception {
        CertificateIssuer issuer = new CertificateIssuer();

        byte[] decryptedBytes = CryptoUtils.decryptAES(issuerCert.getEncryptedPrivateKey(), orgDecryptedKey);
        PrivateKey privateKey = CryptoUtils.bytesToPrivateKey(decryptedBytes);

        // Uzme se javni ključ iz sertifikata (čuvati u bazi )
        PublicKey publicKey = CryptoUtils.bytesToPublicKey(issuerCert.getPublicKey());

        //Kreira se X500Name iz podataka o vlasniku
        X500Name x500Name = buildX500Name(issuerCert.getOwner());

        issuer.setPrivateKey(privateKey);
        issuer.setPublicKey(publicKey);
        issuer.setX500name(x500Name);

        return issuer;
    }

    @Override
    public Boolean checkingValidation(String serialNumber) throws ChangeSetPersister.NotFoundException {
        Certificate certificate = certificateRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new ChangeSetPersister.NotFoundException());
        if(certificate != null)
            return certificate.isValid() && !certificate.isRevoked();

        return false;
    }

    private CertificateSubject createCertificateSubject(CertificateRequest certificateRequest){
        KeyPair keyPairSubject = generateKeyPair();
        User user = certificateRequest.getSubject();

        Date startDate = new Date();

        LocalDateTime endDateTime = startDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .plusDays(certificateRequest.getDurationInDays());
        Date endDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());

        SecureRandom random = new SecureRandom();
        BigInteger serial = new BigInteger(64, random).abs(); // 64
        String serialNumber = serial.toString();

        X500Name subjectName = buildX500Name(user);

        return new CertificateSubject(
                keyPairSubject.getPublic(),
                keyPairSubject.getPrivate(),
                subjectName,
                serialNumber,
                startDate,
                endDate
        );
    }
    private KeyPair generateKeyPair(){
        try {

            KeyPairGenerator kpg = KeyPairGenerator
                    .getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            System.out.println("Keypair : " + kp);
            return kp;
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception thrown : " + e);

        }
        return null;
    }
    private X500Name buildX500Name(User user) {
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        builder.addRDN(BCStyle.CN, user.getUsername());
        builder.addRDN(BCStyle.SURNAME, user.getLastName());
        builder.addRDN(BCStyle.GIVENNAME, user.getName());
        builder.addRDN(BCStyle.E, user.getEmail());
        builder.addRDN(BCStyle.UID, user.getId().toString());
        return builder.build();
    }
    private X509Certificate createCertificateContent(CertificateSubject subject, CertificateIssuer issuer, CertificateType certType) throws OperatorCreationException, CertIOException, CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException {
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                .setProvider("BC")
                .build(issuer.getPrivateKey());

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer.getX500name(),
                new BigInteger(subject.getSerialNumber()),
                subject.getStartDate(),
                subject.getEndDate(),
                subject.getX500name(),
                subject.getPublicKey()
        );

        switch (certType) {
            case ROOT:
                certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
                certBuilder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));
                break;
            case INTERMEDIATE:
                certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(0));
                certBuilder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));
                break;
            case END:
                certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
                certBuilder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
                certBuilder.addExtension(Extension.extendedKeyUsage, false,
                        new ExtendedKeyUsage(new KeyPurposeId[]{KeyPurposeId.id_kp_clientAuth, KeyPurposeId.id_kp_serverAuth}));
                break;
            default:
                throw new IllegalArgumentException("Unknown cert type");
        }

        X509CertificateHolder certHolder = certBuilder.build(contentSigner);
        X509Certificate certificate = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);

        certificate.checkValidity(new Date());
        certificate.verify(issuer.getPublicKey());

        return certificate;
    }
}
