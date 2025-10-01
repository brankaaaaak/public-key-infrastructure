import { Component } from '@angular/core';
import { CertificateService } from '../../services/certificate.service';
import { CommonModule, NgIf } from '@angular/common';
import { RevokeCertificateDialogComponent } from '../revoke-certificate-dialog/revoke-certificate-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { MatTooltip } from '@angular/material/tooltip';

@Component({
  selector: 'app-user-download',
  standalone: true,
  imports: [NgIf, CommonModule,MatTooltip],
  templateUrl: './user-download.component.html',
  styleUrl: './user-download.component.scss'
})
export class UserDownloadComponent {
  requests: any[] = [];

  constructor(private service: CertificateService,private dialog: MatDialog) {}

  ngOnInit(): void {
    this.service.getDownloadTable().subscribe({
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

isRevoked(cert: any): boolean {
  return cert.revoked === true;
}
isExpiredOrRevoked(cert: any): boolean {
  return this.isExpired(cert.validTo) || this.isRevoked(cert);
}

getDisableReason(cert: any,action:string): string {
  if (this.isRevoked(cert)) {
    return 'This certificate has been revoked';
  }
  if (this.isExpired(cert)) {
    return 'This certificate has expired';
  }
  return action;
}

  revokeCertificate(certId: number) {
    const dialogRef = this.dialog.open(RevokeCertificateDialogComponent, {
      width: '400px',
      data: { certId }
    });

    dialogRef.afterClosed().subscribe(reason => {
      if (reason) {
        // ovde backend da zove revocation
        console.log(`Revoking cert ${certId} with reason: ${reason}`);
        // npr. this.certService.revoke(certId, reason).subscribe(...)
      }
  });
  }
}
