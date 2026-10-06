import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { tokenInterceptor } from './token.interceptor';
import { environment } from '../../environments/environment';

describe('Bearer authentication', () => {
  let auth: AuthService;
  let http: HttpTestingController;
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [
      provideRouter([]), provideHttpClient(withInterceptors([tokenInterceptor])), provideHttpClientTesting()
    ] });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => { http.verify(); localStorage.clear(); });

  it('does not trust the legacy connected flag', () => {
    localStorage.setItem('connected', 'true');
    expect(auth.isLoggedIn()).toBeFalse();
  });

  it('stores a successful login and revokes it on logout', () => {
    auth.login({ email: 'test@example.invalid', motDePasse: 'test-only-password' }).subscribe();
    http.expectOne(`${environment.apiUrl}/api/auth/login`).flush({
      success: true, token: 'test-only-token', role: 'ADMIN'
    });
    expect(auth.getToken()).toBe('test-only-token');
    auth.logout().subscribe();
    const logout = http.expectOne(`${environment.apiUrl}/api/auth/logout`);
    expect(logout.request.headers.get('Authorization')).toBe('Bearer test-only-token');
    logout.flush(null, { status: 204, statusText: 'No Content' });
    expect(auth.getToken()).toBeNull();
    expect(auth.getUserRole()).toBeNull();
  });

  it('does not accept an unsuccessful login response', () => {
    auth.login({ email: 'test@example.invalid', motDePasse: 'test-only-password' }).subscribe();
    http.expectOne(`${environment.apiUrl}/api/auth/login`).flush({ success: false });
    expect(auth.isLoggedIn()).toBeFalse();
  });
});
