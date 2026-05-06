import { Injectable, inject } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class LoginGuardService implements CanActivate {
  private router = inject(Router);

  /**
   * canActivate method
   * checks if the user is logged in by determining if the
   * token exists in the local storage
   * @return {boolean}
   */
  public canActivate(): boolean {
    const token = localStorage.getItem('auth');
    const permissions = localStorage.getItem('permissions');

    if (token) {
      const redirectUrl = permissions === 'ADMIN' ? '/admin/candidates' : '/positions-manage';
      this.router.navigateByUrl(redirectUrl);
      return false;
    }

    return true;
  }
}
