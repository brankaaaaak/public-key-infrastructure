package com.example.publicKeyInfrastructure.service.implementation;

import com.example.publicKeyInfrastructure.crypto.CryptoUtils;
import com.example.publicKeyInfrastructure.crypto.KeystoreManager;
import com.example.publicKeyInfrastructure.crypto.MasterKeyManager;
import com.example.publicKeyInfrastructure.data.CertificateIssuer;
import com.example.publicKeyInfrastructure.data.CertificateSubject;
import com.example.publicKeyInfrastructure.dto.CertificateDTO;
import com.example.publicKeyInfrastructure.dto.CertificateTreeDTO;
import com.example.publicKeyInfrastructure.enumeration.CertificateType;
import com.example.publicKeyInfrastructure.model.*;
import com.example.publicKeyInfrastructure.model.Certificate;
import com.example.publicKeyInfrastructure.repository.ICertificateRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.math.BigInteger;
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
            //to do i or ee
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
                if (subject.getKeyUsages() != null) {
                    int keyUsageBits = buildKeyUsageBits(subject.getKeyUsages());
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

        X509CertificateHolder certHolder = certBuilder.build(contentSigner);
        X509Certificate certificate = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);

        certificate.checkValidity(new Date());
        certificate.verify(issuer.getPublicKey());

        return certificate;
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

    private CertificateDTO mapToDTO(Certificate certificate) {
        return new CertificateDTO(certificate);
    }// build chain for keystore
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
                case "CERTIFICATE SIGN": bits |= KeyUsage.keyCertSign; break;
                case "CRL SIGN": bits |= KeyUsage.cRLSign; break;
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
    public CertificateTreeDTO getCertificateChainForLoggedCA() throws Exception {
        User loggedUser = getLoggedUser();

        // nađi root/intermediate sertifikate koji pripadaju CA korisniku
        List<Certificate> roots = certificateRepository.findByOwnerId(loggedUser.getId());

        if (roots.isEmpty()) {
            throw new RuntimeException("No certificates found for this user.");
        }

        // za svaki sertifikat napravi stablo
        return buildTree(roots.get(0)); // uzimam prvi – ako može više, vraćaj listu
    }

    private CertificateTreeDTO buildTree(Certificate cert) {
        return buildTree(cert, new HashSet<>());
    }

    private CertificateTreeDTO buildTree(Certificate cert, Set<String> visited) {
        if (visited.contains(cert.getSerialNumber())) {
            // već viđen, prekidamo da ne napravimo beskonačnu petlju
            return null;
        }
        visited.add(cert.getSerialNumber());

        CertificateTreeDTO dto = new CertificateTreeDTO(
                cert.getSerialNumber(),
                cert.getCertificateType(),
                cert.getOwner().getEmail(),
                cert.isRevoked(),
                new ArrayList<>()
        );

        List<Certificate> children = certificateRepository.findChildrenByIssuerSerialNumber(cert.getSerialNumber());
        for (Certificate child : children) {
            CertificateTreeDTO childDto = buildTree(child, visited);
            if (childDto != null) {
                dto.getChildren().add(childDto);
            }
        }

        return dto;
    }


}
