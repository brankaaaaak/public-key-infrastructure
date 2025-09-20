import { HttpClient } from '@angular/common/http';
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
  

}
