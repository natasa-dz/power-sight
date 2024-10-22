import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
} from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class Interceptor implements HttpInterceptor {
  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    const accessToken: any = localStorage.getItem('user');
    if (req.headers.get('skip')) return next.handle(req);
    console.log("INTERCEPTOR");
    if (accessToken) {
      console.log('accesToken:', accessToken);
      const cloned = req.clone({
        setHeaders: {Authorization: 'Bearer ' + accessToken},
      });
      return next.handle(cloned);
    } else {
      console.log("INTERCEPTOR2");
      return next.handle(req);
    }
  }
}
