import { Injectable, inject } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { UserService } from '../user.service';
import { AuthService } from '../auth.service';

@Injectable({
  providedIn: 'root',
})
export class AdminGuardService implements CanActivate {
  private router = inject(Router);
  private userService = inject(UserService);
  private authService = inject(AuthService);

  /**
   * canActivate method
   * Validates JWT token and checks if user has ADMIN role
   * @return {Observable<boolean>}
   */
  public canActivate(): Observable<boolean> {
    if (!this.authService.hasToken()) {
      this.router.navigate(['/login']);
      return of(false);
    }

    if (!this.authService.isAdmin()) {
      this.router.navigate(['/positions-manage']);
      return of(false);
    }

    return this.userService.tokenValidator().pipe(
      map(response => {
        if (response.status === 'success') {
          return true;
        }
        this.authService.logout();
        return false;
      }),
      catchError(() => {
        this.authService.logout();
        return of(false);
      }),
    );
  }
}
