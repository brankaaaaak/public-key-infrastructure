import { Injectable } from '@angular/core';
import { BehaviorSubject, catchError, map, Observable, throwError } from 'rxjs';
import { ApiService } from './api.service';
import { UserService } from '../services/user.service';
import { ConfigService } from './config.service';
import { Router } from '@angular/router';
import { JwtHelperService } from '@auth0/angular-jwt';


@Injectable({
  providedIn: 'root'
})
export class AuthService {

  isLoggedIn$ = new BehaviorSubject<boolean>(false);
  user$ =new BehaviorSubject<string | null>(null); //role
  userState = this.user$.asObservable();

  constructor(private apiService: ApiService,
    private userService: UserService,
    private config: ConfigService,
    private router: Router) { }

    private access_token = null;
    private refresh_token = null;

  login(credentials: any): Observable<any> {
    const body = {
      email: credentials.email,
      password: credentials.password
    };

    return this.apiService.post(this.config.login_url, JSON.stringify(body)).pipe(
      map((res: any) => {
        this.storeTokens(res.body.accessToken, res.body.refreshToken);
        this.setUser();
        return res;
      })
    );
  }
  storeTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem('access_token', accessToken);
    localStorage.setItem('refresh_token', refreshToken);
    this.isLoggedIn$.next(true);
  }
  getToken(): string | null {
    return localStorage.getItem('access_token');
  }

  setToken(token: string): void {
    localStorage.setItem('access_token', token);
  }

  setUser(): void {
    this.user$.next(this.getRole());
  }
  getRole(): string | null {
    const token = localStorage.getItem('jwt');
    if (!token) return null;

    const helper = new JwtHelperService();
    const decoded = helper.decodeToken(token);
    return decoded?.roles ? decoded.roles[0] : null; 
  }
  refreshToken(): Observable<string> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) return throwError(() => new Error('No refresh token'));

    const body = {
      refreshToken: refreshToken
    };

    return this.apiService.post(`${this.config.api_url}/users/refresh-token`, body).pipe(
      map((res: any) => {
        const { accessToken, refreshToken } = res.body;
        this.storeTokens(accessToken, refreshToken);
        return accessToken;
      }),
      catchError(err => {
        this.logout();
        return throwError(() => err);
      })
    );
  }

  logout(): void {
    this.clearTokens();
    this.user$.next(null);
    this.isLoggedIn$.next(false);
    this.userService.currentUser = null;
    this.router.navigate(['/login']);
  }
  getRefreshToken(): string | null {
    return localStorage.getItem('refresh_token');
  }
  clearTokens(): void {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
  }
    
}
