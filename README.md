# PKI Management System

A Public Key Infrastructure (PKI) Management System for centralized certificate issuing and lifecycle management. The system supports multiple user roles with varying levels of access and functionality, enabling secure and controlled certificate operations across organizations.

---

## 🔐 User Roles

### Administrator
- Add new CA users and issue CA certificates (Root, Intermediate, End-Entity).
- View and manage all certificates in the system.
- Use any certificate from any chain for issuing.
- View, download, or revoke any certificate.

### CA User
- Issue Intermediate and End-Entity certificates **only within their organization**.
- Use their CA certificate or any certificate from their existing chain.
- View, download and revoke certificates only within their chain.

### Regular User
- Upload CSR and private key or fill a form to generate them.
- Choose issuing CA for their certificate.
- Download generated certificate and private key (in `.p12` format).
- View and revoke their own End-Entity certificates (with X.509 revocation reason).
---
## ✅ System Features

### 📝 Registration
- Only **regular users** can self-register.
- Required fields: email, password (with confirmation), name, surname, organization.
- Password strength is validated and assessed.
- Activation via **email link** (single use, time-limited).

### 🔐 Authentication
- All users log in via email and password.
- Upon login:
  - `Access Token` (short-lived) and `Refresh Token` (long-lived) are issued.
  - New access tokens are issued using the refresh token upon expiration.

---
## 🧾 Certificate Issuance

- Support for issuing:
  - **Root CA** (self-signed)
  - **Intermediate CA**
  - **End-Entity** (users, servers, devices)
- Certificate details required:
  - X.500 subject 
  - Validity period
  - Extensions
  - CA issuer
- Issuer certificate is validated (validity period, digital signature, revocation status).

---
## 🔒 Certificate Storage

- Private keys are **encrypted** in the database.
- Encryption is done using a per-organization key.
- Confidentiality of organization-specific keys must be maintained (master key architecture recommended).
---

## 📦 Certificate Download

- When downloading, the certificate and private key are packed into:
  - `.p12` (PKCS#12)

---
## 📂 Certificate Access

- **Admin**: sees all certificates.
- **CA user**: sees certificates in their chain.
- **Regular user**: sees only their issued EE certificates.

---
## ❌ Certificate Revocation

- Revoked certificates cannot be used for further issuance.
- Revocation checks via:
  - **CRL Distribution Point** (CDP) extension

---
## 🌐 Security & Access Control

- All client-server communication is done over **HTTPS**.
  - You can generate your own certificate using this PKI system.
- Role-based access control:
  - Only authenticated users may access protected routes.
  - Registration and login pages are public.

---
