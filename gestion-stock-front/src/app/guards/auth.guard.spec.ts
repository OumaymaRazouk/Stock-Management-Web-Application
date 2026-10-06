import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, provideRouter, Router, UrlTree } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { AuthGuard } from './auth.guard';
import { routes } from '../app.routes';

describe('Role route guard', () => {
  let guard: AuthGuard;
  let router: Router;
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient()] });
    guard = TestBed.inject(AuthGuard);
    router = TestBed.inject(Router);
    localStorage.setItem('token', 'test-only-token');
  });
  afterEach(() => localStorage.clear());
  const snapshot = (roles?: string[]) => ({
    pathFromRoot: [{ data: {} }, { data: roles ? { roles } : {} }]
  } as ActivatedRouteSnapshot);

  it('rejects legacy USER sessions without granting another role', () => {
    localStorage.setItem('userRole', 'USER');
    const result = guard.canActivate(snapshot()) as UrlTree;
    expect(router.serializeUrl(result)).toBe('/auth/login');
    expect(localStorage.getItem('token')).toBeNull();
  });

  for (const role of ['ADMIN', 'ACHETEUR', 'COMMERCIAL']) {
    it(`enforces parent account permissions for ${role}`, () => {
      localStorage.setItem('userRole', role);
      const route = { pathFromRoot: [{ data: { roles: ['ADMIN'] } }, { data: {} }] } as ActivatedRouteSnapshot;
      const result = guard.canActivateChild(route);
      if (role === 'ADMIN') expect(result).toBeTrue();
      else expect(router.serializeUrl(result as UrlTree)).toBe('/dashboard');
    });
    it(`enforces purchase permissions for ${role}`, () => {
      localStorage.setItem('userRole', role);
      const result = guard.canActivate(snapshot(['ADMIN', 'ACHETEUR']));
      if (role !== 'COMMERCIAL') expect(result).toBeTrue();
      else expect(router.serializeUrl(result as UrlTree)).toBe('/dashboard');
    });
  }

  it('does not route to a public registration form', () => {
    expect(routes.find(route => route.path === 'auth/register')?.redirectTo).toBe('auth/login');
    const accounts = routes.find(route => !!route.children)?.children?.find(route => route.path === 'comptes');
    expect(accounts?.data?.['roles']).toEqual(['ADMIN']);
  });
});
