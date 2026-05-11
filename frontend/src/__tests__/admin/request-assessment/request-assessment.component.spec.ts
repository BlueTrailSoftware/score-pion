import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { RequestAssessmentModalComponent } from '../../../app/positions/positions-manage/request-assessment-modal/request-assessment-modal.component';
import { AdminService } from '../../../app/services/admin.service';
import { NotificationService } from '../../../app/services/notification.service';

describe('RequestAssessmentModalComponent', () => {
  let component: RequestAssessmentModalComponent;
  let fixture: ComponentFixture<RequestAssessmentModalComponent>;
  let adminServiceSpy: jasmine.SpyObj<AdminService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  beforeEach(async () => {
    adminServiceSpy = jasmine.createSpyObj('AdminService', ['createExamTicket']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    await TestBed.configureTestingModule({
      imports: [RequestAssessmentModalComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AdminService, useValue: adminServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RequestAssessmentModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set minDate on init', () => {
    const today = new Date().toISOString().split('T')[0];
    expect(component.minDate).toBe(today);
  });

  describe('form validation', () => {
    it('should be invalid when empty', () => {
      expect(component.examForm.invalid).toBeTrue();
    });

    it('isFieldInvalid should return false for untouched fields', () => {
      expect(component.isFieldInvalid('examDate')).toBeFalse();
    });

    it('isFieldInvalid should return true after touching an empty field', () => {
      component.examForm.get('examDate')?.markAsTouched();
      expect(component.isFieldInvalid('examDate')).toBeTrue();
    });

    it('getErrorMessage should return required message', () => {
      component.examForm.get('examDate')?.markAsTouched();
      expect(component.getErrorMessage('examDate')).toBe('This field is required');
    });

    it('getErrorMessage should return minlength message for description', () => {
      component.examForm.get('description')?.setValue('short');
      component.examForm.get('description')?.markAsTouched();
      expect(component.getErrorMessage('description')).toBe(
        'Please provide a more detailed description (at least 10 characters)',
      );
    });
  });

  describe('onSubmit', () => {
    it('should show error and not call service when form is invalid', () => {
      component.onSubmit();
      expect(adminServiceSpy.createExamTicket).not.toHaveBeenCalled();
    });

    it('should call createExamTicket and show success on valid submit', fakeAsync(() => {
      adminServiceSpy.createExamTicket.and.returnValue(of({ status: 'success', message: 'OK' }));

      const futureDate = new Date();
      futureDate.setDate(futureDate.getDate() + 7);
      const dateStr = futureDate.toISOString().split('T')[0];

      component.examForm.setValue({ examDate: dateStr, description: 'A detailed description here' });
      component.onSubmit();
      tick();

      expect(adminServiceSpy.createExamTicket).toHaveBeenCalled();
      expect(notificationServiceSpy.success).toHaveBeenCalledWith(
        "Exam request submitted successfully! You will be notified when it's ready.",
      );
      expect(component.loading).toBeFalse();
    }));

    it('should show error notification on service failure', fakeAsync(() => {
      adminServiceSpy.createExamTicket.and.returnValue(throwError(() => ({ error: { message: 'Server error' } })));

      const futureDate = new Date();
      futureDate.setDate(futureDate.getDate() + 7);
      component.examForm.setValue({
        examDate: futureDate.toISOString().split('T')[0],
        description: 'A detailed description here',
      });

      component.onSubmit();
      tick();

      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Server error');
      expect(component.loading).toBeFalse();
    }));
  });
});
