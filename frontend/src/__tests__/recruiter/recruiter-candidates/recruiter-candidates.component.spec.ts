import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { RecruiterCandidatesComponent } from '../../../app/recruiter/recruiter-candidates/recruiter-candidates.component';
import { CandidatesService } from '../../../app/services/candidates.service';
import { NotificationService } from '../../../app/services/notification.service';
import { CandidateInvitation } from '../../../app/models/candidate.model';
import { PagedData } from '../../../app/models/responses/api-base-response.model';

const mockCandidates: CandidateInvitation[] = [
  {
    candidateEmail: 'alice@example.com',
    candidateName: 'Alice',
    positionId: 'p-1',
    positionTitle: 'Frontend Dev',
    recruiterId: 'r-1',
    invitedAt: '2024-01-01T00:00:00Z',
    assessments: [],
  },
  {
    candidateEmail: 'bob@example.com',
    candidateName: 'Bob',
    positionId: 'p-2',
    positionTitle: 'Backend Dev',
    recruiterId: 'r-1',
    invitedAt: '2024-02-01T00:00:00Z',
    assessments: [],
  },
];

const mockPagedCandidates: PagedData<CandidateInvitation> = {
  items: mockCandidates,
  total: 2,
  page: 0,
  pageSize: 10,
  totalPages: 1,
};

const emptyPaged: PagedData<CandidateInvitation> = { items: [], total: 0, page: 0, pageSize: 10, totalPages: 0 };

describe('RecruiterCandidatesComponent', () => {
  let component: RecruiterCandidatesComponent;
  let fixture: ComponentFixture<RecruiterCandidatesComponent>;
  let candidatesServiceSpy: jasmine.SpyObj<CandidatesService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  beforeEach(async () => {
    candidatesServiceSpy = jasmine.createSpyObj('CandidatesService', ['getInvitedCandidates']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    candidatesServiceSpy.getInvitedCandidates.and.returnValue(of(mockPagedCandidates));

    await TestBed.configureTestingModule({
      imports: [RecruiterCandidatesComponent],
      providers: [
        { provide: CandidatesService, useValue: candidatesServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RecruiterCandidatesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load candidates on init', () => {
    expect(candidatesServiceSpy.getInvitedCandidates).toHaveBeenCalled();
    expect(component.candidates).toEqual(mockCandidates);
    expect(component.total).toBe(2);
    expect(component.loading).toBeFalse();
  });

  it('should show error on load failure', () => {
    spyOn(console, 'error');
    candidatesServiceSpy.getInvitedCandidates.and.returnValue(throwError(() => new Error('fail')));

    fixture = TestBed.createComponent(RecruiterCandidatesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(notificationServiceSpy.error).toHaveBeenCalledWith('Failed to load candidates');
    expect(component.loading).toBeFalse();
  });

  describe('filtering', () => {
    it('should call loadCandidates and reset to page 1 on filter change', () => {
      component.currentPage = 3;
      candidatesServiceSpy.getInvitedCandidates.and.returnValue(of(emptyPaged));

      component.filterSearch = 'alice';
      component.onFilterChange();

      expect(component.currentPage).toBe(1);
      expect(candidatesServiceSpy.getInvitedCandidates).toHaveBeenCalledTimes(2);
    });

    it('should pass search term to service', () => {
      candidatesServiceSpy.getInvitedCandidates.and.returnValue(of(emptyPaged));
      component.filterSearch = 'alice';
      component.onFilterChange();

      const [, search] = candidatesServiceSpy.getInvitedCandidates.calls.mostRecent().args;
      expect(search).toBe('alice');
    });

    it('should pass undefined search when filter is empty', () => {
      candidatesServiceSpy.getInvitedCandidates.and.returnValue(of(emptyPaged));
      component.filterSearch = '';
      component.onFilterChange();

      const [, search] = candidatesServiceSpy.getInvitedCandidates.calls.mostRecent().args;
      expect(search).toBeUndefined();
    });
  });
});
