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
  selector: 'app-admin-manage-requests',
  standalone: true,
  imports: [CommonModule,MatDialogModule,MatButtonModule,
    MatInputModule,MatFormFieldModule,FormsModule,MatTableModule],
  templateUrl: './admin-manage-requests.component.html',
  styleUrl: './admin-manage-requests.component.scss'
})
export class AdminManageRequestsComponent {
  requests: any[] = [];
  displayedColumns: string[] = [
    'id', 'createdAt','durationInDays', 'certificateType', 'subject', 'issuer','issuerType','issuerFrom','issuerTo','keyUsages','extendedKeyUsages', 'actions'
  ];

  constructor(private service: CertificateService,
              private dialog: MatDialog) {}

  ngOnInit(): void {
    this.loadRequests();
  }

  loadRequests() {
    this.service.getPendingRequests().subscribe(data => {
      this.requests = data;
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
