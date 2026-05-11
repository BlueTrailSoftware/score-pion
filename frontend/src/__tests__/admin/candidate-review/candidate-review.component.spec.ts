import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CandidateReviewComponent } from '../../../app/admin/candidate-review/candidate-review.component';
import { CandidatesService } from '../../../app/services/candidates.service';
import { PositionService } from '../../../app/services/position.service';
import { AdminService } from '../../../app/services/admin.service';
import { NotificationService } from '../../../app/services/notification.service';
import { AuthService } from '../../../app/services/auth.service';
import { Candidate } from '../../../app/models/candidate.model';
import { Position } from '../../../app/models/position.model';

const mockCandidate: Candidate = {
  id: 'c-1',
  name: 'Jane Doe',
  email: 'jane@example.com',
  phone: '555-0001',
  positionId: 'p-1',
  positionTitle: 'Frontend Dev',
  status: 'PENDING',
  source: 'web',
  createdAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-01T00:00:00Z',
};

const mockPosition: Position = {
  id: 'p-1',
  title: 'Frontend Dev',
  description: 'Build UIs',
  external: false,
  createdBy: 'admin-1',
  createdAt: new Date('2024-01-01'),
  isActive: true,
  assessments: [{ assessmentId: 'a-1', assessmentName: 'JS Test' }],
  location: 'New York, NY',
  workMode: 'Onsite',
};

const mockPagedCandidates = { items: [mockCandidate], total: 1, page: 0, pageSize: 10, totalPages: 1 };
const emptyPagedInvitations = { items: [], total: 0, page: 0, pageSize: 10, totalPages: 0 };

