import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { UserProfile, AssessmentAccess, RecruiterPosition } from '../models/user-profile.model';
import { InviteRecruiterRequest } from '../models/recruiter.model';
import { RecruiterInvitation, AssignAssessmentsResponse } from '../models/responses/admin-response.model';
import { ApiResponse, PagedData, emptyPagedData } from '../models/responses/api-base-response.model';
import { CreateExamTicketRequest, CreateExamTicketResponse } from '../models/assessment.model';
import { GlobalRecipientsSettings } from '../models/global-recipient.model';

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  private httpClient = inject(HttpClient);

  /**
   * Send invitation email to a new recruiter with assigned assessments.
   * @param request - Recruiter invitation data including email and assessment IDs.
   * @returns Observable with the created recruiter invitation.
   */
  public inviteRecruiter(request: InviteRecruiterRequest): Observable<ApiResponse<RecruiterInvitation>> {
    return this.httpClient.post<ApiResponse<RecruiterInvitation>>('/admin/recruiters/invite', request);
  }

  /**
   * Send invitation email to a new admin.
   * @param request - Object containing the admin's email address.
   * @returns Observable with the created admin invitation.
   */
  public inviteAdmin(request: { email: string }): Observable<ApiResponse<RecruiterInvitation>> {
    return this.httpClient.post<ApiResponse<RecruiterInvitation>>('/admin/invite', request);
  }

  /**
   * Get all recruiters with their assessment access details.
   * @param page - Zero-based page index (default 0).
   * @param pageSize - Number of items per page (default 50, max 50).
   * @returns Observable with a paginated result of recruiter profiles.
   */
  public getAllRecruiters(page: number = 0, pageSize: number = 50): Observable<PagedData<UserProfile>> {
    const params = new HttpParams().set('page', String(page)).set('pageSize', String(pageSize));
    return this.httpClient
      .get<ApiResponse<PagedData<UserProfile>>>('/admin/recruiters', { params })
      .pipe(map(response => response.data ?? emptyPagedData(pageSize)));
  }

  /**
   * Get all admins.
   * @param page - Zero-based page index (default 0).
   * @param pageSize - Number of items per page (default 50, max 50).
   * @returns Observable with a paginated result of admin profiles.
   */
  public getAllAdmins(page: number = 0, pageSize: number = 50): Observable<PagedData<UserProfile>> {
    const params = new HttpParams().set('page', String(page)).set('pageSize', String(pageSize));
    return this.httpClient
      .get<ApiResponse<PagedData<UserProfile>>>('/admin/admins', { params })
      .pipe(map(response => response.data ?? emptyPagedData(pageSize)));
  }

  /**
   * Get recruiter details by ID.
   * @param id - The recruiter's unique identifier.
   * @returns Observable with the recruiter's profile wrapped in ApiResponse.
   */
  public getRecruiterById(id: string): Observable<ApiResponse<UserProfile>> {
    return this.httpClient.get<ApiResponse<UserProfile>>(`/admin/recruiters/${id}`);
  }

  /**
   * Update recruiter active/inactive status.
   * @param id - The recruiter's unique identifier.
   * @param isActive - Whether the recruiter should be active.
   * @returns Observable with the updated recruiter profile wrapped in ApiResponse.
   */
  public updateActiveStatus(id: string, isActive: boolean): Observable<ApiResponse<UserProfile>> {
    return this.httpClient.put<ApiResponse<UserProfile>>(`/admin/recruiters/${id}/activate`, { isActive });
  }

  /**
   * Update admin active/inactive status.
   * @param id - The admin's unique identifier.
   * @param isActive - Whether the admin should be active.
   * @returns Observable with the updated admin profile wrapped in ApiResponse.
   */
  public updateAdminActiveStatus(id: string, isActive: boolean): Observable<ApiResponse<UserProfile>> {
    return this.httpClient.put<ApiResponse<UserProfile>>(`/admin/admins/${id}/activate`, { isActive });
  }

  /**
   * Assign assessments to a recruiter, syncing with the provided list.
   * @param id - The recruiter's unique identifier.
   * @param assessmentIds - List of assessment IDs to assign.
   * @returns Observable with the assignment result wrapped in ApiResponse.
   */
  public assignAssessments(id: string, assessmentIds: string[]): Observable<ApiResponse<AssignAssessmentsResponse>> {
    return this.httpClient.post<ApiResponse<AssignAssessmentsResponse>>(`/admin/recruiters/${id}/assessments`, {
      assessmentIds,
    });
  }

  /**
   * Get all assessments assigned to a recruiter.
   * @param id - The recruiter's unique identifier.
   * @returns Observable with the list of assessment access records wrapped in ApiResponse.
   */
  public getRecruiterAssessments(id: string): Observable<ApiResponse<AssessmentAccess[]>> {
    return this.httpClient.get<ApiResponse<AssessmentAccess[]>>(`/admin/recruiters/${id}/assessments`);
  }

  /**
   * Get all pending recruiter invitations.
   * @returns Observable with the list of pending invitations wrapped in ApiResponse.
   */
  public getPendingInvitations(): Observable<ApiResponse<RecruiterInvitation[]>> {
    return this.httpClient.get<ApiResponse<RecruiterInvitation[]>>('/admin/invitations');
  }

  /**
   * Create an exam ticket for a candidate.
   * @param request - Exam ticket creation data.
   * @returns Observable with the created exam ticket wrapped in ApiResponse.
   */
  public createExamTicket(request: CreateExamTicketRequest): Observable<ApiResponse<CreateExamTicketResponse>> {
    return this.httpClient.post<ApiResponse<CreateExamTicketResponse>>('/admin/ticket', request);
  }

  /**
   * Get all positions assigned to a recruiter.
   * @param recruiterId - The recruiter's unique identifier.
   * @param page - Zero-based page index (default 0).
   * @param pageSize - Number of items per page (default 50, max 50).
   * @returns Observable with a paginated result of assigned positions.
   */
  public getRecruiterPositions(
    recruiterId: string,
    page: number = 0,
    pageSize: number = 50,
  ): Observable<PagedData<RecruiterPosition>> {
    const params = new HttpParams().set('page', String(page)).set('pageSize', String(pageSize));
    return this.httpClient
      .get<ApiResponse<PagedData<RecruiterPosition>>>(`/admin/recruiters/${recruiterId}/positions`, { params })
      .pipe(map(response => response.data ?? emptyPagedData(pageSize)));
  }

  /**
   * Assign positions to a recruiter, syncing with the provided list.
   * @param recruiterId - The recruiter's unique identifier.
   * @param positionIds - List of position IDs to assign.
   * @returns Observable with the result wrapped in ApiResponse.
   */
  public assignPositions(recruiterId: string, positionIds: string[]): Observable<ApiResponse<void>> {
    return this.httpClient.put<ApiResponse<void>>(`/admin/recruiters/${recruiterId}/positions`, { positionIds });
  }

  /**
   * Get all positions available in the system.
   * @param activeOnly - If true, returns only active positions. Defaults to true.
   * @param page - Zero-based page index (default 0).
   * @param pageSize - Number of items per page (default 50, max 50).
   * @returns Observable with a paginated result of positions.
   */
  public getAllPositions(
    activeOnly: boolean = true,
    page: number = 0,
    pageSize: number = 50,
  ): Observable<PagedData<RecruiterPosition>> {
    const params = new HttpParams()
      .set('activeOnly', String(activeOnly))
      .set('page', String(page))
      .set('pageSize', String(pageSize));
    return this.httpClient
      .get<ApiResponse<PagedData<RecruiterPosition>>>('/admin/positions', { params })
      .pipe(map(response => response.data ?? emptyPagedData(pageSize)));
  }

  /**
   * Get global recipients settings.
   * @returns Observable with the global recipients settings including email list and metadata.
   */
  public getGlobalRecipients(): Observable<GlobalRecipientsSettings> {
    return this.httpClient
      .get<ApiResponse<GlobalRecipientsSettings>>('/settings/global-recipients')
      .pipe(
        map(
          (response): GlobalRecipientsSettings =>
            response.data ?? { emails: [], description: '', updatedAt: '', updatedBy: null },
        ),
      );
  }

  /**
   * Add a new global recipient email.
   * @param email - The email address to add.
   * @returns Observable with the updated global recipients settings.
   */
  public addGlobalRecipient(email: string): Observable<GlobalRecipientsSettings | undefined> {
    return this.httpClient
      .post<ApiResponse<GlobalRecipientsSettings>>('/settings/global-recipients/emails', { email })
      .pipe(map(response => response.data));
  }

  /**
   * Delete a global recipient email.
   * @param email - The email address to remove.
   * @returns Observable with the updated global recipients settings.
   */
  public deleteGlobalRecipient(email: string): Observable<GlobalRecipientsSettings | undefined> {
    return this.httpClient
      .delete<ApiResponse<GlobalRecipientsSettings>>(`/settings/global-recipients/emails/${encodeURIComponent(email)}`)
      .pipe(map(response => response.data));
  }
}
