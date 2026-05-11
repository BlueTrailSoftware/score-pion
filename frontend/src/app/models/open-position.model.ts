export interface OpenPosition {
  id: string;
  title: string;
  description: string;
  createdBy: string; // Admin ID
  createdAt: Date;
  isActive: boolean;
  assessments?: OpenPositionAssessment[];
  recruiterAccess?: OpenPositionRecruiterAccess[];
  assessmentCount?: number;
  activeRecruitersCount?: number;
  status?: 'active' | 'closed';
}

export interface OpenPositionAssessment {
  id: string;
  openPositionId: string;
  assessmentId: string;
  assessmentName: string;
  assessmentDescription?: string;
  assessmentDuration?: number;
  isActive?: boolean;
  assignedAt?: Date;
  assignedBy?: string;
}

export interface OpenPositionRecruiterAccess {
  id: string;
  recruiterId: string;
  openPositionId: string;
  grantedBy: string;
  grantedAt: Date;
  isActive: boolean;
  recruiterName?: string;
  recruiterEmail?: string;
}

export interface CreateOpenPositionRequest {
  title: string;
  description: string;
  assessments?: string[];
  recruiters?: string[];
}

export interface UpdateOpenPositionRequest {
  title?: string;
  description?: string;
  isActive?: boolean;
}

export interface AddAssessmentToPositionRequest {
  assessmentId: string;
  assessmentName: string;
}

export interface RemoveAssessmentFromPositionRequest {
  assessmentId: string;
}

export interface GrantRecruiterAccessRequest {
  recruiterId: string;
}

export interface RevokeRecruiterAccessRequest {
  recruiterAccessId: string;
}

export interface OpenPositionFilters {
  status?: 'active' | 'closed' | 'all';
  createdBy?: string;
  hasAssessments?: boolean;
  searchTerm?: string;
  page?: number;
  pageSize?: number;
}

export interface OpenPositionListResponse {
  positions: OpenPosition[];
  totalCount: number;
  page: number;
  pageSize: number;
  totalPages: number;
}
