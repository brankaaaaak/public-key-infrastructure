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
    'CERTIFICATE SIGN',
    'CRL SIGN',
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

