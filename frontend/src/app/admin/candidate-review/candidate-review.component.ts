import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CandidatesService } from '../../services/candidates.service';
import { PositionService } from '../../services/position.service';
import { AdminService } from '../../services/admin.service';
import { NotificationService } from '../../services/notification.service';
import { AuthService } from '../../services/auth.service';
import { Candidate, CandidateInvitation } from '../../models/candidate.model';
import { emptyPagedData } from '../../models/responses/api-base-response.model';
import { Position } from '../../models/position.model';
import { UserProfile } from '../../models/user-profile.model';
import { GradientPageHeaderComponent } from '../../shared/gradient-page-header/gradient-page-header.component';
import { PaginationComponent } from '../../shared/pagination/pagination.component';
import { AssessmentTableComponent } from '../../shared/assessment-table/assessment-table.component';
import { StatusBadgePipe } from '../../shared/pipes/status-badge.pipe';
import { ActionDropdownComponent, ActionItem } from '../../shared/action-dropdown/action-dropdown.component';
import { TabsComponent, TabOption } from '../../shared/tabs/tabs.component';
import { of } from 'rxjs';
import { catchError, finalize, tap } from 'rxjs/operators';

@Component({
  selector: 'app-candidate-review',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    GradientPageHeaderComponent,
    DatePipe,
    PaginationComponent,
    AssessmentTableComponent,
    StatusBadgePipe,
    ActionDropdownComponent,
    TabsComponent,
  ],
  templateUrl: './candidate-review.component.html',
  styleUrls: ['./candidate-review.component.scss'],
})
export class CandidateReviewComponent implements OnInit {
  private candidatesService = inject(CandidatesService);
  private positionService = inject(PositionService);
  private adminService = inject(AdminService);
  private notificationService = inject(NotificationService);
  private authService = inject(AuthService);
  private router = inject(Router);

  isAdmin = this.authService.isAdmin();

  activeTab: 'applicants' | 'candidates' = 'applicants';

  tabOptions: TabOption[] = [
    { id: 'applicants', label: 'Applicants', count: 0 },
    { id: 'candidates', label: 'Candidates', count: 0 },
  ];

  candidates: Candidate[] = [];
  loading = false;
  actionLoading = false;

  // Applicants pagination
  currentPage = 1;
  pageSize = 10;
  totalCandidates = 0;

  // Applicants filters & sort
  filterStatus: string = '';
  filterSearch: string = '';
  sortField: string = 'createdAt';
  sortDirection: 'asc' | 'desc' = 'desc';

  // Recruiter candidates
  recruiterCandidates: CandidateInvitation[] = [];
  loadingRecruiterCandidates = false;
  currentPageRecruiter = 1;
  totalRecruiterCandidates = 0;

  // Recruiter filter
  filterRecruiterId: string = '';
  recruiters: UserProfile[] = [];
  recruitersLoaded = false;
  candidatesDataLoaded = false;

  // Invite Modal
  showInviteModal = false;
  selectedCandidate: Candidate | null = null;
  selectedPosition: Position | null = null;
  modalLoading = false;
  modalError: string | null = null;

  // Reject Modal
  showRejectModal = false;
  candidateToReject: Candidate | null = null;
  rejectNote: string = '';

  // Rejection Reason Modal
  showRejectionReasonModal = false;
  rejectionReasonCandidate: Candidate | null = null;

  ngOnInit(): void {
    this.loadCandidates();
    this.loadRecruiterCandidatesCount();
  }

  private loadRecruiterCandidatesCount(): void {
    this.candidatesService.getInvitedCandidates(undefined, undefined, { page: 1, pageSize: 1 }).subscribe({
      next: result => (this.tabOptions[1].count = result.total),
      error: () => {},
    });
  }

  loadCandidates(): void {
    this.loading = true;
    this.candidatesService
      .getCandidates(
        {
          status: this.filterStatus || undefined,
          search: this.filterSearch || undefined,
          sortField: this.sortField,
          sortDirection: this.sortDirection,
        },
        { page: this.currentPage, pageSize: this.pageSize },
      )
      .pipe(
        tap(result => {
          this.candidates = result.items;
          this.totalCandidates = result.total;
          this.tabOptions[0].count = result.total;
        }),
        catchError(error => {
          console.error('Error loading candidates:', error);
          this.notificationService.error('Failed to load candidates');
          return of(null);
        }),
        finalize(() => {
          this.loading = false;
        }),
      )
      .subscribe();
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.loadCandidates();
  }

