import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { PositionService } from '../../app/services/position.service';
import { Position, PositionListItem, PublicPosition } from '../../app/models/position.model';
import { InviteCandidateRequest } from '../../app/models/recruiter.model';
import { ApiResponse, PagedData } from '../../app/models/responses/api-base-response.model';

const mockPosition: Position = {
  id: 'p-1',
  title: 'Frontend Dev',
  description: 'Build UIs',
  external: false,
  createdBy: 'admin-1',
  createdAt: new Date('2024-01-01'),
  isActive: true,
  location: 'New York, NY',
  workMode: 'Onsite',
};

const mockListItem: PositionListItem = {
  id: 'p-1',
  title: 'Frontend Dev',
  description: 'Build UIs',
  external: false,
  assessmentsCount: 2,
  isActive: true,
  createdAt: new Date('2024-01-01'),
};

describe('PositionService', () => {
  let service: PositionService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), PositionService],
    });
    service = TestBed.inject(PositionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getPositions', () => {
    it('should GET /positions', () => {
      const mockPagedData: PagedData<PositionListItem> = {
        items: [mockListItem],
        total: 1,
        page: 0,
        pageSize: 10,
        totalPages: 1,
      };
      const mockResponse: ApiResponse<PagedData<PositionListItem>> = {
        status: 'success',
        message: 'OK',
        data: mockPagedData,
      };

      service.getPositions().subscribe(res => expect(res).toEqual(mockPagedData));

      const req = httpMock.expectOne(r => r.url === '/positions');
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should pass activeOnly param when filter is provided', () => {
      service.getPositions({ activeOnly: true }).subscribe();

      const req = httpMock.expectOne(r => r.url === '/positions');
      expect(req.request.params.get('activeOnly')).toBe('true');
      req.flush({
        status: 'success',
        message: 'OK',
        data: { items: [], total: 0, page: 0, pageSize: 10, totalPages: 0 },
      });
    });
  });

  describe('getPositionById', () => {
    it('should GET /positions/:id', () => {
      const mockResponse: ApiResponse<Position> = { status: 'success', message: 'OK', data: mockPosition };

      service.getPositionById('p-1').subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/positions/p-1');
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('createPosition', () => {
    it('should POST to /admin/positions with FormData', () => {
      const mockResponse: ApiResponse<Position> = { status: 'success', message: 'Created', data: mockPosition };

      service
        .createPosition({ title: 'Dev', description: 'Desc', external: false, location: 'Remote', workMode: 'Remote' })
        .subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/admin/positions');
      expect(req.request.method).toBe('POST');
      expect(req.request.body instanceof FormData).toBeTrue();
      req.flush(mockResponse);
    });
  });

  describe('updatePosition', () => {
    it('should PUT to /admin/positions/:id with FormData', () => {
      const mockResponse: ApiResponse<Position> = { status: 'success', message: 'Updated', data: mockPosition };

      service.updatePosition('p-1', { title: 'Updated Dev' }).subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/admin/positions/p-1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body instanceof FormData).toBeTrue();
      req.flush(mockResponse);
    });
  });

  describe('updatePositionState', () => {
    it('should PATCH /admin/positions/:id/activate', () => {
      const mockResponse: ApiResponse<null> = { status: 'success', message: 'Updated' };

      service.updatePositionState('p-1', { isActive: false }).subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/admin/positions/p-1/activate');
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toEqual({ isActive: false });
      req.flush(mockResponse);
    });
  });

  describe('inviteCandidateToPosition', () => {
    it('should POST to /candidates/invite with correct payload', () => {
      const mockResponse: ApiResponse<InviteCandidateRequest> = { status: 'success', message: 'Invited' };

      service
        .inviteCandidateToPosition('candidate@example.com', 'Jane', 'p-1')
        .subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/candidates/invite');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ email: 'candidate@example.com', candidateName: 'Jane', positionId: 'p-1' });
      req.flush(mockResponse);
    });
  });

  describe('getPublicPositions', () => {
    it('should GET /applicants/positions', () => {
      const mockPublic: PublicPosition = {
        id: 'p-1',
        title: 'Dev',
        description: 'Desc',
        fileUrl: null,
        createdAt: new Date(),
      };
      const mockResponse: ApiResponse<PublicPosition[]> = { status: 'success', message: 'OK', data: [mockPublic] };

      service.getPublicPositions().subscribe(res => expect(res.data).toEqual([mockPublic]));

      const req = httpMock.expectOne('/applicants/positions');
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should return cached data on second call without HTTP request', () => {
      const mockPublic: PublicPosition = {
        id: 'p-1',
        title: 'Dev',
        description: 'Desc',
        fileUrl: null,
        createdAt: new Date(),
      };
      const mockResponse: ApiResponse<PublicPosition[]> = { status: 'success', message: 'OK', data: [mockPublic] };

      service.getPublicPositions().subscribe();
      httpMock.expectOne('/applicants/positions').flush(mockResponse);

      // Second call should use cache — no HTTP request
      service.getPublicPositions().subscribe(res => expect(res.data).toEqual([mockPublic]));
      httpMock.expectNone('/applicants/positions');
    });

    it('should bypass cache when forceRefresh is true', () => {
      const mockResponse: ApiResponse<PublicPosition[]> = { status: 'success', message: 'OK', data: [] };

      service.getPublicPositions().subscribe();
      httpMock.expectOne('/applicants/positions').flush(mockResponse);

      service.getPublicPositions(true).subscribe();
      httpMock.expectOne('/applicants/positions').flush(mockResponse);
    });
  });

  describe('getPublicPositionById', () => {
    it('should GET /applicants/positions/:id', () => {
      const mockPublic: PublicPosition = {
        id: 'p-1',
        title: 'Dev',
        description: 'Desc',
        fileUrl: null,
        createdAt: new Date(),
      };
      const mockResponse: ApiResponse<PublicPosition> = { status: 'success', message: 'OK', data: mockPublic };

      service.getPublicPositionById('p-1').subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/applicants/positions/p-1');
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('clearPublicPositionsCache', () => {
    it('should force a new HTTP request after cache is cleared', () => {
      const mockResponse: ApiResponse<PublicPosition[]> = { status: 'success', message: 'OK', data: [] };

      service.getPublicPositions().subscribe();
      httpMock.expectOne('/applicants/positions').flush(mockResponse);

      service.clearPublicPositionsCache();

      service.getPublicPositions().subscribe();
      httpMock.expectOne('/applicants/positions').flush(mockResponse);
    });
  });
});
