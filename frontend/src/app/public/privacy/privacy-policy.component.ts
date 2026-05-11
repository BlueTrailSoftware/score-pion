import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { marked } from 'marked';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-privacy-policy',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './privacy-policy.component.html',
  styleUrl: './privacy-policy.component.scss',
})
export class PrivacyPolicyComponent implements OnInit, OnDestroy {
  private http = inject(HttpClient);
  private destroy$ = new Subject<void>();

  htmlContent = '';
  loading = true;
  error = '';

  private readonly placeholders: Record<string, string> = {
    '{{privacyEmail}}': environment.branding.privacy.contactEmail,
    '{{companyLegalName}}': environment.branding.legalName,
    '{{dataRetentionMonths}}': environment.branding.privacy.dataRetentionMonths.toString(),
    '{{baseUrl}}': window.location.origin,
  };

  ngOnInit() {
    this.http
      .get('/assets/privacy-policy.md', { responseType: 'text' })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: markdown => {
          const processedMarkdown = this.replacePlaceholders(markdown);
          this.htmlContent = marked(processedMarkdown) as string;
          this.loading = false;
        },
        error: () => {
          this.error = 'Failed to load privacy policy';
          this.loading = false;
        },
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private replacePlaceholders(content: string): string {
    let result = content;
    for (const [placeholder, value] of Object.entries(this.placeholders)) {
      result = result.split(placeholder).join(value);
    }
    return result;
  }
}
