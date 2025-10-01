import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-revoke-certificate-dialog',
  standalone: true,
  imports: [CommonModule,FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule
  ],
  templateUrl: './revoke-certificate-dialog.component.html',
  styleUrl: './revoke-certificate-dialog.component.scss'
})
export class RevokeCertificateDialogComponent {
  reasons: string[] = [
    'keyCompromise',
    'CACompromise',
    'affiliationChanged',
    'superseded',
    'cessationOfOperation',
    'certificateHold',
    'removeFromCRL',
    'privilegeWithdrawn',
    'AACompromise'
  ];

  selectedReason: string = '';

  constructor(
    public dialogRef: MatDialogRef<RevokeCertificateDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

 confirm() {
  if (!this.selectedReason) {
    alert('Please select a reason before revoking!');
    return; 
  }

  this.dialogRef.close(this.selectedReason);
}


  cancel() {
    this.dialogRef.close(null);
  }
}
