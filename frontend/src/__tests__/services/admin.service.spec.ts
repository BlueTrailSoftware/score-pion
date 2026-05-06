import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AdminService } from '../../app/services/admin.service';
import { UserProfile, AssessmentAccess, RecruiterPosition } from '../../app/models/user-profile.model';
import { InviteRecruiterRequest } from '../../app/models/recruiter.model';
import { RecruiterInvitation, AssignAssessmentsResponse } from '../../app/models/responses/admin-response.model';
import { ApiResponse, PagedData } from '../../app/models/responses/api-base-response.model';
import { CreateExamTicketRequest, CreateExamTicketResponse } from '../../app/models/assessment.model';
import { GlobalRecipientsSettings } from '../../app/models/global-recipient.model';

describe('AdminService', () => {
  let service: AdminService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), AdminService],
    });
    service = TestBed.inject(AdminService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('inviteRecruiter', () => {
    it('should POST to /admin/recruiters/invite', () => {
      const request: InviteRecruiterRequest = { email: 'recruiter@example.com', positionIds: ['pos-1'] };
      const mockResponse: ApiResponse<RecruiterInvitation> = {
        status: 'success',
        message: 'Invited',
        data: {
          id: 'inv-1',
          email: 'recruiter@example.com',
          invitedBy: 'admin@example.com',
          assignedAssessments: [],
          status: 'pending',
          createdAt: '2024-01-01T00:00:00Z',
          expiresAt: '2024-01-08T00:00:00Z',
        },
      };

      service.inviteRecruiter(request).subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/admin/recruiters/invite');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockResponse);
    });
  });

  describe('inviteAdmin', () => {
    it('should POST to /admin/invite', () => {
      const request = { email: 'admin@example.com' };
      const mockResponse: ApiResponse<RecruiterInvitation> = { status: 'success', message: 'Invited' };

      service.inviteAdmin(request).subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/admin/invite');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockResponse);
    });
  });

  describe('getAllRecruiters', () => {
    it('should GET /admin/recruiters and return user array', () => {
      const mockUsers: UserProfile[] = [{ id: 'u-1', email: 'r@example.com', name: 'Recruiter' }];
      const mockPagedData: PagedData<UserProfile> = {
        items: mockUsers,
        total: 1,
        page: 0,
        pageSize: 50,
        totalPages: 1,
      };
      const mockResponse: ApiResponse<PagedData<UserProfile>> = {
        status: 'success',
        message: 'OK',
        data: mockPagedData,
      };

      service.getAllRecruiters().subscribe(res => expect(res).toEqual(mockPagedData));

      const req = httpMock.expectOne(r => r.url === '/admin/recruiters');
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should return empty paged data when data is undefined', () => {
      const emptyPaged: PagedData<UserProfile> = { items: [], total: 0, page: 0, pageSize: 50, totalPages: 0 };
      service.getAllRecruiters().subscribe(res => expect(res).toEqual(emptyPaged));
      httpMock.expectOne(r => r.url === '/admin/recruiters').flush({ status: 'success', message: 'OK' });
    });
  });

  describe('getAllAdmins', () => {
    it('should GET /admin/admins and return data array', () => {
      const mockUsers: UserProfile[] = [{ id: 'a-1', email: 'admin@example.com', name: 'Admin' }];
      const mockPagedData: PagedData<UserProfile> = {
        items: mockUsers,
        total: 1,
        page: 0,
        pageSize: 50,
        totalPages: 1,
      };
      const mockResponse: ApiResponse<PagedData<UserProfile>> = {
        status: 'success',
        message: 'OK',
        data: mockPagedData,
      };

      service.getAllAdmins().subscribe(res => expect(res).toEqual(mockPagedData));

      const req = httpMock.expectOne(r => r.url === '/admin/admins');
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should return empty paged data when data is undefined', () => {
      const emptyPaged: PagedData<UserProfile> = { items: [], total: 0, page: 0, pageSize: 50, totalPages: 0 };
      service.getAllAdmins().subscribe(res => expect(res).toEqual(emptyPaged));
      httpMock.expectOne(r => r.url === '/admin/admins').flush({ status: 'success', message: 'OK' });
    });
  });

  describe('getRecruiterById', () => {
    it('should GET /admin/recruiters/:id', () => {
      const mockResponse: ApiResponse<UserProfile> = {
        status: 'success',
        message: 'OK',
        data: { id: 'u-1', email: 'r@example.com' },
      };

      service.getRecruiterById('u-1').subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/admin/recruiters/u-1');
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('updateActiveStatus', () => {
    it('should PUT to /admin/recruiters/:id/activate', () => {
      const mockResponse: ApiResponse<UserProfile> = { status: 'success', message: 'Updated' };

      service.updateActiveStatus('u-1', false).subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/admin/recruiters/u-1/activate');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({ isActive: false });
      req.flush(mockResponse);
    });
  });

  describe('updateAdminActiveStatus', () => {
    it('should PUT to /admin/admins/:id/activate', () => {
      const mockResponse: ApiResponse<UserProfile> = { status: 'success', message: 'Updated' };

      service.updateAdminActiveStatus('a-1', true).subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/admin/admins/a-1/activate');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({ isActive: true });
      req.flush(mockResponse);
    });
  });

  describe('assignAssessments', () => {
    it('should POST to /admin/recruiters/:id/assessments', () => {
      const assessmentIds = ['a-1', 'a-2'];
      const mockResponse: ApiResponse<AssignAssessmentsResponse> = {
        status: 'success',
        message: 'Assigned',
        data: { assigned: assessmentIds, alreadyAssigned: [], notFound: [] },
      };

      service.assignAssessments('u-1', assessmentIds).subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/admin/recruiters/u-1/assessments');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ assessmentIds });
      req.flush(mockResponse);
    });
  });

  describe('getRecruiterAssessments', () => {
    it('should GET /admin/recruiters/:id/assessments', () => {
      const mockResponse: ApiResponse<AssessmentAccess[]> = { status: 'success', message: 'OK', data: [] };

      service.getRecruiterAssessments('u-1').subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/admin/recruiters/u-1/assessments');
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('getPendingInvitations', () => {
    it('should GET /admin/invitations', () => {
      const mockResponse: ApiResponse<RecruiterInvitation[]> = { status: 'success', message: 'OK', data: [] };

      service.getPendingInvitations().subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/admin/invitations');
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('createExamTicket', () => {
    it('should POST to /admin/ticket', () => {
      const request: CreateExamTicketRequest = { readyDate: '2024-06-01', description: 'Test ticket' };
      const mockResponse: ApiResponse<CreateExamTicketResponse> = {
        status: 'success',
        message: 'Created',
        data: {
          ticketId: 't-1',
          email: 'candidate@example.com',
          readyDate: '2024-06-01',
          description: 'Test ticket',
          createdAt: '2024-01-01T00:00:00Z',
        },
      };

      service.createExamTicket(request).subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/admin/ticket');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockResponse);
    });
  });

  describe('getRecruiterPositions', () => {
    it('should GET /admin/recruiters/:id/positions and return data array', () => {
      const mockPositions: RecruiterPosition[] = [
        {
          id: 'p-1',
          title: 'Dev',
          description: 'Developer',
          external: false,
          assessmentsCount: 2,
          isActive: true,
          createdAt: '2024-01-01T00:00:00Z',
        },
      ];
      const mockPagedData: PagedData<RecruiterPosition> = {
        items: mockPositions,
        total: 1,
        page: 0,
        pageSize: 50,
        totalPages: 1,
      };
      const mockResponse: ApiResponse<PagedData<RecruiterPosition>> = {
        status: 'success',
        message: 'OK',
        data: mockPagedData,
      };

      service.getRecruiterPositions('u-1').subscribe(res => expect(res).toEqual(mockPagedData));

      const req = httpMock.expectOne(r => r.url === '/admin/recruiters/u-1/positions');
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should return empty paged data when data is undefined', () => {
      const emptyPaged: PagedData<RecruiterPosition> = { items: [], total: 0, page: 0, pageSize: 50, totalPages: 0 };
      service.getRecruiterPositions('u-1').subscribe(res => expect(res).toEqual(emptyPaged));
      httpMock.expectOne(r => r.url === '/admin/recruiters/u-1/positions').flush({ status: 'success', message: 'OK' });
    });
  });

  describe('assignPositions', () => {
    it('should PUT to /admin/recruiters/:id/positions', () => {
      const positionIds = ['p-1', 'p-2'];
      const mockResponse: ApiResponse<void> = { status: 'success', message: 'Assigned' };

      service.assignPositions('u-1', positionIds).subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/admin/recruiters/u-1/positions');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({ positionIds });
      req.flush(mockResponse);
    });
  });

  describe('getAllPositions', () => {
    it('should GET /admin/positions with activeOnly=true by default', () => {
      const mockPositions: RecruiterPosition[] = [];
      const mockPagedData: PagedData<RecruiterPosition> = {
        items: mockPositions,
        total: 0,
        page: 0,
        pageSize: 50,
        totalPages: 0,
      };
      const mockResponse: ApiResponse<PagedData<RecruiterPosition>> = {
        status: 'success',
        message: 'OK',
        data: mockPagedData,
      };

      service.getAllPositions().subscribe(res => expect(res).toEqual(mockPagedData));

      const req = httpMock.expectOne(r => r.url === '/admin/positions' && r.params.get('activeOnly') === 'true');
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should GET /admin/positions with activeOnly=false when specified', () => {
      service.getAllPositions(false).subscribe();
      httpMock
        .expectOne(r => r.url === '/admin/positions' && r.params.get('activeOnly') === 'false')
        .flush({
          status: 'success',
          message: 'OK',
          data: { items: [], total: 0, page: 0, pageSize: 50, totalPages: 0 },
        });
    });

    it('should return empty paged data when data is undefined', () => {
      const emptyPaged: PagedData<RecruiterPosition> = { items: [], total: 0, page: 0, pageSize: 50, totalPages: 0 };
      service.getAllPositions().subscribe(res => expect(res).toEqual(emptyPaged));
      httpMock
        .expectOne(r => r.url === '/admin/positions' && r.params.get('activeOnly') === 'true')
        .flush({ status: 'success', message: 'OK' });
    });
  });

  describe('getGlobalRecipients', () => {
    it('should GET /settings/global-recipients and return data', () => {
      const mockData: GlobalRecipientsSettings = {
        emails: ['notify@example.com'],
        description: 'Global',
        updatedAt: '2024-01-01',
        updatedBy: 'admin',
      };
      const mockResponse: ApiResponse<GlobalRecipientsSettings> = { status: 'success', message: 'OK', data: mockData };

      service.getGlobalRecipients().subscribe(res => expect(res).toEqual(mockData));

      const req = httpMock.expectOne('/settings/global-recipients');
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('addGlobalRecipient', () => {
    it('should POST to /settings/global-recipients/emails and return data', () => {
      const email = 'new@example.com';
      const mockData: GlobalRecipientsSettings = {
        emails: [email],
        description: 'Global',
        updatedAt: '2024-01-01',
        updatedBy: 'admin',
      };
      const mockResponse: ApiResponse<GlobalRecipientsSettings> = {
        status: 'success',
        message: 'Email added successfully',
        data: mockData,
      };

      service.addGlobalRecipient(email).subscribe(res => expect(res).toEqual(mockData));

      const req = httpMock.expectOne('/settings/global-recipients/emails');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ email });
      req.flush(mockResponse);
    });
  });

  describe('deleteGlobalRecipient', () => {
    it('should DELETE /settings/global-recipients/emails/:email (URL encoded) and return data', () => {
      const email = 'old@example.com';
      const mockData: GlobalRecipientsSettings = {
        emails: [],
        description: 'Global',
        updatedAt: '2024-01-01',
        updatedBy: 'admin',
      };
      const mockResponse: ApiResponse<GlobalRecipientsSettings> = {
        status: 'success',
        message: 'Email removed successfully',
        data: mockData,
      };

      service.deleteGlobalRecipient(email).subscribe(res => expect(res).toEqual(mockData));

      const req = httpMock.expectOne(`/settings/global-recipients/emails/${encodeURIComponent(email)}`);
      expect(req.request.method).toBe('DELETE');
      req.flush(mockResponse);
    });
  });
});
