import { Component, NgZone, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { environment } from '../../../environments/environment';

// Google Identity Services types
declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: { client_id: string; login_uri: string; ux_mode: string; context: string }) => void;
          renderButton: (
            element: HTMLElement | null,
            config: {
              type: string;
              shape: string;
              theme: string;
              text: string;
              size: string;
              locale: string;
              logo_alignment: string;
            },
          ) => void;
        };
      };
    };
  }
}

@Component({
  selector: 'app-login-form',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './login-form.component.html',
  styleUrl: './login-form.component.scss',
})
export class LoginFormComponent implements OnInit {
  private activatedRoute = inject(ActivatedRoute);
  private ngZone = inject(NgZone);

  public googleBECallbackUrl: string = environment.apiUrl + environment.googleSSOCallbackPath;
  public googleSSOClientID = environment.googleSSOClientID;
  public gisErrorMessage: string = '';
  public companyName = environment.branding?.companyName || 'Score-Pion';

  ngOnInit() {
    this.activatedRoute.queryParams.subscribe(params => {
      this.gisErrorMessage = params['gisError'] || null;
    });

    if (window.google) {
      this.initializeGSI();
    } else {
      this.loadGSIScript();
    }
  }

  private loadGSIScript() {
    // Check if script already exists to avoid duplicate loading
    if (document.querySelector('script[src="https://accounts.google.com/gsi/client"]')) {
      return;
    }

    const script = document.createElement('script');
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.defer = true;
    script.onload = () => {
      this.ngZone.run(() => {
        this.initializeGSI();
      });
    };
    document.head.appendChild(script);
  }

  private initializeGSI() {
    if (!window.google) return;

    window.google.accounts.id.initialize({
      client_id: this.googleSSOClientID,
      login_uri: this.googleBECallbackUrl,
      ux_mode: 'redirect',
      context: 'signin',
    });

    window.google.accounts.id.renderButton(document.getElementById('google-signin-button'), {
      type: 'standard',
      shape: 'pill',
      theme: 'outline',
      text: 'signin_with',
      size: 'large',
      locale: 'en-US',
      logo_alignment: 'left',
    });
  }

  loginWithAuth0(): void {
    // Mark that user is logging in via Auth0
    localStorage.setItem('loginMethod', 'auth0');
    // Redirect to backend Auth0 login endpoint
    window.location.href = `${environment.apiUrl}/auth0-login`;
  }
}
