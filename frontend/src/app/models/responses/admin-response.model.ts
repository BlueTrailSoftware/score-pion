export interface RecruiterInvitation {
  id: string;
  email: string;
  invitedBy: string;
  assignedAssessments: string[];
  status: string;
  createdAt: string;
  expiresAt: string;
  acceptedAt?: string;
}

export interface AssignAssessmentsResponse {
  assigned: string[];
  alreadyAssigned: string[];
  notFound: string[];
}
