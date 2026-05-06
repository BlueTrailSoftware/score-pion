import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of } from 'rxjs';
import { provideRouter } from '@angular/router';
import { ManageRecruitersComponent } from '../../../app/admin/manage-recruiters/manage-recruiters.component';
import { AdminService } from '../../../app/services/admin.service';
import { NotificationService } from '../../../app/services/notification.service';
import { AuthService } from '../../../app/services/auth.service';
import { UserProfile, RecruiterPosition } from '../../../app/models/user-profile.model';

const mockRecruiter: UserProfile = {
  id: 'r-1',
  name: 'Alice',
  email: 'alice@example.com',
  isActive: true,
  status: 'Active',
};

const mockAdmin: UserProfile = {
  id: 'a-1',
  name: 'Bob',
  email: 'bob@example.com',
  isActive: true,
  status: 'Active',
};

const mockPosition: RecruiterPosition = {
  id: 'p-1',
  title: 'Frontend Dev',
  description: 'Build UIs',
  external: false,
  assessmentsCount: 2,
  isActive: true,
  createdAt: '2024-01-01T00:00:00Z',
};

describe('ManageRecruitersComponent', () => {
  let component: ManageRecruitersComponent;
  let fixture: ComponentFixture<ManageRecruitersComponent>;
  let adminServiceSpy: jasmine.SpyObj<AdminService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    adminServiceSpy = jasmine.createSpyObj('AdminService', [
      'getAllRecruiters',
      'getAllAdmins',
      'getAllPositions',
      'updateActiveStatus',
      'updateAdminActiveStatus',
      'getRecruiterPositions',
      'assignPositions',
    ]);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentUserId']);

    adminServiceSpy.getAllRecruiters.and.returnValue(
      of({ items: [mockRecruiter], total: 1, page: 0, pageSize: 50, totalPages: 1 }),
    );
    adminServiceSpy.getAllAdmins.and.returnValue(
      of({ items: [mockAdmin], total: 1, page: 0, pageSize: 50, totalPages: 1 }),
    );
    adminServiceSpy.getAllPositions.and.returnValue(
      of({ items: [mockPosition], total: 1, page: 0, pageSize: 50, totalPages: 1 }),
    );
    authServiceSpy.getCurrentUserId.and.returnValue('current-user-id');

    await TestBed.configureTestingModule({
      imports: [ManageRecruitersComponent],
      providers: [
        provideRouter([]),
        { provide: AdminService, useValue: adminServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ManageRecruitersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load recruiters, admins and positions on init', () => {
    expect(component.recruiters).toEqual([mockRecruiter]);
    expect(component.admins).toEqual([mockAdmin]);
    expect(component.positions).toEqual([mockPosition]);
  });

  describe('setTab', () => {
    it('should switch active tab', () => {
      component.setTab('admins');
      expect(component.activeTab).toBe('admins');
    });
  });

  describe('toggleActiveStatus', () => {
    it('should call updateActiveStatus for recruiter tab and show success', fakeAsync(() => {
      adminServiceSpy.updateActiveStatus.and.returnValue(of({ status: 'success', message: 'OK' }));
      adminServiceSpy.getAllRecruiters.and.returnValue(
        of({ items: [mockRecruiter], total: 1, page: 0, pageSize: 50, totalPages: 1 }),
      );

      component.activeTab = 'recruiters';
      component.toggleActiveStatus(mockRecruiter);
      tick();

      expect(adminServiceSpy.updateActiveStatus).toHaveBeenCalledWith('r-1', false);
      expect(notificationServiceSpy.success).toHaveBeenCalled();
    }));

    it('should call updateAdminActiveStatus for admins tab', fakeAsync(() => {
      adminServiceSpy.updateAdminActiveStatus.and.returnValue(of({ status: 'success', message: 'OK' }));

      component.activeTab = 'admins';
      component.toggleActiveStatus(mockAdmin);
      tick();

      expect(adminServiceSpy.updateAdminActiveStatus).toHaveBeenCalledWith('a-1', false);
    }));

    it('should do nothing when user has no id', () => {
      component.toggleActiveStatus({ email: 'no-id@example.com' });
      expect(adminServiceSpy.updateActiveStatus).not.toHaveBeenCalled();
    });
  });

  describe('isCurrentUser', () => {
    it('should return true when user id matches current user', () => {
      authServiceSpy.getCurrentUserId.and.returnValue('r-1');
      expect(component.isCurrentUser(mockRecruiter)).toBeTrue();
    });

    it('should return false when user id does not match', () => {
      expect(component.isCurrentUser(mockRecruiter)).toBeFalse();
    });
  });

  describe('openAssignModal', () => {
    it('should load recruiter positions and open modal', fakeAsync(() => {
      adminServiceSpy.getRecruiterPositions.and.returnValue(
        of({ items: [mockPosition], total: 1, page: 0, pageSize: 50, totalPages: 1 }),
      );

      component.openAssignModal(mockRecruiter);
      tick();

      expect(component.showAssignModal).toBeTrue();
      expect(component.selectedRecruiter).toEqual(mockRecruiter);
      expect(component.selectedPositions.has('p-1')).toBeTrue();
    }));

    it('should do nothing when recruiter has no id', () => {
      component.openAssignModal({ email: 'no-id@example.com' });
      expect(adminServiceSpy.getRecruiterPositions).not.toHaveBeenCalled();
    });
  });

  describe('closeAssignModal', () => {
    it('should close modal and clear selection', () => {
      component.showAssignModal = true;
      component.selectedRecruiter = mockRecruiter;
      component.selectedPositions.add('p-1');

      component.closeAssignModal();

      expect(component.showAssignModal).toBeFalse();
      expect(component.selectedRecruiter).toBeNull();
      expect(component.selectedPositions.size).toBe(0);
    });
  });

  describe('togglePositionSelection', () => {
    it('should add position when not selected', () => {
      component.togglePositionSelection('p-1');
      expect(component.isPositionSelected('p-1')).toBeTrue();
    });

    it('should remove position when already selected', () => {
      component.selectedPositions.add('p-1');
      component.togglePositionSelection('p-1');
      expect(component.isPositionSelected('p-1')).toBeFalse();
    });
  });

  describe('saveAssignments', () => {
    it('should call assignPositions and show success', fakeAsync(() => {
      adminServiceSpy.assignPositions.and.returnValue(of({ status: 'success', message: 'OK' }));
      adminServiceSpy.getAllRecruiters.and.returnValue(
        of({ items: [mockRecruiter], total: 1, page: 0, pageSize: 50, totalPages: 1 }),
      );

      component.selectedRecruiter = mockRecruiter;
      component.selectedPositions.add('p-1');
      component.saveAssignments();
      tick();

      expect(adminServiceSpy.assignPositions).toHaveBeenCalledWith('r-1', ['p-1']);
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Positions assigned successfully');
    }));

    it('should do nothing when no recruiter is selected', () => {
      component.selectedRecruiter = null;
      component.saveAssignments();
      expect(adminServiceSpy.assignPositions).not.toHaveBeenCalled();
    });
  });
});
