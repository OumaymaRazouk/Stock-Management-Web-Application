import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Acheteur } from '../interfaces/acheteur';

@Injectable({ providedIn: 'root' })
export class AcheteurService {
  private apiUrl = `${environment.apiUrl}/api/acheteurs`;

  constructor(private http: HttpClient) {}

  getAcheteurs(): Observable<Acheteur[]> {
    return this.http.get<Acheteur[]>(this.apiUrl);
  }

  getAcheteurById(id: number): Observable<Acheteur> {
    return this.http.get<Acheteur>(`${this.apiUrl}/${id}`);
  }

  addAcheteur(acheteur: Acheteur) {
    return this.http.post(`${environment.apiUrl}/api/acheteurs`, acheteur, { responseType: 'text' });
  }

  updateAcheteur(id: number, acheteur: Acheteur) {
    return this.http.put(`${environment.apiUrl}/api/acheteurs/${id}`, acheteur, { responseType: 'text' });
  }
  deleteAcheteur(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
