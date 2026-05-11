import { Directive, OnInit, inject } from '@angular/core';
import { catchError, tap, EMPTY } from 'rxjs';
import { RecruiterPosition } from '../../models/user-profile.model';
import { AdminService } from '../../services/admin.service';

/**
 * Base class for components that need position selection functionality
 * Provides common logic for loading and selecting positions
 */
@Directive()
export abstract class PositionSelectionBase implements OnInit {
  protected adminService = inject(AdminService);
  public positions: RecruiterPosition[] = [];
  public selectedPositions: Set<string> = new Set();

  ngOnInit(): void {
    this.loadPositions();
  }

  /**
   * Loads active positions from the service
   */
  protected loadPositions(): void {
    this.adminService
      .getAllPositions(true)
      .pipe(
        tap(result => {
          this.positions = result.items;
          this.onPositionsLoaded(result.items);
        }),
        catchError(error => {
          console.error('Error fetching positions:', error);
          this.onPositionsLoadError(error);
          return EMPTY;
        }),
      )
      .subscribe();
  }

  /**
   * Handles position checkbox change
   * @param positionId The position ID
   * @param checked Whether the checkbox is checked
   */
  public onPositionChange(positionId: string, checked: boolean): void {
    if (checked) {
      this.selectedPositions.add(positionId);
    } else {
      this.selectedPositions.delete(positionId);
    }
  }

  /**
   * Hook called after positions are successfully loaded
   * Can be overridden by child components for custom behavior
   */
  protected onPositionsLoaded(_positions: RecruiterPosition[]): void {
    // Default: do nothing
  }

  /**
   * Hook called when positions fail to load
   * Must be implemented by child components to handle errors appropriately
   */
  protected abstract onPositionsLoadError(error: unknown): void;
}
