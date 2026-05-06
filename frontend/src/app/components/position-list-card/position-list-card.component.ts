import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { PositionListItem, PositionFilters } from '../../models/position.model';
import { PositionService } from '../../services/position.service';

export type PositionListMode = 'view' | 'invite';

@Component({
  selector: 'app-position-list-card',
  templateUrl: './position-list-card.component.html',
  styleUrls: ['./position-list-card.component.scss'],
  imports: [CommonModule],
})
export class PositionListCardComponent implements OnInit, OnDestroy {
  private positionService = inject(PositionService);

  @Input() mode: PositionListMode = 'view';
  @Input() showStatus: boolean = false;
  @Input() showInactive: boolean = false;
  @Input() autoLoad: boolean = true;
  @Input() title: string = 'Positions';
  @Input() subtitle: string = 'Browse and select from available positions';
  @Input() showHeader: boolean = true;

  @Output() positionView = new EventEmitter<PositionListItem>();
  @Output() positionInvite = new EventEmitter<PositionListItem>();
  @Output() positionsLoaded = new EventEmitter<PositionListItem[]>();

  public positions: PositionListItem[] = [];
  public loading: boolean = false;
  public error: string = '';
  private destroy$ = new Subject<void>();

  ngOnInit() {
    if (this.autoLoad) {
      this.loadPositions();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadPositions(): void {
    this.loading = true;
    this.error = '';

    const filters: PositionFilters = {
      activeOnly: !this.showInactive,
      page: 1,
      pageSize: 50,
    };

    this.positionService
      .getPositions(filters)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: result => {
          this.positions = result.items;
          this.loading = false;
          this.positionsLoaded.emit(this.positions);
        },
        error: (error: Error) => {
          this.error = 'Error loading positions';
          this.loading = false;
          console.error('Error loading positions:', error);
        },
      });
  }

  onPositionClick(position: PositionListItem): void {
    this.positionView.emit(position);
  }

  onInvite(position: PositionListItem, event: Event): void {
    event.stopPropagation();
    this.positionInvite.emit(position);
  }

  getInviteButtonTooltip(position: PositionListItem): string {
    if (!position.isActive) {
      return 'Position is inactive';
    }
    if (position.assessmentsCount === 0) {
      return 'No assessments assigned to this position';
    }
    return 'Invite a candidate to this position';
  }

  public reloadPositions(): void {
    this.loadPositions();
  }

  public setPositions(positions: PositionListItem[]): void {
    this.positions = positions;
    this.loading = false;
  }
}
