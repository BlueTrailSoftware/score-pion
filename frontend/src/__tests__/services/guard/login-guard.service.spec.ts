import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { LoginGuardService } from '../../../app/services/guard/login-guard.service';

describe('LoginGuardService', () => {
  let guard: LoginGuardService;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), LoginGuardService],
    });
    guard = TestBed.inject(LoginGuardService);
    router = TestBed.inject(Router);
    localStorage.clear();
  });

  afterEach(() => localStorage.clear());

  it('should allow access when no token', () => {
    expect(guard.canActivate()).toBeTrue();
  });

  it('should redirect admin to candidates when token exists', () => {
    localStorage.setItem('auth', 'token');
    localStorage.setItem('permissions', 'ADMIN');
    spyOn(router, 'navigateByUrl');
    expect(guard.canActivate()).toBeFalse();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/admin/candidates');
  });

  it('should redirect recruiter to positions-manage when token exists', () => {
    localStorage.setItem('auth', 'token');
    localStorage.setItem('permissions', 'RECRUITER');
    spyOn(router, 'navigateByUrl');
    expect(guard.canActivate()).toBeFalse();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/positions-manage');
  });
});
