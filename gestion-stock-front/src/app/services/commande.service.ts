import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {Commande} from '../interfaces/commande';

@Injectable({
  providedIn: 'root'
})
export class CommandeService {
  private apiUrl = `${environment.apiUrl}/api/commandes`;

  constructor(private http: HttpClient) {}

  creerCommande(commandePayload: any): Observable<any> {
    const fournisseurId = commandePayload.fournisseurId;
    return this.http.post(`${this.apiUrl}/passer?fournisseurId=${fournisseurId}`, commandePayload);
  }

  getAll(): Observable<Commande[]> {
    return this.http.get<Commande[]>(this.apiUrl);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
