import { Component } from '@angular/core';
import { CertificateService } from '../../services/certificate.service';
import { CommonModule} from '@angular/common';
import { FormsModule } from '@angular/forms';
@Component({
  selector: 'app-create-root-certificate',
  standalone: true,
  imports: [CommonModule,FormsModule],
  templateUrl: './create-root-certificate.component.html',
  styleUrl: './create-root-certificate.component.scss'
})
export class CreateRootCertificateComponent {
  successMessage = '';
  errorMessage = '';
  durationInDays:number=0;

  constructor(private certificateService: CertificateService) {}
  
  createRootCertificate(): void {
    const request = {
      certificateType: 'ROOT',
      issuer: null,
      durationInDays:this.durationInDays
    };

    this.certificateService.createCertificateRequest(request).subscribe({
      next: (response) => {
        this.successMessage = 'Root certificate created successfully!';
        this.errorMessage = '';
      },
      error: (error) => {
        this.errorMessage = 'An error occurred: ' + error.error?.message || error.message;
        this.successMessage = '';
      }
    });
  }
}

