import { Injectable, inject } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { UserService } from '../user.service';
import { AuthService } from '../auth.service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuardService implements CanActivate {
  private router = inject(Router);
  private userService = inject(UserService);
  private authService = inject(AuthService);

  /**
   * canActivate method
   * Validates JWT token with backend before allowing access to protected routes
   * @return {Observable<boolean>}
   */
  public canActivate(): Observable<boolean> {
    if (!this.authService.hasToken()) {
      this.router.navigate(['/login']);
      return of(false);
    }

    // Validate token with backend
    return this.userService.tokenValidator().pipe(
      map(response => {
        // If token is valid, allow access
        if (response.status === 'success') {
          return true;
        }
        // If token is invalid, redirect to login
        this.authService.logout();
        return false;
      }),
      catchError(() => {
        // On error, clear auth and redirect to login
        this.authService.logout();
        return of(false);
      }),
    );
  }
}
