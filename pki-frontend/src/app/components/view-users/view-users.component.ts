import { Component } from '@angular/core';
import {UserService} from '../../services/user.service';
import {UserDTO} from '../../dto/UserDTO';
import {NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-view-users',
  standalone: true,
  imports: [
    NgIf,
    NgForOf
  ],
  templateUrl: './view-users.component.html',
  styleUrl: './view-users.component.scss'
})
export class ViewUsersComponent {
  users: UserDTO[] = [];
  loading = true;
  error: string | null = null;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.userService.getAllUsers().subscribe({
      next: (data) => {
        console.log("poslat sam idem u next")
        console.log(data)
        this.users = data;
        this.loading = false;
      },
      error: (err) => {
        console.log("ovde dodjem iz nzkog razloga")
        this.error = 'Failed to load users';
        console.error(err);
        this.loading = false;
      }
    });
  }
  upgrade(id: number): void {
    this.userService.upgradeUserToCA(id).subscribe({
      next: (updatedUser) => {
        this.users = this.users.map(u =>
          u.id === id ? { ...u, role: 'ROLE_CA' } : u
        );
      },
      error: (err) => {
        console.error('Upgrade failed', err);
      }
    });
  }

}
