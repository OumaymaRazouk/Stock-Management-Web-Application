import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

export interface Account {
  id: number;
  source: 'UTILISATEUR' | 'ACHETEUR';
  nom: string;
  prenom: string;
  email: string;
  role: string;
}
export interface AccountInput {
  nom: string; prenom: string; email: string; role: string; motDePasse?: string;
}
@Injectable({ providedIn: 'root' })
export class AccountService {
  private url = `${environment.apiUrl}/api/admin/accounts`;
  constructor(private http: HttpClient) {}
  list() { return this.http.get<Account[]>(this.url); }
  create(input: AccountInput) { return this.http.post<Account>(this.url, input); }
  update(account: Account, input: AccountInput) {
    return this.http.put<Account>(`${this.url}/${account.source}/${account.id}`, input);
  }
  delete(account: Account) { return this.http.delete<void>(`${this.url}/${account.source}/${account.id}`); }
}
