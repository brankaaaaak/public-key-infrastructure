import { Component } from '@angular/core';
import { CertificateService } from '../../services/certificate.service';
import { CommonModule } from '@angular/common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule} from '@angular/material/table';
import { FormsModule } from '@angular/forms';
import { RejectDialogComponent } from '../reject-dialog/reject-dialog.component';
@Component({
  selector: 'app-ca-manage-requests',
  standalone: true,
  imports: [CommonModule,MatDialogModule,MatButtonModule,
  MatInputModule,MatFormFieldModule,FormsModule,MatTableModule],
  templateUrl: './ca-manage-requests.component.html',
  styleUrl: './ca-manage-requests.component.scss'
})
export class CaManageRequestsComponent {
requests: any[] = [];
  displayedColumns: string[] = [
    'id', 'createdAt','durationInDays', 'certificateType', 'subject', 'issuer','issuerType','issuerFrom','issuerTo', 'keyUsages',
    'extendedKeyUsages','actions'
  ];

  constructor(private service: CertificateService,
              private dialog: MatDialog) {}

  ngOnInit(): void {
    this.loadRequests();
  }

  loadRequests() {
  this.service.getPendingRequestsCA().subscribe(data => {
    // mapiraj rezultate i dopuni polja
    this.requests = data.map(req => {
      const newReq = { ...req }; // kopiraj objekat

      if (newReq.certificateType === 'ROOT' || newReq.certificateType === 'INTERMEDIATE') {
        // ako backend nije slao key usages, postavi ručno
        newReq.keyUsages = ['keyCertSign', 'cRLSign'];
        newReq.extendedKeyUsages = ['/']; // ili prazno ili po potrebi
      } else {
        // za end-entity može ostati onako kako je stiglo
        newReq.keyUsages = newReq.keyUsages || [];
        newReq.extendedKeyUsages = newReq.extendedKeyUsages || [];
      }

      return newReq;
    });
  });
}

  approve(request: any) {
    this.service.approveRequest(request.id).subscribe(() => {
      this.loadRequests();
    });
  }

  reject(request: any) {
    const dialogRef = this.dialog.open(RejectDialogComponent, {
      width: '400px',
      data: { id: request.id }
    });

    dialogRef.afterClosed().subscribe(reason => {
      if (reason) {
        this.service.rejectRequest(request.id, reason).subscribe(() => {
          this.loadRequests();
        });
      }
    });
  }
}
