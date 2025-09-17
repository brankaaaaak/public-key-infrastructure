import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-create-root-certificate',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule],
  templateUrl: './create-root-certificate.component.html',
  styleUrl: './create-root-certificate.component.scss'
})
export class CreateRootCertificateComponent {
  form: FormGroup;

  constructor(private fb: FormBuilder, private http: HttpClient, private router: Router) {
    this.form = this.fb.group({
      commonName: ['', Validators.required],
      organization: ['', Validators.required],
      validFrom: ['', Validators.required],
      validTo: ['', Validators.required]
    });
  }
  onSubmit() {
    if (this.form.invalid) return;
  }
}
