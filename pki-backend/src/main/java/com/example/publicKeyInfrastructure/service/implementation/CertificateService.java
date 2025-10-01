package com.example.publicKeyInfrastructure.service.implementation;

import com.example.publicKeyInfrastructure.crypto.CryptoUtils;
import com.example.publicKeyInfrastructure.crypto.KeystoreManager;
import com.example.publicKeyInfrastructure.crypto.MasterKeyManager;
import com.example.publicKeyInfrastructure.data.CertificateIssuer;
import com.example.publicKeyInfrastructure.data.CertificateSubject;
import com.example.publicKeyInfrastructure.dto.CertificateDTO;
import com.example.publicKeyInfrastructure.enumeration.CertificateType;
import com.example.publicKeyInfrastructure.model.*;
import com.example.publicKeyInfrastructure.model.Certificate;
import com.example.publicKeyInfrastructure.repository.ICertificateRepository;
import com.example.publicKeyInfrastructure.repository.ICertificateRequestRepository;
import com.example.publicKeyInfrastructure.repository.IOrgKeyRepository;
import com.example.publicKeyInfrastructure.repository.IUserRepository;
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
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import org.springframework.security.access.AccessDeniedException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CertificateService implements ICertificateService {

    @Autowired
    ICertificateRepository certificateRepository;

    @Autowired
    private IOrgKeyRepository orgKeyRepository;

    @Autowired
    private MasterKeyManager masterKeyManager;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private KeystoreManager keystoreManager;

    @Autowired
    private ICertificateRequestRepository certificateRequestRepository;

    @Value("${KEYSTORE_PASSWORD}")
    private String keystorePassword;

    @Override
    public CertificateDTO createCertificate(CertificateRequest certificateRequest){
        CertificateSubject certificateSubject = createCertificateSubject(certificateRequest);
        CertificateIssuer certificateIssuer;
        byte[] encryptedPrivateKey;
        Certificate issuerCert=null;
        if(certificateRequest.getIssuer() ==null || certificateRequest.getCertificateType()==CertificateType.ROOT){
            certificateIssuer = new CertificateIssuer(
                    certificateSubject.getX500name(),
                    certificateSubject.getPrivateKey(),
                    certificateSubject.getPublicKey());
            encryptedPrivateKey = masterKeyManager.encryptWithRoot(certificateSubject.getPrivateKey().getEncoded());
            if(validCertificateCreation(certificateRequest,issuerCert))
                saveCertificateToDb(certificateRequest,certificateSubject,certificateIssuer,encryptedPrivateKey);
        }else{

            // Uzima se issuer sertifikat iz baze
            issuerCert= certificateRepository.findById(certificateRequest.getIssuer().getId())
                    .orElseThrow(() -> new RuntimeException("Issuer certificate not found"));

            // Provera validnosti issuer-a
            if (!issuerCert.isValid() || issuerCert.isRevoked()) {
                throw new RuntimeException("Issuer certificate is not valid");
            }
            SecretKey decryptedOrgKeyIssuer;
            // Uzimamo OrgKey za issuera
            if(issuerCert.getOrganization()!=null) {
                if(orgKeyRepository.findByOrgId(issuerCert.getOrganization().getId())==null){
                    try{
                    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                    keyGen.init(256);
                    SecretKey newMasterKey = keyGen.generateKey();

                    // Šifruj sa root key
                    byte[] encryptedKey =masterKeyManager.encryptWithRoot(newMasterKey.getEncoded());

                    // Sačuvaj u bazu
                    OrgKey key = new OrgKey();
                    key.setOrganization(issuerCert.getOrganization());
                    key.setEncryptedKey(encryptedKey);
                    orgKeyRepository.save(key);
                    }
                    catch (Exception e){

                    }
                }
                OrgKey key = orgKeyRepository.findByOrgId(certificateRequest.getSubject().getOrganization().getId());
                byte[] orgKeyBytes = masterKeyManager.decryptWithRoot(key.getEncryptedKey());
                decryptedOrgKeyIssuer = CryptoUtils.bytesToSecretKey(orgKeyBytes);
            }
            else{
                //Ukoliko je issuer root a nema orgnizaciju
                decryptedOrgKeyIssuer = masterKeyManager.getRootKey();
            }
            // Kreiramo CertificateIssuer objekat
            try {
                certificateIssuer = buildIssuerFromCertificate(issuerCert, decryptedOrgKeyIssuer);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // Novi deo: End Entity CSR scenario
            if(certificateRequest.getCertificateType() == CertificateType.END &&
                    certificateRequest.getCsrPem() != null && !certificateRequest.getCsrPem().isEmpty()) {

                // poziv helper metode koja parsira CSR i kreira sertifikat
                return createEndEntityFromCsr(certificateRequest, certificateIssuer);
            }
            // Šifrujemo privatni ključ novog sertifikata sa org key
            OrgKey key = orgKeyRepository.findByOrgId(certificateRequest.getSubject().getOrganization().getId());
            byte[] orgKeyBytes = masterKeyManager.decryptWithRoot(key.getEncryptedKey());
            SecretKey decryptedOrgKey = CryptoUtils.bytesToSecretKey(orgKeyBytes);
            encryptedPrivateKey = CryptoUtils.encryptAES(certificateSubject.getPrivateKey().getEncoded(), decryptedOrgKey);
            if(validCertificateCreation(certificateRequest,issuerCert))
                saveCertificateToDb(certificateRequest,certificateSubject,certificateIssuer,encryptedPrivateKey);
        }
        return null;
    }

    private CertificateDTO createEndEntityFromCsr(CertificateRequest certificateRequest, CertificateIssuer certificateIssuer) {
        try {
            // 1. Parsiraj CSR iz PEM stringa
            PKCS10CertificationRequest csr = CryptoUtils.parseCsrFromPem(certificateRequest.getCsrPem());

            // 2. Uzimamo javni ključ iz CSR-a
            PublicKey subjectPublicKey = CryptoUtils.getPublicKeyFromCsr(csr);

            // 3. Kreiramo CertificateSubject (privatni ključ ostaje kod korisnika)
            CertificateSubject subject = createCertificateSubjectFromCsr(certificateRequest,csr);

            // 4. Potpiši sertifikat sa issuer-om i dodaj ekstenzije
            X509Certificate signedCert = createCertificateContent(
                    subject,
                    certificateIssuer,
                    certificateRequest.getCertificateType(),
                    certificateRequest.getSubject().getOrganization()
            );
            // 5. Sačuvaj sertifikat u bazi (bez privatnog ključa)
            saveCertificateToDb(certificateRequest, subject, certificateIssuer, null);

            // 6. Vrati DTO sa potpisanim sertifikatom
            return null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create End Entity certificate from CSR", e);
        }
    }

    private Certificate saveCertificateToDb(
            CertificateRequest request,
            CertificateSubject subject,
            CertificateIssuer issuer,
            byte[] encryptedPrivateKey) {
            try {
                X509Certificate x509Certificate = createCertificateContent(subject, issuer, request.getCertificateType(),request.getSubject().getOrganization());

                // Root sertifikat nema issuer u bazi
                Certificate issuerCert = null;
                String issuerSerialNumber = null;
                User owner = null;
                Organization organization = null;

                if (request.getCertificateType() == CertificateType.ROOT) {
                    // Root -> issuer = null, owner može biti null ili iz request-a
                    issuerSerialNumber = null;
                    owner = request.getSubject();
                    organization = null;
                    // Upis u root keystore
                    keystoreManager.writeRoot(subject.getSerialNumber(), subject.getPrivateKey(), x509Certificate);

                } else {
                    // Intermediate / End-Entity -> issuer info
                    issuerCert = certificateRepository.findById(request.getIssuer().getId())
                            .orElseThrow(() -> new RuntimeException("Issuer certificate not found"));

                    issuerSerialNumber = issuerCert.getSerialNumber();
                    owner =request.getSubject();
                    String alias="";
                    Long orgId;
                    if(issuerCert.getOrganization()!=null) {// dodato radi lakseg filtriranja kasnije
                        organization = issuerCert.getOrganization();
                        orgId=organization.getId();
                    }
                    else {
                        organization = owner.getOrganization();
                        orgId=organization.getId();
                    }
                    alias=subject.getSerialNumber();

                    // Pravimo chain pre upisa
                    X509Certificate[] chain = buildCertificateChainForKeystore(x509Certificate,issuerCert);

                    keystoreManager.writeForOrganization(orgId, alias, subject.getPrivateKey(), chain);
                    if(encryptedPrivateKey==null)
                        keystoreManager.writeCertificateForOrganization(orgId,alias,x509Certificate);
                }

                Certificate certificate = new Certificate(
                        request,
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

                return certificate;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create/save certificate", e);
            }
    }
    private boolean validCertificateCreation(CertificateRequest request, Certificate issuer) {
        // issuer ne sme biti END_ENTITY
        if (issuer != null && issuer.getCertificateType() == CertificateType.END) {
            return false;
        }

        // ROOT može da se kreira samo ako još ne postoji nijedan ROOT
        if (request.getCertificateType() == CertificateType.ROOT) {
            boolean rootExists = certificateRepository.existsByCertificateType(CertificateType.ROOT);
            if (rootExists) {
                return false; // već postoji jedan root → ne dozvoljavamo novi
            }
            // takođe ROOT ne treba imati issuer
            if (issuer != null) {
                return false;
            }
        }

        return true;
    }
    public CertificateIssuer buildIssuerFromCertificate(Certificate issuerCert, SecretKey orgDecryptedKey) throws Exception {
        CertificateIssuer issuer = new CertificateIssuer();
        byte[] decryptedBytes ;
        PrivateKey privateKey;
        decryptedBytes = CryptoUtils.decryptAES(issuerCert.getEncryptedPrivateKey(),orgDecryptedKey);
        privateKey = CryptoUtils.bytesToPrivateKey(decryptedBytes);


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
                endDate,
                certificateRequest.getKeyUsages(),
                certificateRequest.getExtendedKeyUsages(),
                certificateRequest.getCsrPem()
        );
    }
    private CertificateSubject createCertificateSubjectFromCsr(CertificateRequest certificateRequest, PKCS10CertificationRequest csr) throws ChangeSetPersister.NotFoundException {
        // Dobavi javni ključ iz CSR-a
        PublicKey publicKey;
        try {
            publicKey = CryptoUtils.getPublicKeyFromCsr(csr);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract public key from CSR", e);
        }
        User loggedUser = getLoggedUser(); // ili iz SecurityContext-a
        //X500Name originalName = CryptoUtils.getSubjectFromCsr(csr);

        // X500Name iz CSR-a
        X500Name subjectName = buildX500Name(getLoggedUser());

        // Start i end date
        Date startDate = new Date();
        LocalDateTime endDateTime = startDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .plusDays(certificateRequest.getDurationInDays());
        Date endDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());

        // Serial number
        SecureRandom random = new SecureRandom();
        BigInteger serial = new BigInteger(64, random).abs();
        String serialNumber = serial.toString();

        return new CertificateSubject(
                publicKey,
                null, // nema privatnog ključa
                subjectName,
                serialNumber,
                startDate,
                endDate,
                certificateRequest.getKeyUsages(),
                certificateRequest.getExtendedKeyUsages(),
                certificateRequest.getCsrPem()
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
    private X509Certificate createCertificateContent(CertificateSubject subject, CertificateIssuer issuer, CertificateType certType,Organization org) throws OperatorCreationException, CertIOException, CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException {
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
                certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
                certBuilder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));
                break;
            case END:
                certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false)); // End entity nema CA prava

                if (subject.getKeyUsages() != null) {
                    // Filtriraju se nedozvoljene KeyUsage bitove za end-entity
                    List<String> filteredUsages = subject.getKeyUsages().stream()
                            .filter(ku -> !ku.equalsIgnoreCase("keyCertSign") && !ku.equalsIgnoreCase("cRLSign"))
                            .toList();

                    int keyUsageBits = buildKeyUsageBits(filteredUsages);
                    certBuilder.addExtension(Extension.keyUsage, true, new KeyUsage(keyUsageBits));
                }

                if (subject.getExtendedKeyUsages() != null) {
                    List<KeyPurposeId> ekuList = buildExtendedKeyUsages(subject.getExtendedKeyUsages());
                    certBuilder.addExtension(Extension.extendedKeyUsage, false,
                            new ExtendedKeyUsage(ekuList.toArray(new KeyPurposeId[0])));
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown cert type");
        }
        try {
            String crlUrl = getCRLUrl(certType,org);

            DistributionPointName distPointName = new DistributionPointName(
                    new GeneralNames(new GeneralName(GeneralName.uniformResourceIdentifier, crlUrl+"/"+subject.getSerialNumber()))
            );

            DistributionPoint distPoint = new DistributionPoint(distPointName, null, null);
            CRLDistPoint crlDistPoints = new CRLDistPoint(new DistributionPoint[]{distPoint});

            certBuilder.addExtension(Extension.cRLDistributionPoints, false, crlDistPoints);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        X509CertificateHolder certHolder = certBuilder.build(contentSigner);
        X509Certificate certificate = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);

        certificate.checkValidity(new Date());
        certificate.verify(issuer.getPublicKey());

        return certificate;
    }
    private String getCRLUrl(CertificateType certType,Organization org) throws ChangeSetPersister.NotFoundException {
        String baseUrl = "https://localhost:8443/api/crl";
        // primer: root, intermediate po organizaciji
        if (certType == CertificateType.ROOT && org==null) {
            return baseUrl+"/root";
        } else  {
            // možeš koristiti serialNumber organizacije ili issuer
            return baseUrl+"/org_" + org.getId();
        }
    }

    private User getLoggedUser() throws ChangeSetPersister.NotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)     //for owner
                .orElseThrow(() -> new ChangeSetPersister.NotFoundException());
    }
    @Override
    public List<CertificateDTO> getValidIntermediateCertificatesByOrganization() throws Exception {
        Long organizationId=getLoggedUser().getOrganization().getId();
        List<Certificate> certificates = certificateRepository.findValidIntermediateByOrganization(organizationId);

        return certificates.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    @Override
    public CertificateDTO getValidRootCertificate() {
        Certificate root = certificateRepository
                .findValidRootCertificate()
                .orElseThrow(() -> new RuntimeException("Root certificate not found"));
        return mapToDTO(root);
    }
    @Override
    public List<CertificateDTO> getAllValidCertificates() {
        List<CertificateDTO> allCertificates = new ArrayList<>();

        // Validni root
        certificateRepository.findValidRootCertificate()
                .ifPresent(root -> allCertificates.add(mapToDTO(root)));

        // Validni intermediate
        List<Certificate> intermediates = certificateRepository.findValidIntermediate();
        intermediates.forEach(intermediate -> allCertificates.add(mapToDTO(intermediate)));

        return allCertificates;
    }
    @Override
    public List<CertificateDTO> getAllValidCertificatesWithoutRoot() {
        List<CertificateDTO> allCertificates = new ArrayList<>();

        // Validni intermediate
        List<Certificate> intermediates = certificateRepository.findValidIntermediate();
        intermediates.forEach(intermediate -> allCertificates.add(mapToDTO(intermediate)));

        return allCertificates;
    }

    private CertificateDTO mapToDTO(Certificate certificate) {
        return new CertificateDTO(certificate);
    }
    // build chain for keystore
    private X509Certificate[] buildCertificateChainForKeystore(X509Certificate newCert, Certificate issuerCert) throws Exception {
        List<X509Certificate> chain = new ArrayList<>();

        // Dodajemo novi sertifikat na početak
        chain.add(newCert);

        // Dodajemo sve issuer sertifikate do root-a
        Certificate current = issuerCert;
        while (current != null) {
            if(current.getCertificateType()!=CertificateType.ROOT) {
                X509Certificate x509 = keystoreManager.readOrgCertificate(current.getOrganization().getId(), current.getSerialNumber());
                chain.add(x509);
            }
            else{
                X509Certificate x509 = keystoreManager.readRootCertificate(current.getSerialNumber());
                chain.add(x509);
            }
            if (current.getIssuerSerialNumber() != null) {
                current = certificateRepository.findBySerialNumber(current.getIssuerSerialNumber())
                        .orElseThrow(() -> new RuntimeException("Issuer not found for serial: " ));
            } else {
                current = null; // root reached
            }
        }

        return chain.toArray(new X509Certificate[0]);
    }


    private int buildKeyUsageBits(List<String> keyUsages) {
        int bits = 0;
        for (String usage : keyUsages) {
            switch (usage.toUpperCase()) {
                case "DIGITAL SIGNATURE": bits |= KeyUsage.digitalSignature; break;
                case "NON REPUDIATION": bits |= KeyUsage.nonRepudiation; break;
                case "KEY ENCIPHERMENT": bits |= KeyUsage.keyEncipherment; break;
                case "DATA ENCIPHERMENT": bits |= KeyUsage.dataEncipherment; break;
                case "KEY AGREEMENT": bits |= KeyUsage.keyAgreement; break;
                case "ENCIPHER ONLY": bits |= KeyUsage.encipherOnly; break;
                case "DECIPHER ONLY": bits |= KeyUsage.decipherOnly; break;
            }
        }
        return bits;
    }

    private List<KeyPurposeId> buildExtendedKeyUsages(List<String> extendedKeyUsages) {
        List<KeyPurposeId> ekuList = new ArrayList<>();
        for (String eku : extendedKeyUsages) {
            switch (eku.toUpperCase()) {
                case "SERVER AUTHENTICATION": ekuList.add(KeyPurposeId.id_kp_serverAuth); break;
                case "CLIENT AUTHENTICATION": ekuList.add(KeyPurposeId.id_kp_clientAuth); break;
                case "CODE SIGNING": ekuList.add(KeyPurposeId.id_kp_codeSigning); break;
                case "EMAIL PROTECTION": ekuList.add(KeyPurposeId.id_kp_emailProtection); break;
                case "TIME STAMPING": ekuList.add(KeyPurposeId.id_kp_timeStamping); break;
                case "OCSP SIGNING": ekuList.add(KeyPurposeId.id_kp_OCSPSigning); break;
                default: throw new IllegalArgumentException("Unknown EKU: " + eku);
            }
        }
        return ekuList;
    }
    @Override
    public byte[] generateKeystore(Long id) throws Exception {
        User loggedUser = getLoggedUser();

        Certificate cert = certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificate not found for this request"));

        if (cert.isRevoked()) {
            throw new IllegalStateException("Certificate has been revoked and cannot be downloaded.");
        }
        Date now = new Date();
        if (now.before(cert.getValidFrom()) || now.after(cert.getValidTo())) {
            throw new IllegalStateException("Certificate is expired and cannot be downloaded.");
        }

        if (cert.getCertificateType() == CertificateType.ROOT) {
            throw new AccessDeniedException("Root certificates cannot be downloaded.");
        }

        // ADMIN -> sve dozvoljeno
        if (loggedUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"))) {
            return buildKeystore(cert);
        }
        // USER -> samo svoje
        if (loggedUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_USER"))) {
            if (!cert.getOwner().getId().equals(loggedUser.getId())) {
                throw new AccessDeniedException("You are not allowed to download this certificate");
            }
            return buildKeystore(cert);
        }
        // CA -> svoje + koje je potpisao (rekurzivno)
        if (loggedUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_CA"))) {
            // da li je vlasnik
            if (cert.getOwner().getId().equals(loggedUser.getId())) {
                return buildKeystore(cert);
            }
            // ili je u lancu potpisivanja
            List<Certificate> allowed = new ArrayList<>();
            List<Certificate> owned = certificateRepository.findAllByOwner(loggedUser);
            for (Certificate ownedCert : owned) {
                allowed.addAll(getSignedCertificatesRecursive(ownedCert.getSerialNumber()));
            }
            boolean hasRight = allowed.stream().anyMatch(c -> c.getId().equals(cert.getId()));
            if (!hasRight) {
                throw new AccessDeniedException("You are not allowed to download this certificate");
            }
            return buildKeystore(cert);
        }
        throw new AccessDeniedException("Unauthorized role");
    }
    private byte[] buildKeystore(Certificate cert) throws Exception {
        // rekonstrukcija privatnog i javnog
        OrgKey key = orgKeyRepository.findByOrgId(cert.getOwner().getOrganization().getId());
        byte[] orgKeyBytes = masterKeyManager.decryptWithRoot(key.getEncryptedKey());
        SecretKey decryptedOrgKeyIssuer = CryptoUtils.bytesToSecretKey(orgKeyBytes);
        byte[] decryptedPrivateKeyBytes = CryptoUtils.decryptAES(cert.getEncryptedPrivateKey(), decryptedOrgKeyIssuer);
        PrivateKey privateKey = CryptoUtils.bytesToPrivateKey(decryptedPrivateKeyBytes);

        PublicKey publicKey = CryptoUtils.bytesToPublicKey(cert.getPublicKey());

        X509Certificate x509 = keystoreManager.readOrgCertificate(cert.getOwner().getOrganization().getId(), cert.getSerialNumber());

        KeyStore downloadKs = KeyStore.getInstance("PKCS12");
        downloadKs.load(null, keystorePassword.toCharArray());

        downloadKs.setKeyEntry(
                "cert-" + cert.getSerialNumber(),
                privateKey,
                keystorePassword.toCharArray(),
                new java.security.cert.Certificate[]{x509}
        );

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        downloadKs.store(bos, keystorePassword.toCharArray());

        return bos.toByteArray();
    }


    @Override
    public List<CertificateDTO> getMineEECertificates() throws ChangeSetPersister.NotFoundException {
        User loggedUser = getLoggedUser();
        List<Certificate> requests = this.certificateRepository.findAllByOwner(loggedUser);
        return requests.stream()
                .map(CertificateDTO::new)
                .collect(Collectors.toList());
    }
    @Override
    public List<CertificateDTO> getDownloadTable() throws ChangeSetPersister.NotFoundException {
        User loggedUser = getLoggedUser();
        if (loggedUser.getRoles().stream().anyMatch((r->r.getName().equals("ROLE_CA")))){
            List<Certificate> owned = certificateRepository.findAllByOwner(loggedUser);
            List<Certificate> all = new ArrayList<>(owned);
            for (Certificate cert : owned) {
                all.addAll(getSignedCertificatesRecursive(cert.getSerialNumber()));
            }
            return all.stream()
                    .distinct() // da ne dođe do duplikata
                    .map(CertificateDTO::new)
                    .collect(Collectors.toList());
        }
        if (loggedUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"))) {
            return certificateRepository.findAll()
                    .stream()
                    .filter(c -> c.getCertificateType() != CertificateType.ROOT)
                    .map(CertificateDTO::new)
                    .collect(Collectors.toList());
        }
        if (loggedUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_USER"))) {
            return certificateRepository.findAllByOwner(loggedUser)
                    .stream()
                    .map(CertificateDTO::new)
                    .collect(Collectors.toList());
        }
        throw new ChangeSetPersister.NotFoundException();
    }
    private List<Certificate> getSignedCertificatesRecursive(String issuerSerialNumber) {
        List<Certificate> signed = certificateRepository.findByIssuerSerialNumber(issuerSerialNumber);
        List<Certificate> result = new ArrayList<>(signed);

        for (Certificate cert : signed) {
            result.addAll(getSignedCertificatesRecursive(cert.getSerialNumber()));
        }

        return result;
    }

}
