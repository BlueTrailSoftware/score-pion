export interface InviteRecruiterRequest {
  email: string;
  positionIds?: string[];
}

export interface InviteAdminRequest {
  email: string;
}

export interface InviteCandidateRequest {
  email: string;
  candidateName: string;
  positionId: string;
}
