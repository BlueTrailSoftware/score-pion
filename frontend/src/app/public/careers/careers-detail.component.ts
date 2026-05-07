import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule, DatePipe, Location } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { PositionService } from '../../services/position.service';
import { PublicPosition } from '../../models/position.model';
import { formatExperienceRange } from './careers-position-card/careers-position-card.utils';

@Component({
  selector: 'app-careers-detail',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './careers-detail.component.html',
  styleUrls: ['./careers-detail.component.scss'],
})
export class CareersDetailComponent implements OnInit, OnDestroy {
  private readonly positionService = inject(PositionService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private readonly destroy$ = new Subject<void>();

  public position: PublicPosition | null = null;
  public loading = false;
  public error: string | null = null;

  ngOnInit(): void {
    const navigation = this.router.getCurrentNavigation();
    const statePosition = navigation?.extras?.state?.['position'] as PublicPosition;

    const historyState = history.state?.position as PublicPosition;

    if (statePosition || historyState) {
      // Use cached data from navigation
      this.position = statePosition || historyState;
      this.loading = false;
    } else {
      // No cached data, fetch from API
      this.loadPositionDetails();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadPositionDetails(): void {
    const positionId = this.route.snapshot.paramMap.get('id');

    if (!positionId) {
      this.error = 'Position ID not found';
      return;
    }

    this.loading = true;
    this.error = null;

    this.positionService
      .getPublicPositionById(positionId)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.loading = false;
        }),
      )
      .subscribe({
        next: response => {
          this.position = response.data || null;
          if (!this.position) {
            this.error = 'Position not found or no longer available';
          }
        },
        error: error => {
          console.error('Error loading position details:', error);
          if (error.status === 404) {
            this.error = 'Position not found or no longer available';
          } else {
            this.error = 'Unable to load position details. Please try again later.';
          }
        },
      });
  }

  public get experienceLabel(): string | null {
    return formatExperienceRange(this.position?.experienceMin, this.position?.experienceMax);
  }

  public goBack(): void {
    this.location.back();
  }

  public onApply(): void {
    if (this.position) {
      this.router.navigate(['/careers', this.position.id, 'apply']);
    }
  }
}
