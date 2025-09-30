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
    return this.http.get<any>(`${environment.apiHost}/api/certificate/organization/intermediate`);
  }
  getRootCertificate(): Observable<any> {
    return this.http.get<any>(`${environment.apiHost}/api/certificate/root`);
  }
  getPendingRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiHost}/api/certificate/admin-requests/pending`);
  }

  approveRequest(id: number): Observable<void> {
    return this.http.put<void>(`${environment.apiHost}/api/certificate/request/${id}/approve`, {});
  }

  rejectRequest(id: number, reason: string): Observable<void> {
    return this.http.put<void>(`${environment.apiHost}/api/certificate/request/${id}/deny`, { reason: reason });
  }
  getPendingRequestsCA(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiHost}/api/certificate/ca-requests/pending`);
  }
  getDownloadTable(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiHost}/api/certificate/download-table`);
  }
  downloadCertificate(requestId: number): Observable<Blob> {
    const url = `${environment.apiHost}/api/certificate/${requestId}/download`;
    return this.http.get(url, {
      responseType: 'blob' 
    });
  }
  getCertificates(): Observable<any> {
    return this.http.get<any>(`${environment.apiHost}/api/certificate/all`);
  }
  getCertificatesWithoutRoot(): Observable<any> {
    return this.http.get<any>(`${environment.apiHost}/api/certificate/all-without-root`);
  }
}
