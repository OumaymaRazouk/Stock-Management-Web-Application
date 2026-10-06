import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const apiRoot = `${environment.apiUrl}/api/`;
  const isApi = req.url.startsWith(apiRoot);
  const isLogin = req.url === `${apiRoot}auth/login`;
  const token = auth.getToken();
  if (isApi && !isLogin && token && !req.headers.has('Authorization')) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req).pipe(catchError((error: HttpErrorResponse) => {
    if (isApi && !isLogin && error.status === 401) {
      auth.clearSession();
      void router.navigate(['/auth/login']);
    }
    return throwError(() => error);
  }));
};
