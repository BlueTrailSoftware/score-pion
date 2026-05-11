import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { UserService } from '../../app/services/user.service';
import { environment } from '../../environments/environment';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), UserService],
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    service.clearCache();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getGitHashFE', () => {
    it('should GET /assets/git-hash.json', () => {
      service.getGitHashFE().subscribe(res => expect(res).toEqual({ hash: 'abc123' }));

      const req = httpMock.expectOne('/assets/git-hash.json');
      expect(req.request.method).toBe('GET');
      req.flush({ hash: 'abc123' });
    });
  });

  describe('getGitHashBE', () => {
    it('should GET the actuator/info endpoint', () => {
      const mockResponse = {
        git: { commit: { id: 'abc', time: '' }, branch: 'main' },
        build: { artifact: '', name: '', time: '', version: '', group: '' },
      };

      service.getGitHashBE().subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne(`${environment.apiUrl}/actuator/info`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('getUser', () => {
    it('should GET the user profile by id', () => {
      const mockProfile = { data: { id: 'u-1', email: 'user@example.com' } };

      service.getUser('u-1').subscribe(res => expect(res).toEqual(mockProfile as any));

      const req = httpMock.expectOne(`${environment.apiUrl}/users/u-1/profile`);
      expect(req.request.method).toBe('GET');
      req.flush(mockProfile);
    });

    it('should return cached observable on second call without new HTTP request', () => {
      const mockProfile = { data: { id: 'u-1', email: 'user@example.com' } };

      service.getUser('u-1').subscribe();
      httpMock.expectOne(`${environment.apiUrl}/users/u-1/profile`).flush(mockProfile);

      service.getUser('u-1').subscribe(res => expect(res).toEqual(mockProfile as any));
      httpMock.expectNone(`${environment.apiUrl}/users/u-1/profile`);
    });

    it('should bypass cache when forceRefresh is true', () => {
      const mockProfile = { data: { id: 'u-1', email: 'user@example.com' } };

      service.getUser('u-1').subscribe();
      httpMock.expectOne(`${environment.apiUrl}/users/u-1/profile`).flush(mockProfile);

      service.getUser('u-1', true).subscribe();
      httpMock.expectOne(`${environment.apiUrl}/users/u-1/profile`).flush(mockProfile);
    });
  });

  describe('getUserRoles', () => {
    it('should GET user roles with userId and page', () => {
      service.getUserRoles(1, 1).subscribe();

      const req = httpMock.expectOne(`${environment.apiUrl}/users/1/roles?page=1`);
      expect(req.request.method).toBe('GET');
      req.flush({});
    });
  });

  describe('logOutUser', () => {
    it('should DELETE the logout endpoint', () => {
      service.logOutUser().subscribe();

      const req = httpMock.expectOne(`${environment.apiUrl}/users/logout`);
      expect(req.request.method).toBe('DELETE');
      req.flush({});
    });
  });

  describe('tokenValidator', () => {
    it('should GET the token-validator endpoint', () => {
      service.tokenValidator().subscribe();

      const req = httpMock.expectOne(`${environment.apiUrl}/users/token-validator`);
      expect(req.request.method).toBe('GET');
      req.flush({});
    });

    it('should return cached observable within TTL without new HTTP request', () => {
      service.tokenValidator().subscribe();
      httpMock.expectOne(`${environment.apiUrl}/users/token-validator`).flush({});

      service.tokenValidator().subscribe();
      httpMock.expectNone(`${environment.apiUrl}/users/token-validator`);
    });
  });

  describe('clearCache', () => {
    it('should force a new HTTP request after cache is cleared', () => {
      const mockProfile = { data: { id: 'u-1' } };

      service.getUser('u-1').subscribe();
      httpMock.expectOne(`${environment.apiUrl}/users/u-1/profile`).flush(mockProfile);

      service.clearCache();

      service.getUser('u-1').subscribe();
      httpMock.expectOne(`${environment.apiUrl}/users/u-1/profile`).flush(mockProfile);
    });
  });
});
