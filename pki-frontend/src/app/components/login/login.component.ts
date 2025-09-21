import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../auth/auth.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  loginFailed = false;
  form: FormGroup;


  constructor(private authService: AuthService, private fb: FormBuilder, private router: Router) {
    this.form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });
  }

  onSubmit() {
    if (this.form.invalid) {
      return;
    }
    const { email, password } = this.form.value;

    this.authService.login({ email, password }).subscribe({
      next: () => {
        this.loginFailed = false;
        const role = this.authService.getRole();
        if (role === 'ROLE_ADMIN') {
          this.router.navigate(['/admin-home']);
        }else if(role === 'ROLE_CA'){
          this.router.navigate(['/ca-home']);
        }
      },
      error: (err) => {
        this.loginFailed = true;
        console.error('Login failed', err);
      }
    });
  }
}
