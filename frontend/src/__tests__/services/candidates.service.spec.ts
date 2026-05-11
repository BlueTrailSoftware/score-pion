import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { CandidatesService } from '../../app/services/candidates.service';
import { Candidate, CandidateInvitation, DeleteDataResponse } from '../../app/models/candidate.model';
import { ApiResponse, PagedData } from '../../app/models/responses/api-base-response.model';

const mockCandidate: Candidate = {
  id: 'c-1',
  name: 'John Doe',
  email: 'john@example.com',
  phone: '555-0000',
  positionId: 'p-1',
  status: 'PENDING',
  source: 'web',
  createdAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-01T00:00:00Z',
};

const emptyPagedData = <T>(): PagedData<T> => ({ items: [], total: 0, page: 0, pageSize: 10, totalPages: 0 });

describe('CandidatesService', () => {
  let service: CandidatesService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), CandidatesService],
    });
    service = TestBed.inject(CandidatesService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getCandidates', () => {
    it('should GET /applicants and return PagedData', () => {
      const mockData: PagedData<Candidate> = { items: [mockCandidate], total: 1, page: 0, pageSize: 10, totalPages: 1 };
      const mockResponse: ApiResponse<PagedData<Candidate>> = { status: 'success', message: 'OK', data: mockData };

      service.getCandidates().subscribe(result => {
        expect(result.items.length).toBe(1);
        expect(result.total).toBe(1);
        expect(result.totalPages).toBe(1);
      });

      httpMock.expectOne(r => r.url === '/applicants').flush(mockResponse);
    });

    it('should send page and pageSize as query params (1-indexed page converted to 0-indexed)', () => {
      service.getCandidates({}, { page: 2, pageSize: 20 }).subscribe();
      const req = httpMock.expectOne(r => r.url === '/applicants');
      expect(req.request.params.get('page')).toBe('1');
      expect(req.request.params.get('pageSize')).toBe('20');
      req.flush({ status: 'success', message: 'OK', data: emptyPagedData() });
    });

    it('should apply status filter as query param', () => {
      service.getCandidates({ status: 'PENDING' }).subscribe();
      const req = httpMock.expectOne(r => r.url === '/applicants');
      expect(req.request.params.get('status')).toBe('pending');
      req.flush({ status: 'success', message: 'OK', data: emptyPagedData() });
    });

    it('should apply position filter as query param', () => {
      service.getCandidates({ position: 'p-1' }).subscribe();
      const req = httpMock.expectOne(r => r.url === '/applicants');
      expect(req.request.params.get('positionId')).toBe('p-1');
      req.flush({ status: 'success', message: 'OK', data: emptyPagedData() });
    });

    it('should apply search filter as query param', () => {
      service.getCandidates({ search: 'john' }).subscribe();
      const req = httpMock.expectOne(r => r.url === '/applicants');
      expect(req.request.params.get('search')).toBe('john');
      req.flush({ status: 'success', message: 'OK', data: emptyPagedData() });
    });

    it('should return empty PagedData when response data is undefined', () => {
      service.getCandidates().subscribe(result => {
        expect(result.items).toEqual([]);
        expect(result.total).toBe(0);
      });
      httpMock.expectOne(r => r.url === '/applicants').flush({ status: 'success', message: 'OK' });
    });
  });

  describe('getCandidateById', () => {
    it('should GET /applicants/:id', () => {
      const mockResponse: ApiResponse<Candidate> = { status: 'success', message: 'OK', data: mockCandidate };

      service.getCandidateById('c-1').subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/applicants/c-1');
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('updateStatus', () => {
    it('should PATCH /applicants/:id/status with status body', () => {
      const mockResponse: ApiResponse<Candidate> = { status: 'success', message: 'Updated', data: mockCandidate };

      service.updateStatus('c-1', 'invited').subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/applicants/c-1/status');
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toEqual({ status: 'invited' });
      req.flush(mockResponse);
    });

    it('should include statusNote in body when provided', () => {
      service.updateStatus('c-1', 'rejected', 'Not a fit').subscribe();

      const req = httpMock.expectOne('/applicants/c-1/status');
      expect(req.request.body).toEqual({ status: 'rejected', statusNote: 'Not a fit' });
      req.flush({ status: 'success', message: 'OK', data: mockCandidate });
    });
  });

  describe('inviteCandidate', () => {
    it('should call updateStatus with invited and map to void', () => {
      service.inviteCandidate('c-1').subscribe(res => expect(res).toBeUndefined());

      const req = httpMock.expectOne('/applicants/c-1/status');
      expect(req.request.body).toEqual({ status: 'invited' });
      req.flush({ status: 'success', message: 'OK', data: mockCandidate });
    });
  });

  describe('rejectCandidate', () => {
    it('should call updateStatus with rejected and map to void', () => {
      service.rejectCandidate('c-1', 'No match').subscribe(res => expect(res).toBeUndefined());

      const req = httpMock.expectOne('/applicants/c-1/status');
      expect(req.request.body).toEqual({ status: 'rejected', statusNote: 'No match' });
      req.flush({ status: 'success', message: 'OK', data: mockCandidate });
    });
  });

  describe('getInvitedCandidates', () => {
    const mockInvitation: CandidateInvitation = {
      candidateEmail: 'john@example.com',
      candidateName: 'John',
      positionId: 'p-1',
      positionTitle: 'Dev',
      recruiterId: 'r-1',
      invitedAt: '2024-01-01T00:00:00Z',
      assessments: [],
    };

    it('should GET /candidates and return PagedData', () => {
      const mockData: PagedData<CandidateInvitation> = {
        items: [mockInvitation],
        total: 1,
        page: 0,
        pageSize: 10,
        totalPages: 1,
      };

      service.getInvitedCandidates().subscribe(res => {
        expect(res.items).toEqual([mockInvitation]);
        expect(res.total).toBe(1);
      });

      const req = httpMock.expectOne(r => r.url === '/candidates');
      expect(req.request.method).toBe('GET');
      req.flush({ status: 'success', message: 'OK', data: mockData });
    });

    it('should pass recruiterId as query param when provided', () => {
      service.getInvitedCandidates('r-1').subscribe();
      const req = httpMock.expectOne(r => r.url === '/candidates');
      expect(req.request.params.get('recruiterId')).toBe('r-1');
      req.flush({ status: 'success', message: 'OK', data: emptyPagedData() });
    });

    it('should pass search as query param when provided', () => {
      service.getInvitedCandidates(undefined, 'john').subscribe();
      const req = httpMock.expectOne(r => r.url === '/candidates');
      expect(req.request.params.get('search')).toBe('john');
      req.flush({ status: 'success', message: 'OK', data: emptyPagedData() });
    });

    it('should send page and pageSize as query params (1-indexed page converted to 0-indexed)', () => {
      service.getInvitedCandidates(undefined, undefined, { page: 3, pageSize: 5 }).subscribe();
      const req = httpMock.expectOne(r => r.url === '/candidates');
      expect(req.request.params.get('page')).toBe('2');
      expect(req.request.params.get('pageSize')).toBe('5');
      req.flush({ status: 'success', message: 'OK', data: emptyPagedData() });
    });

    it('should return empty PagedData when response data is undefined', () => {
      service.getInvitedCandidates().subscribe(res => {
        expect(res.items).toEqual([]);
        expect(res.total).toBe(0);
      });
      httpMock.expectOne(r => r.url === '/candidates').flush({ status: 'success', message: 'OK' });
    });
  });

  describe('GDPR methods', () => {
    it('createErasureRequest should POST to /applicants/privacy/erasures', () => {
      const mockRes: DeleteDataResponse = { message: 'Erasure requested' };
      service
        .createErasureRequest({ email: 'john@example.com', captchaToken: 'tok' })
        .subscribe(res => expect(res).toEqual(mockRes));

      const req = httpMock.expectOne('/applicants/privacy/erasures');
      expect(req.request.method).toBe('POST');
      req.flush(mockRes);
    });

    it('confirmErasure should PUT to /applicants/privacy/erasures/:token', () => {
      const mockRes: DeleteDataResponse = { message: 'Confirmed' };
      service.confirmErasure('abc123').subscribe(res => expect(res).toEqual(mockRes));

      const req = httpMock.expectOne('/applicants/privacy/erasures/abc123');
      expect(req.request.method).toBe('PUT');
      req.flush(mockRes);
    });

    it('createExportRequest should POST to /applicants/privacy/exports', () => {
      const mockRes: DeleteDataResponse = { message: 'Export requested' };
      service
        .createExportRequest({ email: 'john@example.com', captchaToken: 'tok' })
        .subscribe(res => expect(res).toEqual(mockRes));

      const req = httpMock.expectOne('/applicants/privacy/exports');
      expect(req.request.method).toBe('POST');
      req.flush(mockRes);
    });

    it('downloadExport should GET /applicants/privacy/exports/:token as blob', () => {
      const mockBlob = new Blob(['data'], { type: 'application/zip' });
      service.downloadExport('tok123').subscribe(res => expect(res).toEqual(mockBlob));

      const req = httpMock.expectOne('/applicants/privacy/exports/tok123');
      expect(req.request.method).toBe('GET');
      expect(req.request.responseType).toBe('blob');
      req.flush(mockBlob);
    });
  });
});
