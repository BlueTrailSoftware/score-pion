import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  private http = inject(HttpClient);

  /**
   *isLoggedIn method returns access token
   * @returns {boolean} boolean
   */
  public isLoggedIn(): boolean {
    const token = localStorage.getItem('auth');
    return !!token;
  }
}
