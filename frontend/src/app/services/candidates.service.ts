import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import {
  Candidate,
  CandidateInvitation,
  CandidateFilter,
  PageOptions,
  UpdateCandidateRequest,
  DeleteMyDataRequest,
  DeleteDataResponse,
  DownloadMyDataRequest,
  ApplyToPositionRequest,
} from '../models/candidate.model';
import { ApiResponse, PagedData, emptyPagedData } from '../models/responses/api-base-response.model';
import { FileHelper } from '../helpers/fileHelper';

@Injectable({
  providedIn: 'root',
})
export class CandidatesService {
  private httpClient = inject(HttpClient);
  private readonly applicantsUrl = '/applicants';
  private readonly candidatesUrl = '/candidates';

  getCandidates(
    filter: CandidateFilter = {},
    pageOptions: PageOptions = { page: 1, pageSize: 10 },
  ): Observable<PagedData<Candidate>> {
    let params = new HttpParams();

    if (filter.status) {
      params = params.set('status', filter.status.toLowerCase());
    }
    if (filter.position) {
      params = params.set('positionId', filter.position);
    }
    if (filter.search) {
      params = params.set('search', filter.search);
    }
    if (filter.sortField) {
      params = params.set('sortField', filter.sortField);
    }
    if (filter.sortDirection) {
      params = params.set('sortDirection', filter.sortDirection);
    }
    params = params.set('page', String(pageOptions.page - 1));
    params = params.set('pageSize', String(pageOptions.pageSize));

    return this.httpClient
      .get<ApiResponse<PagedData<Candidate>>>(this.applicantsUrl, { params })
      .pipe(map(r => r.data ?? emptyPagedData(pageOptions.pageSize)));
  }

  getCandidateById(id: string): Observable<ApiResponse<Candidate>> {
    return this.httpClient.get<ApiResponse<Candidate>>(`${this.applicantsUrl}/${id}`);
  }

  apply(request: Partial<Candidate> | ApplyToPositionRequest, file?: File): Observable<ApiResponse<Candidate>> {
    const formData = FileHelper.createFormData(request, file);
    return this.httpClient.post<ApiResponse<Candidate>>(`${this.applicantsUrl}/apply`, formData);
  }

  updateStatus(id: string, status: string, statusNote?: string): Observable<ApiResponse<Candidate>> {
    const body: Record<string, string> = { status };
    if (statusNote !== undefined && statusNote !== '') {
      body['statusNote'] = statusNote;
    }
    return this.httpClient.patch<ApiResponse<Candidate>>(`${this.applicantsUrl}/${id}/status`, body);
  }

  inviteCandidate(id: string): Observable<void> {
    return this.updateStatus(id, 'invited').pipe(map(() => void 0));
  }

  rejectCandidate(id: string, statusNote?: string): Observable<void> {
    return this.updateStatus(id, 'rejected', statusNote).pipe(map(() => void 0));
  }

  updateCandidate(id: string, request: UpdateCandidateRequest, file?: File): Observable<ApiResponse<Candidate>> {
    const formData = FileHelper.createFormData(request, file);
    return this.httpClient.put<ApiResponse<Candidate>>(`${this.applicantsUrl}/${id}`, formData);
  }

  getInvitedCandidates(
    recruiterId?: string,
    search?: string,
    pageOptions: PageOptions = { page: 1, pageSize: 10 },
  ): Observable<PagedData<CandidateInvitation>> {
    let params = new HttpParams();
    if (recruiterId) {
      params = params.set('recruiterId', recruiterId);
    }
    if (search) {
      params = params.set('search', search);
    }
    params = params.set('page', String(pageOptions.page - 1));
    params = params.set('pageSize', String(pageOptions.pageSize));

    return this.httpClient
      .get<ApiResponse<PagedData<CandidateInvitation>>>(this.candidatesUrl, { params })
      .pipe(map(r => r.data ?? emptyPagedData(pageOptions.pageSize)));
  }

  // GDPR Privacy Methods
  createErasureRequest(request: DeleteMyDataRequest): Observable<DeleteDataResponse> {
    return this.httpClient.post<DeleteDataResponse>(`${this.applicantsUrl}/privacy/erasures`, request);
  }

  confirmErasure(token: string): Observable<DeleteDataResponse> {
    return this.httpClient.put<DeleteDataResponse>(`${this.applicantsUrl}/privacy/erasures/${token}`, {});
  }

  createExportRequest(request: DownloadMyDataRequest): Observable<DeleteDataResponse> {
    return this.httpClient.post<DeleteDataResponse>(`${this.applicantsUrl}/privacy/exports`, request);
  }

  downloadExport(token: string): Observable<Blob> {
    return this.httpClient.get(`${this.applicantsUrl}/privacy/exports/${token}`, {
      responseType: 'blob',
    });
  }
}
