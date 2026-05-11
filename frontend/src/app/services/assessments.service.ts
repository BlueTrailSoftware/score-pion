import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Assessment } from '../models/assessment.model';
import { ApiResponse } from '../models/responses/api-base-response.model';

@Injectable({
  providedIn: 'root',
})
export class AssessmentsService {
  private httpClient = inject(HttpClient);

  /**
   * Get assessments filtered by user role (admin sees all, recruiter sees assigned)
   */
  public getAssessments(): Observable<ApiResponse<Assessment[]>> {
    return this.httpClient.get<ApiResponse<Assessment[]>>('/admin/assessments');
  }

  /**
   * Send assessment invitations to candidate via Coderbyte
   */
  public inviteCandidate(email: string, assessments: string[]): Observable<void> {
    return this.httpClient.post<void>('/assessments/invite', {
      email,
      assessments,
    });
  }
}
