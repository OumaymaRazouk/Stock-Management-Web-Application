import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Besoin, BesoinDto} from '../interfaces/besoin';
import { Observable } from 'rxjs';
import {BesoinCommande} from '../interfaces/besoin-commande';

@Injectable({
  providedIn: 'root'
})
export class BesoinService {
  private apiUrl = `${environment.apiUrl}/api/besoins`;

  constructor(private http: HttpClient) {}

  createBesoin(besoin: any): Observable<Besoin> {
    return this.http.post<Besoin>(this.apiUrl, besoin);
  }

  getAllBesoins(): Observable<Besoin[]> {
    return this.http.get<Besoin[]>(this.apiUrl);
  }

  getBesoinsByType(type: string): Observable<Besoin[]> {
    return this.http.get<Besoin[]>(`${this.apiUrl}/type/${type}`);
  }

  getBesoinsByArticle(articleId: number): Observable<Besoin[]> {
    return this.http.get<Besoin[]>(`${this.apiUrl}/article/${articleId}`);
  }


  deleteBesoin(besoinId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${besoinId}`);
  }
  getBesoinsByReference(reference: string) {
    return this.http.get<BesoinDto[]>(`${environment.apiUrl}/api/besoins/filter?reference=${reference}`);
  }

// Récupérer besoins par fournisseur
  getBesoinsByFournisseur(nomFournisseur: string) {
    return this.http.get<BesoinDto[]>(`${environment.apiUrl}/api/besoins/fournisseur?nom=${nomFournisseur}`);
  }

  getPreparationCommande(): Observable<BesoinCommande[]> {
    return this.http.get<BesoinCommande[]>(`${environment.apiUrl}/api/besoins/commande/preparation`);
  }

  // NOUVELLE MÉTHODE : Récupérer un besoin par ID
  getBesoinById(id: number): Observable<Besoin> {
    return this.http.get<Besoin>(`${this.apiUrl}/${id}`);
  }

  // NOUVELLE MÉTHODE : Modifier un besoin
  updateBesoin(id: number, besoin: any): Observable<Besoin> {
    return this.http.put<Besoin>(`${this.apiUrl}/${id}`, besoin);
  }
}
