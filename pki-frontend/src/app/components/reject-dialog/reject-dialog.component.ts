import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-reject-dialog',
  standalone: true,
  imports: [],
  templateUrl: './reject-dialog.component.html',
  styleUrl: './reject-dialog.component.scss'
})
export class RejectDialogComponent {
  reason = '';

  constructor(
    public dialogRef: MatDialogRef<RejectDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { id: number }) {}

  onCancel(): void {
    this.dialogRef.close();
  }

  onSubmit(): void {
    this.dialogRef.close(this.reason);
  }
}
