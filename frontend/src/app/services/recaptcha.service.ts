import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

declare const grecaptcha: any;

@Injectable({
  providedIn: 'root',
})
export class RecaptchaService {
  private readonly siteKey = environment.recaptchaSiteKey;
  private readonly timeout = 10000; // 10 seconds timeout
  private scriptLoaded = false;
  private scriptLoading = false;

  /**
   * Load reCAPTCHA script dynamically
   */
  private loadRecaptchaScript(): Promise<void> {
    if (this.scriptLoaded) {
      return Promise.resolve();
    }

    if (this.scriptLoading) {
      return new Promise((resolve, reject) => {
        const checkInterval = setInterval(() => {
          if (this.scriptLoaded) {
            clearInterval(checkInterval);
            resolve();
          }
        }, 100);

        setTimeout(() => {
          clearInterval(checkInterval);
          reject(new Error('reCAPTCHA script loading timeout'));
        }, this.timeout);
      });
    }

    this.scriptLoading = true;

    return new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = `https://www.google.com/recaptcha/api.js?render=${this.siteKey}`;
      script.async = true;
      script.defer = true;

      script.onload = () => {
        this.scriptLoaded = true;
        this.scriptLoading = false;
        resolve();
      };

      script.onerror = () => {
        this.scriptLoading = false;
        reject(new Error('Failed to load reCAPTCHA script'));
      };

      document.head.appendChild(script);
    });
  }

  /**
   * Execute reCAPTCHA v3 and get token
   * @param action - The action name
   * @returns Promise with the reCAPTCHA token
   */
  async executeRecaptcha(action: string): Promise<string> {
    try {
      await this.loadRecaptchaScript();

      await this.waitForRecaptchaReady();

      const token = await Promise.race([grecaptcha.execute(this.siteKey, { action }), this.createTimeout()]);

      if (!token) {
        throw new Error('Failed to obtain reCAPTCHA token');
      }

      return token;
    } catch (error) {
      console.error('reCAPTCHA execution error:', error);
      throw new Error('Security validation failed. Please refresh and try again.');
    }
  }

  /**
   * Wait for grecaptcha to be ready
   */
  private waitForRecaptchaReady(): Promise<void> {
    return new Promise((resolve, reject) => {
      let attempts = 0;
      const maxAttempts = 50; // 5 seconds max

      const checkReady = () => {
        if (typeof grecaptcha !== 'undefined' && typeof grecaptcha.execute === 'function') {
          resolve();
        } else if (attempts >= maxAttempts) {
          reject(new Error('reCAPTCHA not ready'));
        } else {
          attempts++;
          setTimeout(checkReady, 100);
        }
      };

      checkReady();
    });
  }

  /**
   * Create a timeout promise
   */
  private createTimeout(): Promise<never> {
    return new Promise((_, reject) => {
      setTimeout(() => {
        reject(new Error('reCAPTCHA timeout'));
      }, this.timeout);
    });
  }
}
