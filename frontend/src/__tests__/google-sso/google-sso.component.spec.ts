import { TestBed } from '@angular/core/testing';
import { provideRouter, Router, ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { GoogleSsoComponent } from '../../app/google-sso/google-sso.component';

describe('GoogleSsoComponent', () => {
  let router: Router;
  let queryParams$: Subject<any>;

  beforeEach(async () => {
    queryParams$ = new Subject();
    localStorage.clear();

    await TestBed.configureTestingModule({
      imports: [GoogleSsoComponent],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { queryParams: queryParams$.asObservable() } },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate');
  });

  afterEach(() => localStorage.clear());

  it('should store auth data and navigate to admin route for ADMIN', () => {
    const fixture = TestBed.createComponent(GoogleSsoComponent);
    fixture.detectChanges();

    queryParams$.next({ id: '123', token: 'jwt-token', permissions: 'ADMIN' });

    expect(localStorage.getItem('id')).toBe('123');
    expect(localStorage.getItem('auth')).toBe('jwt-token');
    expect(localStorage.getItem('permissions')).toBe('ADMIN');
    expect(router.navigate).toHaveBeenCalledWith(['/admin/candidates']);
  });

  it('should store auth data and navigate to recruiter route for RECRUITER', () => {
    const fixture = TestBed.createComponent(GoogleSsoComponent);
    fixture.detectChanges();

    queryParams$.next({ id: '456', token: 'jwt-token', permissions: 'RECRUITER' });

    expect(localStorage.getItem('id')).toBe('456');
    expect(localStorage.getItem('auth')).toBe('jwt-token');
    expect(localStorage.getItem('permissions')).toBe('RECRUITER');
    expect(router.navigate).toHaveBeenCalledWith(['/recruiter/candidates']);
  });

  it('should redirect to login with error when no token', () => {
    const fixture = TestBed.createComponent(GoogleSsoComponent);
    fixture.detectChanges();

    queryParams$.next({ error: 'Access denied' });

    expect(router.navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: { gisError: 'Access denied' },
    });
  });

  it('should redirect to login with default error when no token and no error param', () => {
    const fixture = TestBed.createComponent(GoogleSsoComponent);
    fixture.detectChanges();

    queryParams$.next({});

    expect(router.navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: { gisError: 'No token received' },
    });
  });
});
