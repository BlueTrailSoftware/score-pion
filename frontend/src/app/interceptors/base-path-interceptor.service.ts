import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable()
export class BaseUrlInterceptor implements HttpInterceptor {
  private readonly baseUrl = environment.apiUrl;

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    let modifiedReq = req;

    const isStaticFile =
      req.url.startsWith('/git-hash.json') || req.url.startsWith('/assets/') || req.url.startsWith('/public/');

    // Add base URL to relative paths
    if (!req.url.startsWith('http') && !isStaticFile) {
      modifiedReq = modifiedReq.clone({
        url: `${this.baseUrl}${req.url}`,
      });
    }

    // Add JWT token to Authorization header if available
    const token = localStorage.getItem('auth');
    if (token) {
      modifiedReq = modifiedReq.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      });
    }

    return next.handle(modifiedReq);
  }
}
