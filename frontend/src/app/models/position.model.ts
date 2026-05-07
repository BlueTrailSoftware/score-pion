export const JOB_TYPES = ['Full Time', 'Part Time', 'Contract', 'Internship'] as const;
export const WORK_MODES = ['Onsite', 'Remote', 'Hybrid'] as const;

export type JobType = (typeof JOB_TYPES)[number];
export type WorkMode = (typeof WORK_MODES)[number];

export interface Position {
  id: string;
  title: string;
  description: string;
  external: boolean;
  createdBy: string; // Admin ID
  createdAt: Date;
  isActive: boolean;
  assessments?: PositionAssessment[];
  recruiterAccess?: PositionRecruiterAccess[];
  assessmentCount?: number;
  activeRecruitersCount?: number;
  status?: 'active' | 'closed';
  fileUrl?: string;
  fileName?: string;
  jobType?: JobType;
  workMode?: WorkMode;
  experienceMin?: number;
  experienceMax?: number;
  location?: string;
  skills?: string[];
}

export interface PositionListItem {
  id: string;
  title: string;
  description: string;
  external: boolean;
  assessmentsCount: number;
  isActive: boolean;
  createdAt: Date;
}

export interface PositionAssessment {
  assessmentId: string;
  assessmentName: string;
  addedAt?: Date;
}

export interface PositionRecruiterAccess {
  id: string;
  recruiterId: string;
  openPositionId: string;
  grantedBy: string;
  grantedAt: Date;
  isActive: boolean;
  recruiterName?: string;
  recruiterEmail?: string;
}

export interface CreatePositionRequest {
  title: string;
  description: string;
  external: boolean;
  assessments?: string[];
  jobType?: JobType;
  workMode: WorkMode;
  experienceMin?: number;
  experienceMax?: number;
  location: string;
  skills?: string[];
}

export interface UpdatePositionStatusRequest {
  isActive: boolean;
}

export interface UpdatePositionRequest {
  title?: string;
  description?: string;
  external?: boolean;
  isActive?: boolean;
  deleteFile?: boolean;
  assessmentIds?: string[];
  jobType?: JobType;
  workMode?: WorkMode;
  experienceMin?: number;
  experienceMax?: number;
  location?: string;
  skills?: string[];
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

export interface PositionFilters {
  activeOnly?: boolean;
  createdBy?: string;
  hasAssessments?: boolean;
  searchTerm?: string;
  page?: number;
  pageSize?: number;
}

export interface PublicPosition {
  id: string;
  title: string;
  description: string;
  fileUrl: string | null;
  createdAt: Date;
  jobType?: JobType;
  workMode?: WorkMode;
  experienceMin?: number;
  experienceMax?: number;
  location?: string;
  skills?: string[];
}

export interface PublicPositionListCache {
  data: PublicPosition[];
  timestamp: number;
}
