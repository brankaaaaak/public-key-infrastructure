import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpInterceptor,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { catchError, switchMap, Observable, throwError, BehaviorSubject } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable()
export class Interceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

  constructor(
    private auth: AuthService,
    private _snackBar: MatSnackBar,
    private router: Router
  ) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.auth.getToken();
    if (token) {
      request = this.addTokenHeader(request, token);
    }

    return next.handle(request).pipe(
      catchError(error => {
        if (error instanceof HttpErrorResponse && error.status === 401) {
          if (!this.isRefreshing) {
            this.isRefreshing = true;
            this.refreshTokenSubject.next(null);

            return this.auth.refreshToken().pipe(
              switchMap((newAccessToken: string) => {
                this.isRefreshing = false;
                this.auth.setToken(newAccessToken);
                this.refreshTokenSubject.next(newAccessToken);

                return next.handle(this.addTokenHeader(request, newAccessToken));
              }),
              catchError(err => {
                this.isRefreshing = false;
                this.auth.logout();
                this.openSnackBar("Session expired. Please log in again.");
                this.router.navigate(['/login']);
                return throwError(() => err);
              })
            );
          } else {
            return this.refreshTokenSubject.pipe(
              switchMap((token) => {
                if (token) {
                  return next.handle(this.addTokenHeader(request, token));
                }
                return throwError(() => error);
              })
            );
          }
        }

        return throwError(() => error);
      })
    );
  }

  private addTokenHeader(request: HttpRequest<any>, token: string) {
    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  private openSnackBar(snackMsg: string): void {
    this._snackBar.open(snackMsg, "Dismiss", {
      duration: 3000
    });
  }
}
