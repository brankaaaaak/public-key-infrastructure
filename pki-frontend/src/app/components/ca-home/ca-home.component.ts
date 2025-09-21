import { Component } from '@angular/core';
import { AuthService } from '../../auth/auth.service';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-ca-home',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './ca-home.component.html',
  styleUrl: './ca-home.component.scss'
})
export class CaHomeComponent {

  constructor(private authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }
}
