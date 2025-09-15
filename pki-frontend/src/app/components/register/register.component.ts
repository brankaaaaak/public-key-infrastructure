import { Component } from '@angular/core';
import { FormGroup, Validators, FormBuilder, ReactiveFormsModule } from '@angular/forms';
import zxcvbn from 'zxcvbn';
import { passwordStrengthValidator } from '../../helpers/password-strength.validator';
import { NgIf } from '@angular/common';
import { UserService } from '../../services/user.service';
import { Router } from '@angular/router';
import { passwordMatchValidator } from '../../helpers/password-match.validator';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  form: FormGroup;
  passwordStrength = 0;
  showSuccessPopup = false;
  notification?: { msgBody: string };

  constructor(private fb: FormBuilder, private userService: UserService, private router: Router) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      organization: ['', Validators.required],
      password: ['', [Validators.required, passwordStrengthValidator()]],
      repeatPassword: ['', Validators.required],
    }, { validators: passwordMatchValidator() });
  }

  onPasswordInput(): void {
    const password = this.form.get('password')?.value || '';
    const result = zxcvbn(password);
    this.passwordStrength = result.score;
  }

  get passwordStrengthLabel(): string {
    return ['Very weak', 'Weak', 'Fair', 'Good', 'Excellent'][this.passwordStrength];
  }

  get passwordStrengthColor(): string {
    return ['#d32f2f', '#f57c00', '#fbc02d', '#388e3c', '#2e7d32'][this.passwordStrength];
  }

  onSubmit(): void {
    if (this.form.valid) {
      this.notification = { msgBody: 'Processing registration, please wait...' };
      const dto = {
        name: this.form.value.name,
        lastName: this.form.value.lastName,
        email: this.form.value.email,
        organization: this.form.value.organization,
        password: this.form.value.password
      };

      this.userService.register(dto).subscribe({
        next: () => {
          this.showSuccessPopup = true;

          setTimeout(() => {
            this.showSuccessPopup = false;
            this.router.navigate(['/login']);
          }, 3000);
        },
        error: (err) => {
          console.error('Registration failed:', err);
        }
      });
    }
  }
}
