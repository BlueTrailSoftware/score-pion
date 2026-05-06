import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule, Location } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { CandidatesService } from '../../services/candidates.service';
import { RecaptchaService } from '../../services/recaptcha.service';
import { NotificationService } from '../../services/notification.service';
import { FileHelper } from '../../helpers/fileHelper';

@Component({
  selector: 'app-apply-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './apply-form.component.html',
  styleUrl: './apply-form.component.scss',
})
export class ApplyFormComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private location = inject(Location);
  private candidatesService = inject(CandidatesService);
  private recaptchaService = inject(RecaptchaService);
  private notificationService = inject(NotificationService);
  private destroy$ = new Subject<void>();

  applyForm: FormGroup;
  positionId = '';
  selectedFile: File | null = null;
  loading = false;
  error = '';

  constructor() {
    this.applyForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: [''],
      linkedinUrl: [''],
      gdprConsent: [false, Validators.requiredTrue],
    });
  }

  ngOnInit() {
    this.positionId = this.route.snapshot.paramMap.get('id') || '';
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  goBack() {
    this.location.back();
  }

  onFileSelected(event: Event) {
    const result = FileHelper.handleFileSelection(event);
    if (result.error) {
      this.error = result.error;
      this.selectedFile = null;
      return;
    }
    this.selectedFile = result.file;
    this.error = '';
  }

  onSubmit() {
    if (this.applyForm.invalid) {
      this.applyForm.markAllAsTouched();
      return;
    }

    const linkedinUrl = this.applyForm.get('linkedinUrl')?.value?.trim();

    // Requirement: at least one must be provided (CV file or LinkedIn URL)
    if (!this.selectedFile && !linkedinUrl) {
      this.error = 'Please upload your CV/Resume OR provide your LinkedIn profile URL';
      return;
    }

    // Validation for LinkedIn URL if provided
    if (linkedinUrl) {
      // Regex to match backend validation: ^https?://(www\.)?linkedin\.com/.*$
      const linkedinRegex = /^https?:\/\/(www\.)?linkedin\.com\/.*$/;
      if (!linkedinRegex.test(linkedinUrl)) {
        this.error = 'Please enter a valid LinkedIn profile URL (e.g., https://www.linkedin.com/in/username)';
        return;
      }
    }

    this.submitApplication();
  }

  private async submitApplication() {
    this.loading = true;
    this.error = '';

    try {
      const captchaToken = await this.recaptchaService.executeRecaptcha('apply');

      const request = {
        ...this.applyForm.value,
        positionId: this.positionId,
        captchaToken,
      };

      this.candidatesService
        .apply(request, this.selectedFile || undefined)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.loading = false;
            this.notificationService.success('Thank you! Your application has been received successfully.');
            this.router.navigate(['/careers']);
          },
          error: err => {
            this.loading = false;
            if (err.status === 400 && err.error?.message?.includes('CAPTCHA')) {
              this.error = 'Security validation failed. Please refresh and try again.';
            } else {
              this.error = err.error?.message || 'Failed to submit application. Please try again.';
            }
          },
        });
    } catch (error: unknown) {
      this.loading = false;
      const errorMessage = error instanceof Error ? error.message : 'Unknown error';
      this.error = errorMessage || 'Security validation failed. Please refresh and try again.';
    }
  }
}
