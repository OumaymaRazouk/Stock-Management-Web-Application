import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, finalize, tap } from 'rxjs';
import { AuthResponse } from '../interfaces/auth-response';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${environment.apiUrl}/api/auth`;
  constructor(private http: HttpClient) {}

  login(credentials: { email: string; motDePasse: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        this.clearSession();
        if (response.success && response.token) {
          this.setToken(response.token);
          localStorage.setItem('userRole', response.role ?? '');
        }
      })
    );
  }

  logout(): Observable<void> {
    const token = this.getToken();
    const headers = token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
    return this.http.post<void>(`${this.apiUrl}/logout`, {}, { headers }).pipe(
      finalize(() => this.clearSession())
    );
  }

  clearSession(): void {
    if (typeof localStorage === 'undefined') return;
    ['token', 'connected', 'userRole', 'userEmail'].forEach(key => localStorage.removeItem(key));
  }

  isLoggedIn(): boolean { return !!this.getToken(); }
  setToken(token: string): void { localStorage.setItem('token', token); }
  getToken(): string | null {
    return typeof localStorage === 'undefined' ? null : localStorage.getItem('token');
  }
  getUserRole(): string | null {
    return typeof localStorage === 'undefined' ? null : localStorage.getItem('userRole');
  }
  isAcheteur(): boolean { return this.getUserRole() === 'ACHETEUR'; }
  isAdmin(): boolean { return this.getUserRole() === 'ADMIN'; }
  isCommercial(): boolean { return this.getUserRole() === 'COMMERCIAL'; }
  canEdit(): boolean { return this.isAdmin(); }
  canCreateBesoin(): boolean { return this.isAdmin() || this.isCommercial(); }
  canManagePurchases(): boolean { return this.isAdmin() || this.isAcheteur(); }
}
