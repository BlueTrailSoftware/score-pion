import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthGuardService } from '../../../app/services/guard/auth-guard.service';
import { UserService } from '../../../app/services/user.service';
import { AuthService } from '../../../app/services/auth.service';

describe('AuthGuardService', () => {
  let guard: AuthGuardService;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let router: Router;

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['hasToken', 'logout']);
    userServiceSpy = jasmine.createSpyObj('UserService', ['tokenValidator']);

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        AuthGuardService,
        { provide: AuthService, useValue: authServiceSpy },
        { provide: UserService, useValue: userServiceSpy },
      ],
    });

    guard = TestBed.inject(AuthGuardService);
    router = TestBed.inject(Router);
  });

  it('should redirect to login when no token', done => {
    authServiceSpy.hasToken.and.returnValue(false);
    spyOn(router, 'navigate');

    guard.canActivate().subscribe(result => {
      expect(result).toBeFalse();
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
      done();
    });
  });

  it('should allow access when token is valid', done => {
    authServiceSpy.hasToken.and.returnValue(true);
    userServiceSpy.tokenValidator.and.returnValue(of({ status: 'success' } as any));

    guard.canActivate().subscribe(result => {
      expect(result).toBeTrue();
      done();
    });
  });

  it('should logout when token validation fails', done => {
    authServiceSpy.hasToken.and.returnValue(true);
    userServiceSpy.tokenValidator.and.returnValue(of({ status: 'error' } as any));

    guard.canActivate().subscribe(result => {
      expect(result).toBeFalse();
      expect(authServiceSpy.logout).toHaveBeenCalled();
      done();
    });
  });

  it('should logout on error', done => {
    authServiceSpy.hasToken.and.returnValue(true);
    userServiceSpy.tokenValidator.and.returnValue(throwError(() => new Error('fail')));

    guard.canActivate().subscribe(result => {
      expect(result).toBeFalse();
      expect(authServiceSpy.logout).toHaveBeenCalled();
      done();
    });
  });
});
