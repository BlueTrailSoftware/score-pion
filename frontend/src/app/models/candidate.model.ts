export interface Candidate {
  id: string;
  name: string;
  email: string;
  phone: string;
  positionId: string;
  positionTitle?: string;
  status: CandidateStatus;
  source: string;
  resumeUrl?: string;
  resumeFileName?: string;
  fileUrl?: string;
  isFileDeleted?: boolean;
  createdAt: string;
  updatedAt: string;
  statusNote?: string;
  reviewedBy?: string;
  reviewedAt?: string;
  gdprConsent?: boolean;
  gdprConsentDate?: string;
  linkedinUrl?: string;
  assessments?: AssessmentInvitationDetail[];
}

export type CandidateStatus = 'PENDING' | 'INVITED' | 'REJECTED' | 'ANONYMIZED';

export interface ApplyToPositionRequest {
  name: string;
  email: string;
  phone?: string;
  positionId: string;
  gdprConsent: boolean;
  captchaToken: string;
  linkedinUrl?: string;
}

export interface PrivacyRequest {
  email: string;
  captchaToken: string;
}

export type DeleteMyDataRequest = PrivacyRequest;

export type DownloadMyDataRequest = PrivacyRequest;

export interface DeleteDataResponse {
  message: string;
}

export interface CandidateFilter {
  status?: string;
  position?: string;
  search?: string;
  sortField?: string;
  sortDirection?: 'asc' | 'desc';
}

export interface PageOptions {
  page: number;
  pageSize: number;
}

export interface UpdateCandidateRequest {
  name?: string;
  email?: string;
  phone?: string;
  status?: string;
  deleteFile?: boolean;
}

export interface AssessmentInvitationDetail {
  assessmentId: string;
  assessmentName: string | null;
  status: string;
  finalScore: number | null;
  mcScore: number | null;
  codeScore: number | null;
  qualified: boolean | null;
  completedAt: string | null;
  plagiarism: string | null;
  pastedCode: string | null;
  suspiciousActivity: boolean | null;
  aiUsage: boolean | null;
  tabSwitchCount: number | null;
}

export interface CandidateInvitation {
  candidateEmail: string;
  candidateName: string;
  positionId: string;
  positionTitle: string | null;
  recruiterId: string;
  invitedAt: string;
  assessments: AssessmentInvitationDetail[];
}
