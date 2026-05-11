export interface AssessmentAccess {
  id: string;
  recruiterId: string;
  recruiterEmail: string;
  recruiterName: string;
  assessmentId: string;
  assessmentName: string;
  grantedAt: string;
  grantedBy: string;
  isActive: boolean;
}

export interface UserProfile {
  id?: string;
  email?: string;
  name?: string;
  pictureUrl?: string;
  role?: string;
  createdAt?: string;
  isActive?: boolean;
  status?: string;
  positionsCount?: number; // Number of positions assigned to recruiter
}

export interface RecruiterPosition {
  id: string;
  title: string;
  description: string;
  external: boolean;
  assessmentsCount: number;
  isActive: boolean;
  createdAt: string;
}
