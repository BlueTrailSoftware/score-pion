import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { PositionService } from '../../services/position.service';
import { PublicPosition } from '../../models/position.model';
import { CareersPositionCardComponent } from './careers-position-card/careers-position-card.component';
import { environment } from '../../../environments/environment';
import {
  deriveLocationOptions,
  deriveJobTypeOptions,
  deriveWorkModeOptions,
  deriveSkillOptions,
  filterPositions,
  ALL_LOCATIONS,
  ALL_JOB_TYPES,
  ALL_WORK_MODES,
  ALL_SKILLS,
} from './careers-list.utils';

@Component({
  selector: 'app-careers-list',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, CareersPositionCardComponent],
  templateUrl: './careers-list.component.html',
  styleUrls: ['./careers-list.component.scss'],
})
export class CareersListComponent implements OnInit, OnDestroy {
  private readonly positionService = inject(PositionService);
  private readonly router = inject(Router);
  private readonly destroy$ = new Subject<void>();

  public allPositions: PublicPosition[] = [];
  public filteredPositions: PublicPosition[] = [];
  public locationOptions: string[] = [];
  public jobTypeOptions: string[] = [];
  public workModeOptions: string[] = [];
  public skillOptions: string[] = [];
  public selectedLocation: string = ALL_LOCATIONS;
  public selectedJobType: string = ALL_JOB_TYPES;
  public selectedWorkMode: string = ALL_WORK_MODES;
  public selectedSkill: string = ALL_SKILLS;
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
          this.allPositions = response.data || [];
          this.locationOptions = deriveLocationOptions(this.allPositions);
          this.jobTypeOptions = deriveJobTypeOptions(this.allPositions);
          this.workModeOptions = deriveWorkModeOptions(this.allPositions);
          this.skillOptions = deriveSkillOptions(this.allPositions);
          this.selectedLocation = ALL_LOCATIONS;
          this.selectedJobType = ALL_JOB_TYPES;
          this.selectedWorkMode = ALL_WORK_MODES;
          this.selectedSkill = ALL_SKILLS;
          this.filteredPositions = this.allPositions;
        },
        error: error => {
          console.error('Error loading public positions:', error);
          this.error = 'Unable to load positions. Please try again later.';
          this.allPositions = [];
          this.filteredPositions = [];
          this.locationOptions = [];
          this.jobTypeOptions = [];
          this.workModeOptions = [];
          this.skillOptions = [];
          this.selectedLocation = ALL_LOCATIONS;
          this.selectedJobType = ALL_JOB_TYPES;
          this.selectedWorkMode = ALL_WORK_MODES;
          this.selectedSkill = ALL_SKILLS;
        },
      });
  }

  public onLocationFilterChange(location: string): void {
    this.selectedLocation = location;
    this.applyFilters();
  }

  public onJobTypeFilterChange(jobType: string): void {
    this.selectedJobType = jobType;
    this.applyFilters();
  }

  public onWorkModeFilterChange(workMode: string): void {
    this.selectedWorkMode = workMode;
    this.applyFilters();
  }

  public onSkillFilterChange(skill: string): void {
    this.selectedSkill = skill;
    this.applyFilters();
  }

  private applyFilters(): void {
    this.filteredPositions = filterPositions(
      this.allPositions,
      this.selectedLocation,
      this.selectedJobType,
      this.selectedWorkMode,
      this.selectedSkill,
    );
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
