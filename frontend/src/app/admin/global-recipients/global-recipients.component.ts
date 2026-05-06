import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { NotificationService } from '../../services/notification.service';
import { GradientPageHeaderComponent } from '../../shared/gradient-page-header/gradient-page-header.component';
import { ActionDropdownComponent } from '../../shared/action-dropdown/action-dropdown.component';
import { catchError, of, tap, switchMap } from 'rxjs';
import { GlobalRecipient, GlobalRecipientsSettings } from '../../models/global-recipient.model';

@Component({
  selector: 'app-global-recipients',
  standalone: true,
  imports: [CommonModule, FormsModule, GradientPageHeaderComponent, DatePipe, ActionDropdownComponent],
  templateUrl: './global-recipients.component.html',
  styleUrl: './global-recipients.component.scss',
})
export class GlobalRecipientsComponent implements OnInit {
  private adminService = inject(AdminService);
  private notificationService = inject(NotificationService);

  public recipients: GlobalRecipient[] = [];
  public settings: GlobalRecipientsSettings | null = null;
  public loading = false;
  public showModal = false;
  public isEditMode = false;
  public actionLoading = false;
  public searchQuery = '';
  public editingIndex = -1;

  public showDeleteConfirm = false;
  public recipientToDelete: GlobalRecipient | null = null;

  public formData = {
    email: '',
    enabled: true,
  };

  public formErrors = {
    email: '',
  };

  ngOnInit(): void {
    this.loadRecipients();
  }

  loadRecipients(): void {
    this.loading = true;
    this.adminService
      .getGlobalRecipients()
      .pipe(
        tap((settings: GlobalRecipientsSettings) => {
          this.settings = settings;
          this.recipients = (settings.emails || []).map(email => ({
            email,
            enabled: true,
          }));
          this.loading = false;
        }),
        catchError(error => {
          console.error('Error fetching recipients:', error);
          this.notificationService.error('Failed to load recipients');
          this.loading = false;
          return of(null);
        }),
      )
      .subscribe();
  }

  get filteredRecipients(): GlobalRecipient[] {
    if (!this.searchQuery.trim()) {
      return this.recipients;
    }
    const query = this.searchQuery.toLowerCase();
    return this.recipients.filter(r => r.email.toLowerCase().includes(query));
  }

  openAddModal(): void {
    this.isEditMode = false;
    this.editingIndex = -1;
    this.resetForm();
    this.showModal = true;
  }

  openEditModal(recipient: GlobalRecipient, index: number): void {
    this.isEditMode = true;
    this.editingIndex = index;
    this.formData = {
      email: recipient.email,
      enabled: recipient.enabled,
    };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.resetForm();
  }

  resetForm(): void {
    this.formData = {
      email: '',
      enabled: true,
    };
    this.formErrors = {
      email: '',
    };
    this.editingIndex = -1;
  }

  validateEmail(): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!this.formData.email.trim()) {
      this.formErrors.email = 'Email is required';
      return false;
    }
    if (!emailRegex.test(this.formData.email)) {
      this.formErrors.email = 'Invalid email format';
      return false;
    }
    this.formErrors.email = '';
    return true;
  }

  submitForm(): void {
    if (!this.validateEmail()) {
      return;
    }

    const emailExists = this.recipients.some(
      (r, idx) => r.email.toLowerCase() === this.formData.email.toLowerCase() && idx !== this.editingIndex,
    );

    if (emailExists) {
      this.formErrors.email = 'This email already exists';
      return;
    }

    this.actionLoading = true;

    if (this.isEditMode && this.editingIndex >= 0) {
      const oldEmail = this.recipients[this.editingIndex].email;

      this.adminService
        .deleteGlobalRecipient(oldEmail)
        .pipe(
          switchMap(() => this.adminService.addGlobalRecipient(this.formData.email)),
          tap(() => {
            this.notificationService.success('Recipient updated successfully');
            this.actionLoading = false;
            this.closeModal();
            this.loadRecipients();
          }),
          catchError(err => {
            const errorMessage = err?.error?.message || 'Failed to update recipient';
            this.notificationService.error(errorMessage);
            this.actionLoading = false;
            return of(null);
          }),
        )
        .subscribe();
    } else {
      this.adminService
        .addGlobalRecipient(this.formData.email)
        .pipe(
          tap(() => {
            this.notificationService.success('Recipient added successfully');
            this.actionLoading = false;
            this.closeModal();
            this.loadRecipients();
          }),
          catchError(err => {
            const errorMessage = err?.error?.message || 'Failed to add recipient';
            if (
              errorMessage.toLowerCase().includes('duplicate') ||
              errorMessage.toLowerCase().includes('already exists')
            ) {
              this.formErrors.email = 'This email already exists';
            } else {
              this.notificationService.error(errorMessage);
            }
            this.actionLoading = false;
            return of(null);
          }),
        )
        .subscribe();
    }
  }

  openDeleteConfirm(recipient: GlobalRecipient): void {
    this.recipientToDelete = recipient;
    this.showDeleteConfirm = true;
  }

  closeDeleteConfirm(): void {
    this.showDeleteConfirm = false;
    this.recipientToDelete = null;
  }

  confirmDelete(): void {
    if (!this.recipientToDelete) return;

    this.actionLoading = true;

    this.adminService
      .deleteGlobalRecipient(this.recipientToDelete.email)
      .pipe(
        tap(() => {
          this.notificationService.success('Recipient deleted successfully');
          this.actionLoading = false;
          this.closeDeleteConfirm();
          this.loadRecipients();
        }),
        catchError(() => {
          this.notificationService.error('Failed to delete recipient');
          this.actionLoading = false;
          return of(null);
        }),
      )
      .subscribe();
  }

  getRecipientActions(recipient: GlobalRecipient, index: number) {
    return [
      {
        label: 'Edit',
        icon: 'bi-pencil',
        colorClass: 'text-dark',
        action: () => this.openEditModal(recipient, index),
      },
      {
        label: 'Delete',
        icon: 'bi-trash',
        colorClass: 'text-danger',
        action: () => this.openDeleteConfirm(recipient),
      },
    ];
  }
}
