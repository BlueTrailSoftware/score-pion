import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from './user.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private router = inject(Router);
  private userService = inject(UserService);

  /**
   * Centralized logout logic
   * Clears all authentication data and redirects to login
   */
  logout(): void {
    this.userService.clearCache();
    localStorage.removeItem('id');
    localStorage.removeItem('auth');
    localStorage.removeItem('permissions');

    // Check if user logged in via Auth0
    const loginMethod = localStorage.getItem('loginMethod');
    localStorage.removeItem('loginMethod');

    if (loginMethod === 'auth0') {
      // Redirect to backend Auth0 logout endpoint
      // Backend should handle Auth0 logout and redirect back to login
      window.location.href = `${environment.apiUrl}/auth0-logout`;
    } else {
      this.router.navigate(['/login']);
    }
  }

  /**
   * Check if user has authentication token
   */
  hasToken(): boolean {
    return !!localStorage.getItem('auth');
  }

  /**
   * Get current user's role
   */
  getUserRole(): string | null {
    return localStorage.getItem('permissions');
  }

  /**
   * Get current user's ID
   */
  getCurrentUserId(): string | null {
    return localStorage.getItem('id');
  }

  /**
   * Check if current user is admin
   */
  isAdmin(): boolean {
    return this.getUserRole() === 'ADMIN';
  }

  /**
   * Check if current user is recruiter
   */
  isRecruiter(): boolean {
    return this.getUserRole() === 'RECRUITER';
  }
}
