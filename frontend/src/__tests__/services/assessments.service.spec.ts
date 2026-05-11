import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AssessmentsService } from '../../app/services/assessments.service';
import { Assessment } from '../../app/models/assessment.model';
import { ApiResponse } from '../../app/models/responses/api-base-response.model';

describe('AssessmentsService', () => {
  let service: AssessmentsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), AssessmentsService],
    });
    service = TestBed.inject(AssessmentsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAssessments', () => {
    it('should GET /admin/assessments and return the full response', () => {
      const mockAssessments: Assessment[] = [
        { displayName: 'JavaScript Test', testID: 'js-001' },
        { displayName: 'TypeScript Test', testID: 'ts-002' },
      ];
      const mockResponse: ApiResponse<Assessment[]> = {
        status: 'success',
        message: 'OK',
        data: mockAssessments,
      };

      service.getAssessments().subscribe(res => expect(res).toEqual(mockResponse));

      const req = httpMock.expectOne('/admin/assessments');
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should return response with undefined data when no assessments exist', () => {
      const mockResponse: ApiResponse<Assessment[]> = { status: 'success', message: 'OK' };

      service.getAssessments().subscribe(res => expect(res).toEqual(mockResponse));

      httpMock.expectOne('/admin/assessments').flush(mockResponse);
    });
  });

  describe('inviteCandidate', () => {
    it('should POST to /assessments/invite with email and assessments', () => {
      const email = 'candidate@example.com';
      const assessments = ['js-001', 'ts-002'];

      service.inviteCandidate(email, assessments).subscribe();

      const req = httpMock.expectOne('/assessments/invite');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ email, assessments });
      req.flush(null);
    });

    it('should POST with an empty assessments array', () => {
      service.inviteCandidate('candidate@example.com', []).subscribe();

      const req = httpMock.expectOne('/assessments/invite');
      expect(req.request.body).toEqual({ email: 'candidate@example.com', assessments: [] });
      req.flush(null);
    });
  });
});
