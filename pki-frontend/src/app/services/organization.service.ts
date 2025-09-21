import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../env/enviroment';

@Injectable({
  providedIn: 'root'
})
export class OrganizationService {

  constructor(private http: HttpClient) { }

  getOrganizationNames(): Observable<string[]> {
    return this.http.get<string[]>(`${environment.apiHost}/api/organization/names`);
  }

}
