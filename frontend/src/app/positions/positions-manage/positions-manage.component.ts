import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { PositionListItem } from '../../models/position.model';
import { NotificationService } from '../../services/notification.service';
import { PositionService } from '../../services/position.service';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { GradientPageHeaderComponent } from '../../shared/gradient-page-header/gradient-page-header.component';
import { PositionListCardComponent } from '../../components/position-list-card/position-list-card.component';
import { RequestAssessmentModalComponent } from './request-assessment-modal/request-assessment-modal.component';
import { ActionDropdownComponent } from '../../shared/action-dropdown/action-dropdown.component';

@Component({
  selector: 'app-positions-manage.component',
  templateUrl: './positions-manage.component.html',
  styleUrl: './positions-manage.component.scss',
  imports: [
    ReactiveFormsModule,
    FormsModule,
    GradientPageHeaderComponent,
    PositionListCardComponent,
    RequestAssessmentModalComponent,
    ActionDropdownComponent,
  ],
})
export class PositionsManageComponent {
  private positionService = inject(PositionService);
  private notificationService = inject(NotificationService);
  private router = inject(Router);

  // Modal state - Invite Candidate
  public showModal: boolean = false;

  // Candidate information for modal
  public candidateEmail: string = '';
  public candidateName: string = '';
  public emailError: string = '';
  public nameError: string = '';

  // Position selection
  public selectedPosition: PositionListItem | null = null;
  public positions: PositionListItem[] = [];

  // Loading states
  public loading: boolean = false;
  public modalLoading: boolean = false;

  // Error state
  public positionsError: string = '';

  // Modal state - Request Assessment
  public showRequestAssessmentModal: boolean = false;

  public userAdmin: boolean = false;

  constructor() {
    this.userAdmin = localStorage.getItem('permissions') == 'ADMIN';
  }

  /**
   * Validate email format using regex
   */
  validateEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }

  /**
   * Validate name (not empty and reasonable length)
   */
  validateName(name: string): boolean {
    if (!name) return false;
    const trimmedName = name.trim();
    return trimmedName.length >= 2 && trimmedName.length <= 100;
  }

  onPositionsLoaded(positions: PositionListItem[]): void {
    this.positions = positions;
  }

  onViewPositionDetails(position: PositionListItem): void {
    this.router.navigate(['/positions-manage', position.id]);
  }

  onInviteToPosition(position: PositionListItem): void {
    this.selectedPosition = position;
    this.showModal = true;

    this.candidateEmail = '';
    this.candidateName = '';
    this.emailError = '';
    this.nameError = '';
  }

  /**
   * Close the modal and reset form
   */
  closeModal(): void {
    this.showModal = false;
    this.selectedPosition = null;
    this.candidateEmail = '';
    this.candidateName = '';
    this.emailError = '';
    this.nameError = '';
    this.modalLoading = false;
  }

  /**
   * Validate modal form
   */
  validateModalForm(): boolean {
    this.emailError = '';
    this.nameError = '';

    let isValid = true;

    if (!this.candidateEmail || !this.validateEmail(this.candidateEmail)) {
      this.emailError = 'Please enter a valid email address.';
      isValid = false;
    }

    if (!this.candidateName || !this.validateName(this.candidateName)) {
      this.nameError = 'Please enter a valid candidate name (2-100 characters).';
      isValid = false;
    }

    return isValid;
  }

  /**
   * Handle modal form submission
   */
  onSubmitModal(): void {
    if (!this.validateModalForm() || !this.selectedPosition) {
      return;
    }

    this.modalLoading = true;

    this.positionService
      .inviteCandidateToPosition(this.candidateEmail, this.candidateName.trim(), this.selectedPosition.id)
      .pipe(
        tap({
          next: () => {
            this.notificationService.success(`Invitation sent successfully to ${this.candidateName}!`);
            this.modalLoading = false;
            this.closeModal();
          },
          error: err => {
            this.notificationService.error(err?.error?.message || 'Failed to send invitation. Please try again.');
            this.modalLoading = false;
          },
        }),
      )
      .subscribe();
  }

  public goToCreatePosition(): void {
    this.router.navigate(['/positions-manage/create']);
  }

  /**
   * Open the Request Assessment modal
   */
  public openRequestAssessmentModal(): void {
    this.showRequestAssessmentModal = true;
  }

  /**
   * Close the Request Assessment modal
   */
  public closeRequestAssessmentModal(): void {
    this.showRequestAssessmentModal = false;
  }

  /**
   * Get selected position title for modal display
   */
  get selectedPositionTitle(): string {
    return this.selectedPosition ? this.selectedPosition.title : '';
  }

  get actionItems() {
    return [
      {
        label: 'Create New Position',
        icon: 'bi-plus-lg',
        colorClass: 'text-dark',
        action: () => this.goToCreatePosition(),
      },
      {
        label: 'Request Assessment',
        icon: 'bi-clipboard-check',
        colorClass: 'text-dark',
        action: () => this.openRequestAssessmentModal(),
      },
    ];
  }
}