describe('CandidateReviewComponent', () => {
  let component: CandidateReviewComponent;
  let fixture: ComponentFixture<CandidateReviewComponent>;
  let candidatesServiceSpy: jasmine.SpyObj<CandidatesService>;
  let positionServiceSpy: jasmine.SpyObj<PositionService>;
  let adminServiceSpy: jasmine.SpyObj<AdminService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    candidatesServiceSpy = jasmine.createSpyObj('CandidatesService', [
      'getCandidates',
      'inviteCandidate',
      'rejectCandidate',
      'getInvitedCandidates',
    ]);
    positionServiceSpy = jasmine.createSpyObj('PositionService', ['getPositionById']);
    adminServiceSpy = jasmine.createSpyObj('AdminService', ['getAllRecruiters']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isAdmin', 'getCurrentUserId']);

    candidatesServiceSpy.getCandidates.and.returnValue(of(mockPagedCandidates));
    candidatesServiceSpy.getInvitedCandidates.and.returnValue(of(emptyPagedInvitations));
    authServiceSpy.isAdmin.and.returnValue(true);

    await TestBed.configureTestingModule({
      imports: [CandidateReviewComponent],
      providers: [
        provideRouter([]),
        { provide: CandidatesService, useValue: candidatesServiceSpy },
        { provide: PositionService, useValue: positionServiceSpy },
        { provide: AdminService, useValue: adminServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateReviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load candidates on init', () => {
    expect(component.candidates).toEqual([mockCandidate]);
    expect(component.totalCandidates).toBe(1);
  });

  describe('onFilterChange', () => {
    it('should reset to page 1 and reload candidates', () => {
      component.currentPage = 3;
      component.onFilterChange();
      expect(component.currentPage).toBe(1);
      expect(candidatesServiceSpy.getCandidates).toHaveBeenCalledTimes(2);
    });
  });

  describe('onSortChange', () => {
    it('should toggle direction when sorting by same field', () => {
      component.sortField = 'name';
      component.sortDirection = 'asc';
      component.onSortChange('name');
      expect(component.sortDirection).toBe('desc');
    });

    it('should set new field and reset direction to asc', () => {
      component.onSortChange('email');
      expect(component.sortField).toBe('email');
      expect(component.sortDirection).toBe('asc');
    });
  });

  describe('onPageChange', () => {
    it('should update currentPage and reload', () => {
      component.onPageChange(2);
      expect(component.currentPage).toBe(2);
      expect(candidatesServiceSpy.getCandidates).toHaveBeenCalledTimes(2);
    });
  });

  describe('invite modal', () => {
    beforeEach(() => {
      positionServiceSpy.getPositionById.and.returnValue(of({ status: 'success', message: 'OK', data: mockPosition }));
    });

    it('openInviteModal should set candidate and load position', fakeAsync(() => {
      component.openInviteModal(mockCandidate);
      tick();

      expect(component.showInviteModal).toBeTrue();
      expect(component.selectedCandidate).toEqual(mockCandidate);
      expect(component.selectedPosition).toEqual(mockPosition);
    }));

    it('closeInviteModal should reset modal state', () => {
      component.showInviteModal = true;
      component.selectedCandidate = mockCandidate;
      component.closeInviteModal();

      expect(component.showInviteModal).toBeFalse();
      expect(component.selectedCandidate).toBeNull();
    });

    it('hasAssessments should return true when position has assessments', fakeAsync(() => {
      component.openInviteModal(mockCandidate);
      tick();
      expect(component.hasAssessments).toBeTrue();
    }));

    it('confirmInvite should call inviteCandidate and show success', fakeAsync(() => {
      candidatesServiceSpy.inviteCandidate.and.returnValue(of(undefined));
      component.selectedCandidate = mockCandidate;

      component.confirmInvite();
      tick();

      expect(candidatesServiceSpy.inviteCandidate).toHaveBeenCalledWith('c-1');
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Candidate Jane Doe invited.');
    }));

    it('confirmInvite should show error on failure', fakeAsync(() => {
      candidatesServiceSpy.inviteCandidate.and.returnValue(throwError(() => new Error('fail')));
      component.selectedCandidate = mockCandidate;

      component.confirmInvite();
      tick();

      expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to invite candidate.');
    }));
  });

  describe('reject modal', () => {
    it('openRejectModal should set candidate and show modal', () => {
      component.openRejectModal(mockCandidate);
      expect(component.showRejectModal).toBeTrue();
      expect(component.candidateToReject).toEqual(mockCandidate);
    });

    it('closeRejectModal should reset modal state', () => {
      component.showRejectModal = true;
      component.candidateToReject = mockCandidate;
      component.closeRejectModal();

      expect(component.showRejectModal).toBeFalse();
      expect(component.candidateToReject).toBeNull();
    });

    it('confirmReject should call rejectCandidate and show success', fakeAsync(() => {
      candidatesServiceSpy.rejectCandidate.and.returnValue(of(undefined));
      component.candidateToReject = mockCandidate;
      component.rejectNote = 'Not a fit';

      component.confirmReject();
      tick();

      expect(candidatesServiceSpy.rejectCandidate).toHaveBeenCalledWith('c-1', 'Not a fit');
      expect(notificationServiceSpy.success).toHaveBeenCalledWith('Candidate Jane Doe rejected.');
    }));
  });

  describe('switchTab', () => {
    it('should switch to candidates tab and load recruiter candidates', fakeAsync(() => {
      adminServiceSpy.getAllRecruiters.and.returnValue(
        of({ items: [], total: 0, page: 0, pageSize: 50, totalPages: 0 }),
      );
      candidatesServiceSpy.getInvitedCandidates.and.returnValue(of(emptyPagedInvitations));

      component.switchTab('candidates');
      tick();

      expect(component.activeTab).toBe('candidates');
      expect(adminServiceSpy.getAllRecruiters).toHaveBeenCalled();
    }));
  });

  describe('getStatusBadgeClass', () => {
    it('should return correct class for each status', () => {
      expect(component.getStatusBadgeClass('INVITED')).toBe('badge-status-invited');
      expect(component.getStatusBadgeClass('REJECTED')).toBe('badge-status-rejected');
      expect(component.getStatusBadgeClass('PENDING')).toBe('badge-status-pending');
      expect(component.getStatusBadgeClass('UNKNOWN')).toBe('badge-status-unknown');
    });
  });

  describe('getFileIcon', () => {
    it('should return pdf icon for .pdf files', () => {
      expect(component.getFileIcon('http://example.com/file.pdf')).toBe('bi-file-earmark-pdf');
    });

    it('should return word icon for .docx files', () => {
      expect(component.getFileIcon('http://example.com/file.docx')).toBe('bi-file-earmark-word');
    });

    it('should return image icon for .jpg files', () => {
      expect(component.getFileIcon('http://example.com/file.jpg')).toBe('bi-file-earmark-image');
    });

    it('should return generic icon for unknown extension', () => {
      expect(component.getFileIcon('http://example.com/file.xyz')).toBe('bi-file-earmark');
    });

    it('should return generic icon when fileUrl is undefined', () => {
      expect(component.getFileIcon(undefined)).toBe('bi-file-earmark');
    });
  });
});
