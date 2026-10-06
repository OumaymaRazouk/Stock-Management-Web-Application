import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, CanActivateChild, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate, CanActivateChild {
  constructor(private auth: AuthService, private router: Router) {}
  canActivateChild(route: ActivatedRouteSnapshot): boolean | UrlTree { return this.canActivate(route); }
  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const role = this.auth.getUserRole();
    if (!this.auth.isLoggedIn() || !['ADMIN', 'ACHETEUR', 'COMMERCIAL'].includes(role ?? '')) {
      this.auth.clearSession();
      return this.router.createUrlTree(['/auth/login']);
    }
    const allowed = route.pathFromRoot.every(part => {
      const roles: string[] | undefined = part.data['roles'];
      return !roles || roles.includes(role!);
    });
    return allowed || this.router.createUrlTree(['/dashboard']);
  }
}
