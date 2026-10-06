import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface BandeCommande {
  id: number;
  articleReference: string;
  designationArticle: string; // Ajouté si tu l'as dans le backend
  quantite: number;
  quantiteConditionnement: number;
  dateCommande: string;
  dateArrivage: string;
  fournisseurNom: string;
  prixUnitaire?: number;
  montantBande?: number;
}

@Injectable({ providedIn: 'root' })
export class BandeCommandeService {
  private apiUrl = `${environment.apiUrl}/api/commandes/bandes`;

  constructor(private http: HttpClient) {}

  getAllBandes(): Observable<BandeCommande[]> {
    return this.http.get<BandeCommande[]>(this.apiUrl);
  }
}
