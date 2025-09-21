import { Component } from '@angular/core';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-ca-home',
  standalone: true,
  imports: [],
  templateUrl: './ca-home.component.html',
  styleUrl: './ca-home.component.scss'
})
export class CaHomeComponent {

  constructor(private authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }
}
