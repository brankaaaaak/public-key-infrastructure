import { Component } from '@angular/core';
import { CertificateService } from '../../services/certificate.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-ca-create-intermediate-certificate',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './ca-create-intermediate-certificate.component.html',
  styleUrl: './ca-create-intermediate-certificate.component.scss'
})
export class CaCreateIntermediateCertificateComponent {
  successMessage = '';
  errorMessage = '';
  durationInDays:number=0;

  constructor(private certificateService: CertificateService) {}
  
  createRootCertificate(): void {
    const request = {
      certificateType: 'INTERMEDIATE',
      issuer: null,
      durationInDays:this.durationInDays
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
