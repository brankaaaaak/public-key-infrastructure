import { Component } from '@angular/core';
import { AuthService } from '../../auth/auth.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-user-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './user-home.component.html',
  styleUrl: './user-home.component.scss'
})
export class UserHomeComponent {
constructor(private authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }
}
