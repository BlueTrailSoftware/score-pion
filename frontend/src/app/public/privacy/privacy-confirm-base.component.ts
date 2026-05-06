import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Observable, Subject, takeUntil } from 'rxjs';
import { CandidatesService } from '../../services/candidates.service';

@Component({
  template: '',
  standalone: true,
  imports: [CommonModule],
})
export abstract class PrivacyConfirmBaseComponent implements OnInit, OnDestroy {
  protected route = inject(ActivatedRoute);
  protected candidatesService = inject(CandidatesService);
  protected destroy$ = new Subject<void>();

  loading = true;
  success = false;
  error = '';

  protected abstract processConfirmation(token: string): Observable<any>;
  protected abstract handleSuccess(response: any): void;
  protected abstract getInvalidTokenMessage(): string;

  ngOnInit() {
    const token = this.route.snapshot.paramMap.get('token');

    if (!token) {
      this.loading = false;
      this.error = this.getInvalidTokenMessage();
      return;
    }

    this.processConfirmation(token)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: response => {
          this.loading = false;
          this.success = true;
          this.handleSuccess(response);
        },
        error: () => {
          this.loading = false;
          this.error = 'Failed to process request. Token may be invalid or expired.';
        },
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
