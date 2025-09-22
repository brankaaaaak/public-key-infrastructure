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
  
  createEndCertificate(): void {
    if (!this.selectedIssuerSerialNumber) {
      this.errorMessage = 'Please select an issuer certificate.';
      return;
    }
    const request = {
      certificateType: 'END',
      issuer: this.selectedIssuerSerialNumber,
      durationInDays:this.durationInDays
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

