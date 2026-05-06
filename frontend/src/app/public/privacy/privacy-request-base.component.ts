import { Component, OnDestroy, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Observable, Subject, takeUntil } from 'rxjs';
import { CandidatesService } from '../../services/candidates.service';
import { RecaptchaService } from '../../services/recaptcha.service';
import { DeleteDataResponse } from '../../models/candidate.model';

@Component({
  template: '',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
})
export abstract class PrivacyRequestBaseComponent implements OnDestroy {
  protected fb = inject(FormBuilder);
  protected candidatesService = inject(CandidatesService);
  protected recaptchaService = inject(RecaptchaService);
  protected destroy$ = new Subject<void>();

  requestForm: FormGroup;
  loading = false;
  success = false;
  error = '';

  constructor() {
    this.requestForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  protected abstract sendRequest(email: string, captchaToken: string): Observable<DeleteDataResponse>;
  protected abstract getRecaptchaAction(): string;

  onSubmit() {
    if (this.requestForm.invalid) return;

    this.submitRequest();
  }

  private async submitRequest() {
    this.loading = true;
    this.error = '';
    this.success = false;

    try {
      const captchaToken = await this.recaptchaService.executeRecaptcha(this.getRecaptchaAction());
      const email = this.requestForm.value.email;

      this.sendRequest(email, captchaToken)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.loading = false;
            this.success = true;
            this.requestForm.reset();
          },
          error: err => {
            this.loading = false;
            if (err.status === 400 && err.error?.message?.includes('CAPTCHA')) {
              this.error = 'Security validation failed. Please refresh and try again.';
            } else {
              this.error = 'Failed to send verification email. Please try again.';
            }
          },
        });
    } catch (error) {
      this.loading = false;
      this.error = 'Security validation failed. Please refresh and try again.';
      console.error('reCAPTCHA error:', error);
    }
  }
}
