import { Component } from '@angular/core';
import { CertificateService } from '../../services/certificate.service';
import { CommonModule, NgIf } from '@angular/common';

@Component({
  selector: 'app-user-download',
  standalone: true,
  imports: [NgIf, CommonModule],
  templateUrl: './user-download.component.html',
  styleUrl: './user-download.component.scss'
})
export class UserDownloadComponent {
  requests: any[] = [];

  constructor(private service: CertificateService) {}

  ngOnInit(): void {
    this.service.getMineEndEntityCertificates().subscribe({
      next: (data) => this.requests = data,
      error: (err) => console.error('Failed to fetch requests', err)
    });
  }

  downloadCertificate(cert: any): void {
    this.service.downloadCertificate(cert.id).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `certificate-${cert.serialNumber}.p12`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Failed to download certificate', err);
        alert('Download failed.');
      }
    });
  }
  isExpired(validTo: Date | string): boolean {
    return new Date(validTo) < new Date();
  }


}
