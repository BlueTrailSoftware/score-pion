import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { catchError, map, tap, shareReplay } from 'rxjs/operators';
import {
  PositionFilters,
  PositionListItem,
  Position,
  UpdatePositionRequest,
  CreatePositionRequest,
  UpdatePositionStatusRequest,
  PublicPosition,
  PublicPositionListCache,
} from '../models/position.model';
import { ApiResponse, PagedData, emptyPagedData } from '../models/responses/api-base-response.model';
import { InviteCandidateRequest } from '../models/recruiter.model';
import { FileHelper } from '../helpers/fileHelper';

@Injectable({
  providedIn: 'root',
})
export class PositionService {
  private readonly http = inject(HttpClient);

  private readonly positionsUrl = '/positions';
  private readonly adminPositionsUrl = '/admin/positions';
  private readonly candidatesInviteUrl = '/candidates/invite';
  private readonly publicPositionsUrl = '/applicants/positions';

  private readonly publicPositionsCache$ = new BehaviorSubject<PublicPositionListCache | null>(null);
  private readonly CACHE_TTL_MS = 2 * 60 * 1000;

  public getPositions(filters?: PositionFilters): Observable<PagedData<PositionListItem>> {
    let params = new HttpParams();
    if (filters?.activeOnly !== undefined) {
      params = params.set('activeOnly', String(filters.activeOnly));
    }
    if (filters?.page !== undefined) {
      params = params.set('page', String(filters.page - 1));
    }
    if (filters?.pageSize !== undefined) {
      params = params.set('pageSize', String(filters.pageSize));
    }
    return this.http
      .get<ApiResponse<PagedData<PositionListItem>>>(this.positionsUrl, { params })
      .pipe(map(response => response.data ?? emptyPagedData(filters?.pageSize ?? 10)));
  }

  public getPositionById(positionId: string): Observable<ApiResponse<Position>> {
    return this.http.get<ApiResponse<Position>>(`${this.positionsUrl}/${positionId}`);
  }

  public createPosition(createPositionRequest: CreatePositionRequest, file?: File): Observable<ApiResponse<Position>> {
    const formData = FileHelper.createFormData(createPositionRequest, file);
    return this.http.post<ApiResponse<Position>>(this.adminPositionsUrl, formData);
  }

  public updatePosition(
    positionId: string,
    request: UpdatePositionRequest,
    file?: File,
  ): Observable<ApiResponse<Position>> {
    const formData = FileHelper.createFormData(request, file);
    return this.http.put<ApiResponse<Position>>(`${this.adminPositionsUrl}/${positionId}`, formData);
  }

  public updatePositionState(positionId: string, request: UpdatePositionStatusRequest): Observable<ApiResponse<null>> {
    return this.http.patch<ApiResponse<null>>(`${this.adminPositionsUrl}/${positionId}/activate`, request);
  }

  /**
   * Invite a candidate to a specific position
   */
  inviteCandidateToPosition(
    email: string,
    candidateName: string,
    positionId: string,
  ): Observable<ApiResponse<InviteCandidateRequest>> {
    const payload: InviteCandidateRequest = {
      email,
      candidateName,
      positionId,
    };

    return this.http.post<ApiResponse<InviteCandidateRequest>>(this.candidatesInviteUrl, payload);
  }

  /**
   * Get all public positions (no authentication required)
   */
  public getPublicPositions(forceRefresh: boolean = false): Observable<ApiResponse<PublicPosition[]>> {
    const cachedData = this.publicPositionsCache$.getValue();
    const now = Date.now();

    // Return cached data if valid and not forcing refresh
    if (!forceRefresh && cachedData && now - cachedData.timestamp < this.CACHE_TTL_MS) {
      return of({
        status: 'success',
        message: 'Public positions retrieved from cache',
        data: cachedData.data,
      });
    }

    return this.http.get<ApiResponse<PublicPosition[]>>(this.publicPositionsUrl).pipe(
      tap(response => {
        if (response.data) {
          this.publicPositionsCache$.next({
            data: response.data,
            timestamp: now,
          });
        }
      }),
      catchError(error => {
        console.error('Error fetching public positions:', error);
        return throwError(() => error);
      }),
      shareReplay(1),
    );
  }

  /**
   * Get a single public position by ID (no authentication required)
   */
  public getPublicPositionById(positionId: string): Observable<ApiResponse<PublicPosition>> {
    return this.http.get<ApiResponse<PublicPosition>>(`${this.publicPositionsUrl}/${positionId}`);
  }

  /**
   * Clear the public positions cache (useful for testing or force refresh)
   */
  public clearPublicPositionsCache(): void {
    this.publicPositionsCache$.next(null);
  }
}
