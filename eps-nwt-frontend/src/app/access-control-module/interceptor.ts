import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
} from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable()
export class Interceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    console.log('[Interceptor] Request:', {
      url: req.url,
      method: req.method,
      headers: req.headers.keys(),
    });

    if (req.url.includes('/login') || req.url.includes('/register')) {
      console.log('[Interceptor] Skipping for:', req.url);
      return next.handle(req);
    }

    if (req.headers.has('X-Skip-Interceptor')) {
      console.log('[Interceptor] Skipping due to X-Skip-Interceptor:', req.url);

      const cleanReq = req.clone({
        headers: req.headers.delete('X-Skip-Interceptor'),
      });

      return next.handle(cleanReq);
    }

    const token = localStorage.getItem('accessToken');
    if (token) {
      const cloned = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` },
      });
      console.log('[Interceptor] Added Authorization header for:', req.url);

      return next.handle(cloned).pipe(
        tap({
          error: (err) => console.error('[Interceptor] Error with token:', err),
        })
      );
    }

    console.warn('[Interceptor] No token found, sending request without Authorization:', req.url);
    return next.handle(req).pipe(
      tap({
        error: (err) => console.error('[Interceptor] Error (no token):', err),
      })
    );
  }
}
