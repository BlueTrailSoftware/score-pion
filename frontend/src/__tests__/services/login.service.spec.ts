import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { LoginService } from '../../app/services/login.service';

describe('LoginService', () => {
  let service: LoginService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), LoginService],
    });
    service = TestBed.inject(LoginService);
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('isLoggedIn', () => {
    it('should return true when auth token exists in localStorage', () => {
      localStorage.setItem('auth', 'valid-token');
      expect(service.isLoggedIn()).toBeTrue();
    });

    it('should return false when auth token is absent', () => {
      expect(service.isLoggedIn()).toBeFalse();
    });

    it('should return false when auth token is empty string', () => {
      localStorage.setItem('auth', '');
      expect(service.isLoggedIn()).toBeFalse();
    });
  });
});
