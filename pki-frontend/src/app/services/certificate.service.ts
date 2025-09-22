import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../env/enviroment';

@Injectable({
  providedIn: 'root'
})
export class CertificateService {

  constructor(private http: HttpClient) { }

  createCertificateRequest(dto: any): Observable<any> {
    return this.http.post<any>(`${environment.apiHost}/api/certificate/request`, dto);
  }
  getOrganizationCertificates(): Observable<any> {
    const token = localStorage.getItem('jwt');
              let headers = new HttpHeaders();
              if (token) {
                headers = headers.set('Authorization', `Bearer ${token}`);
              }
    return this.http.get<any>(`${environment.apiHost}/api/certificate/organization/intermediate`,{headers});
  }
  getRootCertificate(): Observable<any> {
    const token = localStorage.getItem('jwt');
              let headers = new HttpHeaders();
              if (token) {
                headers = headers.set('Authorization', `Bearer ${token}`);
              }
    return this.http.get<any>(`${environment.apiHost}/api/certificate/root`,{headers});
  }


}
