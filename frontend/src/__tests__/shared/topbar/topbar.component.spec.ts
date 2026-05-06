import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { TopbarComponent } from '../../../app/shared/topbar/topbar.component';
import { UserService } from '../../../app/services/user.service';
import { AuthService } from '../../../app/services/auth.service';

describe('TopbarComponent', () => {
  let component: TopbarComponent;
  let fixture: ComponentFixture<TopbarComponent>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    userServiceSpy = jasmine.createSpyObj('UserService', ['getUser', 'logOutUser']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['logout']);

    userServiceSpy.getUser.and.returnValue(
      of({ data: { id: 'u-1', name: 'Jane Doe', email: 'jane@example.com' } } as any),
    );
    userServiceSpy.logOutUser.and.returnValue(of({} as any));

    localStorage.setItem('id', 'u-1');
    localStorage.setItem('permissions', 'ADMIN');

    await TestBed.configureTestingModule({
      imports: [TopbarComponent],
      providers: [
        provideRouter([]),
        { provide: UserService, useValue: userServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TopbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => localStorage.clear());

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load user profile on init', () => {
    expect(userServiceSpy.getUser).toHaveBeenCalledWith('u-1');
    expect(component.userProfile?.name).toBe('Jane Doe');
  });

  it('should set userPermissions from localStorage', () => {
    expect(component.userPermissions).toBe('ADMIN');
  });

  describe('getHomeRoute', () => {
    it('should return admin route for ADMIN', () => {
      component.userPermissions = 'ADMIN';
      expect(component.getHomeRoute()).toBe('/admin/candidates');
    });

    it('should return positions-manage for non-ADMIN', () => {
      component.userPermissions = 'RECRUITER';
      expect(component.getHomeRoute()).toBe('/positions-manage');
    });
  });

  describe('getUserInitials', () => {
    it('should return two initials for full name', () => {
      component.userProfile = { name: 'Jane Doe', email: 'jane@example.com' };
      expect(component.getUserInitials()).toBe('JD');
    });

    it('should return one initial for single name', () => {
      component.userProfile = { name: 'Jane', email: 'jane@example.com' };
      expect(component.getUserInitials()).toBe('J');
    });

    it('should return email initial when no name', () => {
      component.userProfile = { email: 'jane@example.com' };
      expect(component.getUserInitials()).toBe('J');
    });

    it('should return ? when no profile data', () => {
      component.userProfile = {};
      expect(component.getUserInitials()).toBe('?');
    });
  });

  describe('logout', () => {
    it('should call logOutUser then authService.logout', () => {
      component.logout();
      expect(userServiceSpy.logOutUser).toHaveBeenCalled();
      expect(authServiceSpy.logout).toHaveBeenCalled();
    });
  });

  describe('onImageError', () => {
    it('should set imageLoadError to true', () => {
      component.onImageError();
      expect(component.imageLoadError).toBeTrue();
    });
  });
});