  onSortChange(field: string): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.loadCandidates();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadCandidates();
  }

  openInviteModal(candidate: Candidate): void {
    this.selectedCandidate = candidate;
    this.selectedPosition = null;
    this.modalError = null;
    this.showInviteModal = true;
    this.loadPosition(candidate.positionId);
  }

  private loadPosition(positionId: string): void {
    this.modalLoading = true;
    this.positionService
      .getPositionById(positionId)
      .pipe(finalize(() => (this.modalLoading = false)))
      .subscribe({
        next: response => (this.selectedPosition = response.data ?? null),
        error: () => (this.modalError = 'Failed to load position details.'),
      });
  }

  closeInviteModal(): void {
    this.showInviteModal = false;
    this.selectedCandidate = null;
    this.selectedPosition = null;
    this.modalError = null;
  }

  confirmInvite(): void {
    if (!this.selectedCandidate || this.actionLoading) return;

    this.actionLoading = true;
    this.candidatesService.inviteCandidate(this.selectedCandidate.id).subscribe({
      next: () => {
        this.notificationService.success(`Candidate ${this.selectedCandidate!.name} invited.`);
        this.closeInviteModal();
        this.loadCandidates();
        this.actionLoading = false;
      },
      error: () => {
        this.notificationService.error('Failed to invite candidate.');
        this.actionLoading = false;
      },
    });
  }

  get hasAssessments(): boolean {
    return (this.selectedPosition?.assessments?.length ?? 0) > 0;
  }

  openRejectModal(candidate: Candidate): void {
    this.candidateToReject = candidate;
    this.rejectNote = '';
    this.showRejectModal = true;
  }

  closeRejectModal(): void {
    this.showRejectModal = false;
    this.candidateToReject = null;
    this.rejectNote = '';
  }

  confirmReject(): void {
    if (!this.candidateToReject || this.actionLoading) return;

    this.actionLoading = true;
    this.candidatesService.rejectCandidate(this.candidateToReject.id, this.rejectNote).subscribe({
      next: () => {
        this.notificationService.success(`Candidate ${this.candidateToReject!.name} rejected.`);
        this.closeRejectModal();
        this.loadCandidates();
        this.actionLoading = false;
      },
      error: () => {
        this.notificationService.error('Failed to reject candidate.');
        this.actionLoading = false;
      },
    });
  }

  switchTab(tab: string): void {
    this.activeTab = tab as 'applicants' | 'candidates';
    if (tab === 'candidates') {
      if (this.isAdmin && !this.recruitersLoaded) {
        this.loadRecruiters();
      }
      if (!this.candidatesDataLoaded) {
        this.loadRecruiterCandidates();
      }
    }
  }

  loadRecruiters(): void {
    this.adminService.getAllRecruiters().subscribe({
      next: result => {
        this.recruiters = result.items;
        this.recruitersLoaded = true;
      },
      error: () => {
        this.notificationService.error('Failed to load recruiters');
      },
    });
  }

  loadRecruiterCandidates(): void {
    this.loadingRecruiterCandidates = true;
    this.candidatesService
      .getInvitedCandidates(this.filterRecruiterId || undefined, undefined, {
        page: this.currentPageRecruiter,
        pageSize: this.pageSize,
      })
      .pipe(
        catchError(error => {
          console.error('Error loading recruiter candidates:', error);
          this.notificationService.error('Failed to load recruiter candidates');
          return of(emptyPagedData<CandidateInvitation>(this.pageSize));
        }),
        finalize(() => {
          this.loadingRecruiterCandidates = false;
        }),
      )
      .subscribe(result => {
        this.recruiterCandidates = result.items;
        this.totalRecruiterCandidates = result.total;
        this.tabOptions[1].count = result.total;
        this.candidatesDataLoaded = true;
      });
  }

  onRecruiterFilterChange(): void {
    this.currentPageRecruiter = 1;
    this.candidatesDataLoaded = false;
    this.loadRecruiterCandidates();
  }

  onPageChangeRecruiter(page: number): void {
    this.currentPageRecruiter = page;
    this.loadRecruiterCandidates();
  }

  getRecruiterName(id: string): string {
    return this.recruiters.find(r => r.id === id)?.name ?? id;
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'INVITED':
        return 'badge-status-invited';
      case 'REJECTED':
        return 'badge-status-rejected';
      case 'PENDING':
        return 'badge-status-pending';
      default:
        return 'badge-status-unknown';
    }
  }

  getStatusDescription(status: string): string {
    switch (status) {
      case 'INVITED':
        return 'Invited to assessments';
      case 'REJECTED':
        return 'Application rejected';
      case 'PENDING':
        return 'Pending review';
      default:
        return 'Unknown status';
    }
  }

  getFileIcon(fileUrl: string | undefined): string {
    if (!fileUrl) return 'bi-file-earmark';

    let fileName = 'unknown';
    try {
      const url = new URL(fileUrl);
      const pathParts = url.pathname.split('/');
      fileName = pathParts[pathParts.length - 1] || 'unknown';
    } catch {
      console.warn('getFileIcon: invalid fileUrl:', fileUrl);
      const parts = fileUrl.split('/');
      fileName = parts[parts.length - 1] || fileUrl;
    }

    fileName = (fileName || '').split(/[?#]/)[0].toLowerCase();

    if (fileName.endsWith('.pdf')) return 'bi-file-earmark-pdf';
    if (fileName.endsWith('.doc') || fileName.endsWith('.docx')) return 'bi-file-earmark-word';
    if (fileName.endsWith('.jpg') || fileName.endsWith('.jpeg') || fileName.endsWith('.png'))
      return 'bi-file-earmark-image';

    return 'bi-file-earmark';
  }

  downloadFile(fileUrl: string | undefined): void {
    if (fileUrl) {
      window.open(fileUrl, '_blank');
    }
  }

  getCandidateActions(candidate: Candidate): ActionItem[] {
    return [
      {
        label: 'Invite',
        icon: 'bi-envelope-check',
        colorClass: 'text-success',
        action: () => this.openInviteModal(candidate),
      },
      {
        label: 'Reject',
        icon: 'bi-x-circle',
        colorClass: 'text-danger',
        action: () => this.openRejectModal(candidate),
      },
    ];
  }

  openRejectionReasonModal(candidate: Candidate): void {
    this.rejectionReasonCandidate = candidate;
    this.showRejectionReasonModal = true;
  }

  closeRejectionReasonModal(): void {
    this.showRejectionReasonModal = false;
    this.rejectionReasonCandidate = null;
  }

  goToEditPosition(): void {
    if (this.selectedPosition) {
      const positionId = this.selectedPosition.id;
      this.closeInviteModal();
      this.router.navigate(['/positions-manage', positionId], {
        queryParams: { edit: 'true' },
      });
    }
  }
}
