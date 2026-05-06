import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { UserProfile, RecruiterPosition } from '../../models/user-profile.model';
import { InviteRecruiterRequest } from '../../models/recruiter.model';
import { catchError, of, tap } from 'rxjs';
import { AdminService } from '../../services/admin.service';
import { NotificationService } from '../../services/notification.service';
import { AuthService } from '../../services/auth.service';
import { GradientPageHeaderComponent } from '../../shared/gradient-page-header/gradient-page-header.component';
import { DatePipe, LowerCasePipe } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { TabsComponent, TabOption } from '../../shared/tabs/tabs.component';
import { StatusBadgePipe } from '../../shared/pipes/status-badge.pipe';
import { ActionDropdownComponent } from '../../shared/action-dropdown/action-dropdown.component';

@Component({
  selector: 'app-manage-recruiters',
  templateUrl: './manage-recruiters.component.html',
  styleUrls: ['./manage-recruiters.component.scss'],
  imports: [
    GradientPageHeaderComponent,
    DatePipe,
    LowerCasePipe,
    ReactiveFormsModule,
    TabsComponent,
    StatusBadgePipe,
    ActionDropdownComponent,
  ],
})
export class ManageRecruitersComponent implements OnInit {
  private adminService = inject(AdminService);
  private notificationService = inject(NotificationService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  public recruiters: UserProfile[] = [];
  public admins: UserProfile[] = [];
  public positions: RecruiterPosition[] = [];
  public loading: boolean = false;
  public selectedRecruiter: UserProfile | null = null;
  public showAssignModal: boolean = false;
  public actionLoading: boolean = false;
  public selectedPositions = new Set<string>();
  public activeTab: 'recruiters' | 'admins' = 'recruiters';

  public tabOptions: TabOption[] = [
    { id: 'recruiters', label: 'Recruiters', count: 0 },
    { id: 'admins', label: 'Admins', count: 0 },
  ];

  public showInviteModal: boolean = false;
  public inviteLoading: boolean = false;
  public inviteSelectedPositions = new Set<string>();
  public inviteForm: FormGroup = this.fb.group({
    role: ['RECRUITER'],
    email: ['', [Validators.required, Validators.email]],
  });

  get inviteRoleControl() {
    return this.inviteForm.get('role');
  }

  get inviteEmailControl() {
    return this.inviteForm.get('email');
  }

  get inviteRoleName(): string {
    return this.inviteRoleControl?.value === 'ADMIN' ? 'Admin' : 'Recruiter';
  }

  setTab(tab: string): void {
    this.activeTab = tab as 'recruiters' | 'admins';
  }

  ngOnInit(): void {
    this.loadPositions();
    this.loadRecruiters();
    this.loadAdmins();
  }

  /**
   * Load all available positions in the system
   */
  loadPositions(): void {
    this.adminService
      .getAllPositions(true)
      .pipe(
        tap(result => {
          this.positions = result.items;
        }),
        catchError(error => {
          console.error('Error fetching positions:', error);
          this.notificationService.error('Failed to load positions');
          return [];
        }),
      )
      .subscribe();
  }

  /**
   * Load all recruiters with their position details
   */
  loadRecruiters(): void {
    this.loading = true;
    this.adminService
      .getAllRecruiters()
      .pipe(
        tap(result => {
          this.recruiters = result.items;
          this.tabOptions[0].count = result.total;
          this.loading = false;
        }),
        catchError(error => {
          console.error('Error fetching recruiters:', error);
          this.notificationService.error('Failed to load recruiters');
          this.loading = false;
          return of({ items: [], total: 0, page: 0, pageSize: 50, totalPages: 0 });
        }),
      )
      .subscribe();
  }

  /**
   * Load all admins
   */
  loadAdmins(): void {
    this.adminService
      .getAllAdmins()
      .pipe(
        tap(result => {
          this.admins = result.items;
          this.tabOptions[1].count = result.total;
        }),
        catchError(error => {
          console.error('Error fetching admins:', error);
          return of({ items: [], total: 0, page: 0, pageSize: 50, totalPages: 0 });
        }),
      )
      .subscribe();
  }

  /**
   * Toggle user active/inactive status
   */
  toggleActiveStatus(user: UserProfile): void {
    if (!user.id) return;

    this.actionLoading = true;
    const newStatus = !user.isActive;

    const request$ =
      this.activeTab === 'admins'
        ? this.adminService.updateAdminActiveStatus(user.id, newStatus)
        : this.adminService.updateActiveStatus(user.id, newStatus);

    request$
      .pipe(
        tap({
          next: () => {
            user.isActive = newStatus;
            this.notificationService.success(
              `${this.activeTab === 'admins' ? 'Admin' : 'Recruiter'} ${newStatus ? 'activated' : 'deactivated'} successfully`,
            );
            this.actionLoading = false;
          },
          error: () => {
            this.notificationService.error('Failed to update status');
            this.actionLoading = false;
          },
        }),
      )
      .subscribe();
  }

  /**
   * Check if the given user is the currently logged-in admin
   */
  isCurrentUser(user: UserProfile): boolean {
    return user.id === this.authService.getCurrentUserId();
  }

  /**
   * Open modal to assign positions to recruiter
   */
  openAssignModal(recruiter: UserProfile): void {
    if (!recruiter.id) return;

    this.selectedRecruiter = recruiter;
    this.selectedPositions.clear();

    // Load recruiter's current positions and pre-select them
    this.adminService
      .getRecruiterPositions(recruiter.id)
      .pipe(
        tap(result => {
          result.items.forEach(position => this.selectedPositions.add(position.id));
          this.showAssignModal = true;
        }),
        catchError(error => {
          console.error('Error fetching recruiter positions:', error);
          this.notificationService.error('Failed to load recruiter positions');
          this.showAssignModal = true;
          return [];
        }),
      )
      .subscribe();
  }

  /**
   * Close assignment modal and clear selection
   */
  closeAssignModal(): void {
    this.showAssignModal = false;
    this.selectedRecruiter = null;
    this.selectedPositions.clear();
  }

  /**
   * Toggle position selection
   */
  togglePositionSelection(positionId: string): void {
    if (this.selectedPositions.has(positionId)) {
      this.selectedPositions.delete(positionId);
    } else {
      this.selectedPositions.add(positionId);
    }
  }

  /**
   * Check if position is selected
   */
  isPositionSelected(positionId: string): boolean {
    return this.selectedPositions.has(positionId);
  }

  /**
   * Save position assignments for selected recruiter
   */
  saveAssignments(): void {
    if (!this.selectedRecruiter?.id) return;

    this.actionLoading = true;

    this.adminService
      .assignPositions(this.selectedRecruiter.id, Array.from(this.selectedPositions))
      .pipe(
        tap({
          next: () => {
            this.notificationService.success('Positions assigned successfully');
            this.actionLoading = false;
            this.closeAssignModal();
            this.loadRecruiters();
          },
          error: err => {
            this.notificationService.error(err?.error?.message || 'Failed to assign positions');
            this.actionLoading = false;
          },
        }),
      )
      .subscribe();
  }

  /**
   * Navigate to create position page
   */
  goToCreatePosition(): void {
    this.router.navigate(['/positions-manage/create']);
  }

  openInviteModal(): void {
    this.inviteForm.reset({ role: 'RECRUITER', email: '' });
    this.inviteSelectedPositions.clear();
    this.showInviteModal = true;
  }

  closeInviteModal(): void {
    this.showInviteModal = false;
    this.inviteForm.reset({ role: 'RECRUITER', email: '' });
    this.inviteSelectedPositions.clear();
    this.inviteLoading = false;
  }

  selectInviteRole(role: 'RECRUITER' | 'ADMIN'): void {
    this.inviteRoleControl?.setValue(role);
    if (role === 'ADMIN') {
      this.inviteSelectedPositions.clear();
    }
  }

  onInvitePositionChange(positionId: string, checked: boolean): void {
    if (checked) {
      this.inviteSelectedPositions.add(positionId);
    } else {
      this.inviteSelectedPositions.delete(positionId);
    }
  }

  onSubmitInvite(): void {
    if (this.inviteForm.invalid) {
      this.inviteForm.markAllAsTouched();
      return;
    }

    this.inviteLoading = true;
    const role = this.inviteForm.value.role;

    if (role === 'RECRUITER') {
      const request: InviteRecruiterRequest = {
        email: this.inviteForm.value.email,
        positionIds: Array.from(this.inviteSelectedPositions),
      };
      this.adminService
        .inviteRecruiter(request)
        .pipe(
          tap({
            next: () => {
              this.notificationService.success('Recruiter invitation sent successfully!');
              this.closeInviteModal();
              this.loadRecruiters();
            },
            error: err => {
              this.notificationService.error(err?.error?.message || 'Failed to send invitation. Please try again.');
              this.inviteLoading = false;
            },
          }),
        )
        .subscribe();
    } else {
      this.adminService
        .inviteAdmin({ email: this.inviteForm.value.email })
        .pipe(
          tap({
            next: () => {
              this.notificationService.success('Admin invitation sent successfully!');
              this.closeInviteModal();
              this.loadAdmins();
            },
            error: err => {
              this.notificationService.error(
                err?.error?.message || 'Failed to send admin invitation. Please try again.',
              );
              this.inviteLoading = false;
            },
          }),
        )
        .subscribe();
    }
  }

  getRecruiterActions(recruiter: UserProfile) {
    if (recruiter.status === 'Pending' || !recruiter.id) return [];

    const actions = [
      {
        label: 'Manage Positions',
        icon: 'bi-pencil',
        colorClass: 'text-dark',
        action: () => this.openAssignModal(recruiter),
      },
      {
        label: recruiter.isActive ? 'Deactivate' : 'Activate',
        icon: recruiter.isActive ? 'bi-person-x' : 'bi-person-check',
        colorClass: recruiter.isActive ? 'text-danger' : 'text-success',
        action: () => this.toggleActiveStatus(recruiter),
      },
    ];

    return actions;
  }

  getAdminActions(admin: UserProfile) {
    if (admin.status === 'Pending' || !admin.id) return [];

    return [
      {
        label: admin.isActive ? 'Deactivate' : 'Activate',
        icon: admin.isActive ? 'bi-person-x' : 'bi-person-check',
        colorClass: admin.isActive ? 'text-danger' : 'text-success',
        action: () => this.toggleActiveStatus(admin),
        disabled: admin.isActive && this.isCurrentUser(admin),
      },
    ];
  }
}
