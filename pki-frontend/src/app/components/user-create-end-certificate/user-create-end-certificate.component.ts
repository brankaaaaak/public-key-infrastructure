import { Component } from '@angular/core';
import { CertificateService } from '../../services/certificate.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-user-create-end-certificate',
  standalone: true,
  imports: [FormsModule,CommonModule],
  templateUrl: './user-create-end-certificate.component.html',
  styleUrl: './user-create-end-certificate.component.scss'
})
export class UserCreateEndCertificateComponent {
  successMessage = '';
  errorMessage = '';
  durationInDays:number=0;
  caCertificates:any=[];
  selectedIssuerSerialNumber: string | null = null;

  constructor(private certificateService: CertificateService) {}

   ngOnInit(): void {
    this.loadCertificates();
  }
  
   private loadCertificates(): void {

        // 1. Dohvati intermediate sertifikate za organizaciju
        this.certificateService.getOrganizationCertificates().subscribe({
          next: (intermediates) => {
            this.caCertificates.push(...intermediates);
          },
          error: (err) => {
            console.error('Error fetching intermediate certificates', err);
          }
        });
  }
  keyUsageOptions = [
    'DIGITAL SIGNATURE',
    'NON REPUDIATION',
    'KEY ENCIPHERMENT',
    'DATA ENCIPHERMENT',
    'KEY AGREEMENT',
    'ENCIPHER ONLY',
    'DECIPHER ONLY'
  ];

  extendedKeyUsageOptions = [
    'SERVER AUTHENTICATION',
    'CLIENT AUTHENTICATION',
    'CODE SIGNING',
    'EMAIL PROTECTION',
    'TIME STAMPING',
    'OCSP SIGNING'
  ];


  selectedKeyUsages: string[] = [];
  selectedExtendedKeyUsages: string[] = [];
  csrFileContent: string | null = null;

  onKeyUsageChange(event: any) {
    const value = event.target.value;
    if (event.target.checked) {
      this.selectedKeyUsages.push(value);
    } else {
      this.selectedKeyUsages = this.selectedKeyUsages.filter(u => u !== value);
    }
  }

  onExtendedKeyUsageChange(event: any) {
    const value = event.target.value;
    if (event.target.checked) {
      this.selectedExtendedKeyUsages.push(value);
    } else {
      this.selectedExtendedKeyUsages = this.selectedExtendedKeyUsages.filter(u => u !== value);
    }
  }
  useCsrFile: boolean = false;

onUseCsrFileChange() {
  if (this.useCsrFile) {
    // Resetuj key usages ako koristi CSR
    this.selectedKeyUsages = [];
    this.selectedExtendedKeyUsages = [];
  } else {
    // Resetuj fajl ako isključi CSR
    this.csrFileContent = null;
    const fileInput = document.getElementById('csrUpload') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = ''; // resetuje input polje
    }
  }
}
  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      this.csrFileContent = reader.result as string;
    };
    reader.readAsText(file);
  }

  createEndCertificate(): void {
    if (!this.selectedIssuerSerialNumber) {
      this.errorMessage = 'Please select an issuer certificate.';
      return;
    }
    if (this.durationInDays==0) {
            this.errorMessage = 'Please select days > 0!';
            return;
          }
     const issuer = this.caCertificates.find(
    (c: any) => c.serialNumber === this.selectedIssuerSerialNumber
  );

  if (!issuer) {
    this.errorMessage = 'Issuer not found.';
    return;
  }

  // Računaj kraj važenja novog sertifikata
  const startDate = new Date();
  const endDate = new Date(startDate);
  endDate.setDate(endDate.getDate() + this.durationInDays);

  const issuerValidFrom = new Date(issuer.validFrom);
  const issuerValidTo = new Date(issuer.validTo);

  // Provera: novi sertifikat mora stati unutar perioda issuera
  if (startDate < issuerValidFrom || endDate > issuerValidTo) {
    this.errorMessage = `Duration must be within issuer's validity period: 
        ${issuerValidFrom.toDateString()} - ${issuerValidTo.toDateString()}`;
    this.successMessage = '';
    return;
  }
    const request = {
      issuer: this.selectedIssuerSerialNumber,
      certificateType: 'END',
      durationInDays: this.durationInDays,
      keyUsages: this.selectedKeyUsages,
      extendedKeyUsages: this.selectedExtendedKeyUsages,
      csrPem: this.csrFileContent
    };

    this.certificateService.createCertificateRequest(request).subscribe({
      next: (response) => {
        this.successMessage = 'Request for End Entity certificate created successfully! Waiting for approval!';
        this.errorMessage = '';
      },
      error: (error) => {
        this.errorMessage = 'An error occurred: ' + error.error?.message || error.message;
        this.successMessage = '';
      }
    });
  }
}

