import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { PositionService } from '../../services/position.service';
import { PublicPosition } from '../../models/position.model';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-careers-list',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './careers-list.component.html',
  styleUrls: ['./careers-list.component.scss'],
})
export class CareersListComponent implements OnInit, OnDestroy {
  private readonly positionService = inject(PositionService);
  private readonly router = inject(Router);
  private readonly destroy$ = new Subject<void>();

  public positions: PublicPosition[] = [];
  public loading = false;
  public error: string | null = null;

  // Configurable branding (optional)
  public readonly showCompanyLogo = environment.branding?.showCompanyLogo ?? false;
  public readonly companyLogoPath = environment.branding?.companyLogoPath;
  public readonly careersTitle = environment.branding?.careers?.title;
  public readonly careersSubtitle = environment.branding?.careers?.subtitle;

  ngOnInit(): void {
    this.loadPublicPositions();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadPublicPositions(): void {
    this.loading = true;
    this.error = null;

    this.positionService
      .getPublicPositions()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.loading = false;
        }),
      )
      .subscribe({
        next: response => {
          this.positions = response.data || [];
        },
        error: error => {
          console.error('Error loading public positions:', error);
          this.error = 'Unable to load positions. Please try again later.';
          this.positions = [];
        },
      });
  }

  public onPositionClick(position: PublicPosition): void {
    this.router.navigate(['/careers', position.id], {
      state: { position },
    });
  }

  public onRefresh(): void {
    this.positionService.clearPublicPositionsCache();
    this.loadPublicPositions();
  }

  public trackByPositionId(_index: number, position: PublicPosition): string {
    return position.id;
  }
}
