import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../env/enviroment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) { }

  public currentUser : any | null = null;

  register(dto: any): Observable<any> {
    return this.http.post<any>(`${environment.apiHost}/api/users/register`, dto);
  }
  getAllCAUsers(): Observable<any> {
    return this.http.get<any>(`${environment.apiHost}/api/users/all`);
  }
  getCAUsersByOrganization(orgId: number): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiHost}/api/users/all-org/${orgId}`);

  }
  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiHost}/api/users/all-users`);
  }
  getUserById(id: number): Observable<any> {
    return this.http.get<any>(`${environment.apiHost}/api/users/${id}`);
  }

  upgradeUserToCA(id: number): Observable<any> {
    return this.http.put<any>(`${environment.apiHost}/api/users/${id}/upgrade`, {});
  }
  getUsersByOrganization(orgId: number): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiHost}/api/users/all-org/role-user/${orgId}`);
  }
}
