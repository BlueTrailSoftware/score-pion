import { TestBed } from '@angular/core/testing';
import { HTTP_INTERCEPTORS, HttpClient, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { BaseUrlInterceptor } from '../../app/interceptors/base-path-interceptor.service';
import { environment } from '../../environments/environment';

describe('BaseUrlInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: HTTP_INTERCEPTORS, useClass: BaseUrlInterceptor, multi: true },
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should prepend base URL to relative paths', () => {
    http.get('/admin/test').subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/admin/test`);
    expect(req.request.url).toBe(`${environment.apiUrl}/admin/test`);
    req.flush({});
  });

  it('should NOT prepend base URL to absolute URLs', () => {
    http.get('http://external.com/api').subscribe();
    const req = httpMock.expectOne('http://external.com/api');
    expect(req.request.url).toBe('http://external.com/api');
    req.flush({});
  });

  it('should NOT prepend base URL to /assets/ paths', () => {
    http.get('/assets/data.json').subscribe();
    const req = httpMock.expectOne('/assets/data.json');
    expect(req.request.url).toBe('/assets/data.json');
    req.flush({});
  });

  it('should add Authorization header when token exists', () => {
    localStorage.setItem('auth', 'my-jwt-token');
    http.get('/admin/test').subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/admin/test`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-jwt-token');
    req.flush({});
  });

  it('should NOT add Authorization header when no token', () => {
    http.get('/admin/test').subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/admin/test`);
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });
});
