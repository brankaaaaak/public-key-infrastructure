import { Component } from '@angular/core';
import { CertificateService } from '../../services/certificate.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-admin-create-intermediate-certificate',
  standalone: true,
  imports: [FormsModule,CommonModule],
  templateUrl: './admin-create-intermediate-certificate.component.html',
  styleUrl: './admin-create-intermediate-certificate.component.scss'
})
export class AdminCreateIntermediateCertificateComponent {
successMessage = '';
  errorMessage = '';
  durationInDays:number=0;
  caCertificates:any=[];
  selectedIssuerSerialNumber: string | null = null;
  users:any[] = [];
  selectedOwnerId: number | null = null;

  constructor(private certificateService: CertificateService,
              private userService: UserService) {}

  ngOnInit(): void {
    this.loadCertificates();
  }

  private loadCertificates(): void {
    this.certificateService.getCertificates().subscribe({
      next: (intermediates) => this.caCertificates = intermediates,
      error: (err) => console.error('Error fetching certificates', err)
    });
  }

   
  onIssuerChange(): void {
    const issuer = this.caCertificates.find(
      (c: any) => c.serialNumber === this.selectedIssuerSerialNumber
    );

    if (!issuer) {
      this.users = [];
      return;
    }

    if (issuer.certificateType === 'ROOT') {
      // Root -> svi korisnici
      this.userService.getAllUsers().subscribe({
        next: (data) => this.users = data,
        error: (err) => console.error('Error fetching users', err)
      });
    } else {
      // Intermediate -> samo korisnici te organizacije
      const orgId = issuer.organization?.id;
      if (orgId) {
        this.userService.getUsersByOrganization(orgId).subscribe({
          next: (data) => this.users = data,
          error: (err) => console.error('Error fetching users for org', err)
        });
      } else {
        this.users = [];
      }
    }

    // Reset selected owner
    this.selectedOwnerId = null;
  }
  createIntermediateCertificate(): void {
    if (!this.selectedIssuerSerialNumber) {
      this.errorMessage = 'Please select an issuer certificate.';
      return;
    }
     if (!this.selectedOwnerId) {
        this.errorMessage = 'Please select an owner!';
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
      certificateType: 'INTERMEDIATE',
      issuer: this.selectedIssuerSerialNumber,
      durationInDays:this.durationInDays,
      keyUsages: null,
      extendedKeyUsages: null,
      csrPem: null,
      ownerId:this.selectedOwnerId
    };

    this.certificateService.createCertificateRequest(request).subscribe({
      next: (response) => {
        this.successMessage = 'Intermediate certificate created successfully!';
        this.errorMessage = '';
      },
      error: (error) => {
        this.errorMessage = 'An error occurred: ' + error.error?.message || error.message;
        this.successMessage = '';
      }
    });
  }
}

