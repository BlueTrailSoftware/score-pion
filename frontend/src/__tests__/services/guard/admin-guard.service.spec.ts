import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AdminGuardService } from '../../../app/services/guard/admin-guard.service';
import { UserService } from '../../../app/services/user.service';
import { AuthService } from '../../../app/services/auth.service';

describe('AdminGuardService', () => {
  let guard: AdminGuardService;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let router: Router;

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['hasToken', 'isAdmin', 'logout']);
    userServiceSpy = jasmine.createSpyObj('UserService', ['tokenValidator']);

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        AdminGuardService,
        { provide: AuthService, useValue: authServiceSpy },
        { provide: UserService, useValue: userServiceSpy },
      ],
    });

    guard = TestBed.inject(AdminGuardService);
    router = TestBed.inject(Router);
  });

  it('should redirect to login when no token', done => {
    authServiceSpy.hasToken.and.returnValue(false);
    spyOn(router, 'navigate');
    guard.canActivate().subscribe(r => {
      expect(r).toBeFalse();
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
      done();
    });
  });

  it('should redirect to positions-manage when not admin', done => {
    authServiceSpy.hasToken.and.returnValue(true);
    authServiceSpy.isAdmin.and.returnValue(false);
    spyOn(router, 'navigate');
    guard.canActivate().subscribe(r => {
      expect(r).toBeFalse();
      expect(router.navigate).toHaveBeenCalledWith(['/positions-manage']);
      done();
    });
  });

  it('should allow access when admin with valid token', done => {
    authServiceSpy.hasToken.and.returnValue(true);
    authServiceSpy.isAdmin.and.returnValue(true);
    userServiceSpy.tokenValidator.and.returnValue(of({ status: 'success' } as any));
    guard.canActivate().subscribe(r => {
      expect(r).toBeTrue();
      done();
    });
  });

  it('should logout on token validation error', done => {
    authServiceSpy.hasToken.and.returnValue(true);
    authServiceSpy.isAdmin.and.returnValue(true);
    userServiceSpy.tokenValidator.and.returnValue(throwError(() => new Error('fail')));
    guard.canActivate().subscribe(r => {
      expect(r).toBeFalse();
      expect(authServiceSpy.logout).toHaveBeenCalled();
      done();
    });
  });
});
