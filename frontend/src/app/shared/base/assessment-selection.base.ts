import { Directive, OnInit, inject } from '@angular/core';
import { catchError, tap, EMPTY } from 'rxjs';
import { Assessment } from '../../models/assessment.model';
import { AssessmentsService } from '../../services/assessments.service';
import { ApiResponse } from '../../models/responses/api-base-response.model';

/**
 * Base class for components that need assessment selection functionality
 * Provides common logic for loading and selecting assessments
 * Backend filters assessments by role automatically (ADMIN sees all, RECRUITER sees assigned)
 */
@Directive()
export abstract class AssessmentSelectionBase implements OnInit {
  protected assessmentsService = inject(AssessmentsService);
  public assessments: Assessment[] = [];
  public selectedAssessments: Set<string> = new Set();

  ngOnInit(): void {
    this.loadAssessments();
  }

  /**
   * Loads assessments from the service
   * Backend automatically filters by user role
   */
  protected loadAssessments(): void {
    this.assessmentsService
      .getAssessments()
      .pipe(
        tap((response: ApiResponse<Assessment[]>) => {
          this.assessments = response.data || [];
          this.onAssessmentsLoaded(this.assessments);
        }),
        catchError(error => {
          console.error('Error fetching assessments:', error);
          this.onAssessmentsLoadError(error);
          return EMPTY;
        }),
      )
      .subscribe();
  }

  /**
   * Handles assessment checkbox change
   * @param testID The assessment ID
   * @param checked Whether the checkbox is checked
   */
  public onAssessmentChange(testID: string, checked: boolean): void {
    if (checked) {
      this.selectedAssessments.add(testID);
    } else {
      this.selectedAssessments.delete(testID);
    }
  }

  /**
   * Hook called after assessments are successfully loaded
   * Can be overridden by child components for custom behavior
   */
  protected onAssessmentsLoaded(_assessments: Assessment[]): void {
    // Default: do nothing
  }

  /**
   * Hook called when assessments fail to load
   * Must be implemented by child components to handle errors appropriately
   */
  protected abstract onAssessmentsLoadError(error: unknown): void;
}
