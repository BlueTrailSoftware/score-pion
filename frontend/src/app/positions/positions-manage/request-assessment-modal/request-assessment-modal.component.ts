import { Component, EventEmitter, OnInit, Output, inject } from '@angular/core';
import { FormGroup, FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-request-assessment-modal',
  templateUrl: './request-assessment-modal.component.html',
  styleUrl: './request-assessment-modal.component.scss',
  imports: [ReactiveFormsModule],
})
export class RequestAssessmentModalComponent implements OnInit {
  @Output() closeModal = new EventEmitter<void>();

  private adminService = inject(AdminService);
  private fb = inject(FormBuilder);
  private notificationService = inject(NotificationService);

  examForm: FormGroup;
  loading: boolean = false;
  minDate: string = '';

  constructor() {
    this.examForm = this.fb.group({
      examDate: ['', Validators.required],
      description: ['', [Validators.required, Validators.minLength(10)]],
    });
  }

  ngOnInit() {
    this.initializeForm();
  }

  /**
   * Initialize minimum date
   */
  private initializeForm(): void {
    const today = new Date();
    this.minDate = today.toISOString().split('T')[0];
  }

  /**
   * Check if a field has a specific error
   */
  hasError(field: string, error: string): boolean {
    const control = this.examForm.get(field);
    return !!(control?.hasError(error) && (control?.dirty || control?.touched));
  }

  /**
   * Check if field is invalid
   */
  isFieldInvalid(field: string): boolean {
    const control = this.examForm.get(field);
    return !!(control?.invalid && (control?.dirty || control?.touched));
  }

  /**
   * Get error message for field
   */
  getErrorMessage(field: string): string {
    const control = this.examForm.get(field);

    if (control?.hasError('required')) {
      return 'This field is required';
    }

    if (field === 'description' && control?.hasError('minlength')) {
      return 'Please provide a more detailed description (at least 10 characters)';
    }

    return '';
  }

  /**
   * Format date to "Mon DD, YYYY" format
   */
  private formatDate(dateString: string): string {
    const date = new Date(dateString);
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const month = months[date.getMonth()];
    const day = date.getDate();
    const year = date.getFullYear();

    return `${month} ${day}, ${year}`;
  }

  /**
   * Handle form submission
   */
  onSubmit(): void {
    if (this.examForm.invalid) {
      Object.keys(this.examForm.controls).forEach(key => {
        const control = this.examForm.get(key);
        if (control?.value || control?.touched) {
          control?.markAsTouched();
        }
      });

      const touchedInvalidFields = Object.keys(this.examForm.controls).filter(key => {
        const control = this.examForm.get(key);
        return control?.invalid && control?.touched;
      });

      if (touchedInvalidFields.length > 0) {
        this.notificationService.error('Please correct the errors in the form before submitting');
      } else {
        this.notificationService.error('Please fill in all required fields');
      }
      return;
    }

    const selectedDate = new Date(this.examForm.value.examDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1);

    if (selectedDate < yesterday) {
      this.notificationService.error('The exam date cannot be in the past. Please select today or a future date');
      return;
    }

    this.loading = true;

    const requestData = {
      readyDate: this.formatDate(this.examForm.value.examDate),
      description: this.examForm.value.description,
    };

    this.adminService.createExamTicket(requestData).subscribe({
      next: () => {
        this.loading = false;
        this.notificationService.success("Exam request submitted successfully! You will be notified when it's ready.");
        this.onClose();
      },
      error: error => {
        this.loading = false;
        this.notificationService.error(
          error.error?.message || 'An error occurred while submitting the request. Please try again.',
        );
      },
    });
  }

  /**
   * Close modal
   */
  onClose(): void {
    this.examForm.reset();
    this.closeModal.emit();
  }

  /**
   * Handle click outside modal (backdrop)
   */
  onBackdropClick(): void {
    this.onClose();
  }
}
