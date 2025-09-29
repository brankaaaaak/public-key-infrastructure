import { Component } from '@angular/core';
import { CertificateService } from '../../services/certificate.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-ca-create-intermediate-certificate',
  standalone: true,
  imports: [FormsModule,CommonModule],
  templateUrl: './ca-create-intermediate-certificate.component.html',
  styleUrl: './ca-create-intermediate-certificate.component.scss'
})
export class CaCreateIntermediateCertificateComponent {
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
    // 1. Dohvati root sertifikat
    this.certificateService.getRootCertificate().subscribe({
      next: (root) => {
        this.caCertificates = [root]; // dodaj root prvi

        // 2. Dohvati intermediate sertifikate za organizaciju
        this.certificateService.getOrganizationCertificates().subscribe({
          next: (intermediates) => {
            this.caCertificates.push(...intermediates);
          },
          error: (err) => {
            console.error('Error fetching intermediate certificates', err);
          }
        });
      },
      error: (err) => {
        console.error('Error fetching root certificate', err);
      }
    });
  }
  
  createIntermediateCertificate(): void {
    if (!this.selectedIssuerSerialNumber) {
      this.errorMessage = 'Please select an issuer certificate.';
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
      certificateType: 'INTERMEDIATE',
      issuer: this.selectedIssuerSerialNumber,
      durationInDays:this.durationInDays,
      keyUsages: null,
      extendedKeyUsages: null,
      csrPem: null
    };

    this.certificateService.createCertificateRequest(request).subscribe({
      next: (response) => {
        this.successMessage = 'Request for Intermediate certificate created successfully! Waiting for approval!';
        this.errorMessage = '';
      },
      error: (error) => {
        this.errorMessage = 'An error occurred: ' + error.error?.message || error.message;
        this.successMessage = '';
      }
    });
  }
}
