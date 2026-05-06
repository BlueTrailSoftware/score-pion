import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of } from 'rxjs';
import { GlobalRecipientsComponent } from '../../../app/admin/global-recipients/global-recipients.component';
import { AdminService } from '../../../app/services/admin.service';
import { NotificationService } from '../../../app/services/notification.service';
import { GlobalRecipientsSettings } from '../../../app/models/global-recipient.model';

const mockSettings: GlobalRecipientsSettings = {
  emails: ['a@example.com', 'b@example.com'],
  description: 'Global',
  updatedAt: '2024-01-01T00:00:00Z',
  updatedBy: 'admin',
};

describe('GlobalRecipientsComponent', () => {
  let component: GlobalRecipientsComponent;
  let fixture: ComponentFixture<GlobalRecipientsComponent>;
  let adminServiceSpy: jasmine.SpyObj<AdminService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  beforeEach(async () => {
    adminServiceSpy = jasmine.createSpyObj('AdminService', [
      'getGlobalRecipients',
      'addGlobalRecipient',
      'deleteGlobalRecipient',
    ]);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    adminServiceSpy.getGlobalRecipients.and.returnValue(of(mockSettings));

    await TestBed.configureTestingModule({
      imports: [GlobalRecipientsComponent],
      providers: [
        { provide: AdminService, useValue: adminServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(GlobalRecipientsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load recipients on init', () => {
    expect(component.recipients.length).toBe(2);
    expect(component.recipients[0].email).toBe('a@example.com');
  });

  describe('filteredRecipients', () => {
    it('should return all recipients when search is empty', () => {
      expect(component.filteredRecipients.length).toBe(2);
    });

    it('should filter by search query', () => {
      component.searchQuery = 'a@';
      expect(component.filteredRecipients.length).toBe(1);
      expect(component.filteredRecipients[0].email).toBe('a@example.com');
    });
  });

  describe('modal', () => {
    it('openAddModal should show modal in add mode', () => {
      component.openAddModal();
      expect(component.showModal).toBeTrue();
      expect(component.isEditMode).toBeFalse();
    });

    it('openEditModal should show modal in edit mode with prefilled data', () => {
      component.openEditModal({ email: 'a@example.com', enabled: true }, 0);
      expect(component.showModal).toBeTrue();
      expect(component.isEditMode).toBeTrue();
      expect(component.formData.email).toBe('a@example.com');
    });

    it('closeModal should hide modal and reset form', () => {
      component.openAddModal();
      component.formData.email = 'test@example.com';
      component.closeModal();
      expect(component.showModal).toBeFalse();
      expect(component.formData.email).toBe('');
    });
  });

  describe('validateEmail', () => {
    it('should return false and set error when email is empty', () => {
      component.formData.email = '';
      expect(component.validateEmail()).toBeFalse();
      expect(component.formErrors.email).toBe('Email is required');
    });

    it('should return false and set error for invalid email', () => {
      component.formData.email = 'not-an-email';
      expect(component.validateEmail()).toBeFalse();
      expect(component.formErrors.email).toBe('Invalid email format');
    });

    it('should return true for valid email', () => {
      component.formData.email = 'valid@example.com';
      expect(component.validateEmail()).toBeTrue();
      expect(component.formErrors.email).toBe('');
    });
  });

  describe('submitForm - add', () => {
    it('should call addGlobalRecipient and show success', fakeAsync(() => {
      adminServiceSpy.addGlobalRecipient.and.returnValue(of(undefined));
      adminServiceSpy.getGlobalRecipients.and.returnValue(of(mockSettings));

      component.openAddModal();
      component.formData.email = 'new@example.com';
      component.submitForm();
      tick();

      expect(adminServiceSpy.addGlobalRecipient).toHaveBeenCalledWith('new@example.com');
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Recipient added successfully');
    }));

    it('should set duplicate email error when email already exists', () => {
      component.openAddModal();
      component.formData.email = 'a@example.com'; // already in list
      component.submitForm();
      expect(component.formErrors.email).toBe('This email already exists');
    });
  });

  describe('delete recipient', () => {
    it('openDeleteConfirm should set recipientToDelete and show confirm', () => {
      component.openDeleteConfirm({ email: 'a@example.com', enabled: true });
      expect(component.showDeleteConfirm).toBeTrue();
      expect(component.recipientToDelete?.email).toBe('a@example.com');
    });

    it('closeDeleteConfirm should reset state', () => {
      component.openDeleteConfirm({ email: 'a@example.com', enabled: true });
      component.closeDeleteConfirm();
      expect(component.showDeleteConfirm).toBeFalse();
      expect(component.recipientToDelete).toBeNull();
    });

    it('confirmDelete should call deleteGlobalRecipient and show success', fakeAsync(() => {
      adminServiceSpy.deleteGlobalRecipient.and.returnValue(of(undefined));
      adminServiceSpy.getGlobalRecipients.and.returnValue(of(mockSettings));

      component.openDeleteConfirm({ email: 'a@example.com', enabled: true });
      component.confirmDelete();
      tick();

      expect(adminServiceSpy.deleteGlobalRecipient).toHaveBeenCalledWith('a@example.com');
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Recipient deleted successfully');
    }));

    it('confirmDelete should do nothing when recipientToDelete is null', () => {
      component.recipientToDelete = null;
      component.confirmDelete();
      expect(adminServiceSpy.deleteGlobalRecipient).not.toHaveBeenCalled();
    });
  });
});
