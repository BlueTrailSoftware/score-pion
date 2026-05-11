import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { catchError, Observable, of, Subject, shareReplay, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { HealthCheckResponse } from '../models/responses/health-check-response.model';
import { ProfileResponse } from '../models/responses/profile-response.model';
import { RolesResponse } from '../models/responses/roles-response.model';
import { EmptyResponse } from '../models/responses/empty-response.model';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private http = inject(HttpClient);

  // refresh
  private _refreshNedded$ = new Subject<void>();

  get refreshNedded$() {
    return this._refreshNedded$;
  }

  // Cache for user profile to avoid repeated calls
  private userProfileCache = new Map<string, Observable<ProfileResponse>>();

  // Cache for token validation (5 minutes TTL)
  private tokenValidationCache: { observable: Observable<EmptyResponse> | null; timestamp: number } = {
    observable: null,
    timestamp: 0,
  };
  private readonly TOKEN_CACHE_TTL = 5 * 60 * 1000; // 5 minutes

  /**
   * getGitHashFE method
   * read file from assets folder to get the current git hash
   * @return {Observable<{ hash: string }>} observable from response
   */
  getGitHashFE(): Observable<{ hash: string }> {
    return this.http.get<{ hash: string }>('/assets/git-hash.json');
  }

  /**
   * getGitHashBE method
   * it gets the current git hash from the backend
   * @return {Observable<HealthCheckResponse>} observable from response
   */
  getGitHashBE(): Observable<HealthCheckResponse> {
    return this.http.get<HealthCheckResponse>(`${environment.apiUrl}/actuator/info`).pipe(
      catchError(error => {
        return of(error);
      }),
    );
  }

  /**
   * getUser method
   * it gets the profile´s user information with caching
   * @param {string} id id from user
   * @param {boolean} forceRefresh force refresh bypassing cache
   * @return {Observable<ProfileResponse>} observable from response
   */
  public getUser(id: string, forceRefresh: boolean = false): Observable<ProfileResponse> {
    // If forceRefresh, clear cache for this user
    if (forceRefresh) {
      this.userProfileCache.delete(id);
    }

    // Check if we have a cached observable for this user
    if (!this.userProfileCache.has(id)) {
      // Create new observable and cache it with shareReplay
      const profile$ = this.http.get<ProfileResponse>(`${environment.apiUrl}/users/${id}/profile`).pipe(
        shareReplay(1), // Cache the last emitted value and share with all subscribers
        catchError(error => {
          // On error, remove from cache so next call tries again
          this.userProfileCache.delete(id);
          return of(error);
        }),
      );

      this.userProfileCache.set(id, profile$);
    }

    return this.userProfileCache.get(id)!;
  }

  /**
   * getUserRoles method
   * request the roles assigned to the user
   * @param {number} userId id of the user from whom retrieve the roles
   * @param {number} page page to get from the database
   * @returns {Observable<RolesResponse>}
   */
  public getUserRoles(userId: number, page: number): Observable<RolesResponse> {
    return this.http.get<RolesResponse>(`${environment.apiUrl}/users/${userId}/roles?page=${page}`).pipe(
      catchError(error => {
        return of(error);
      }),
    );
  }

  /**
   * logOutUser method
   * call the enpoint logout by delete token and
   * log out
   * @returns {Observable<EmptyResponse>}
   */
  public logOutUser(): Observable<EmptyResponse> {
    return this.http.delete<EmptyResponse>(`${environment.apiUrl}/users/logout`).pipe(
      catchError(error => {
        return of(error);
      }),
    );
  }

  /**
   * tokenValidator method
   * call endpoint token-validator to verify the token before loading views
   * @returns {Observable<EmptyResponse>}
   */
  public tokenValidator(): Observable<EmptyResponse> {
    const now = Date.now();

    if (this.tokenValidationCache.observable && now - this.tokenValidationCache.timestamp < this.TOKEN_CACHE_TTL) {
      return this.tokenValidationCache.observable;
    }

    // Create new validation request and cache it
    const validation$ = this.http.get<EmptyResponse>(`${environment.apiUrl}/users/token-validator`).pipe(
      shareReplay(1), // Share the result with multiple subscribers
      tap({
        error: () => {
          // On error, invalidate cache immediately
          this.tokenValidationCache.observable = null;
          this.tokenValidationCache.timestamp = 0;
        },
      }),
      catchError(error => {
        return of(error);
      }),
    );

    // Update cache
    this.tokenValidationCache.observable = validation$;
    this.tokenValidationCache.timestamp = now;

    return validation$;
  }

  /**
   * clearCache method
   * Clears all caches (useful when logging out or on auth errors)
   */
  public clearCache(): void {
    this.userProfileCache.clear();
    this.tokenValidationCache.observable = null;
    this.tokenValidationCache.timestamp = 0;
  }
}
