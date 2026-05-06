export interface Assessment {
  displayName: string;
  testID: string;
}
export interface CreateExamTicketRequest {
  readyDate: string;
  description: string;
}

export interface CreateExamTicketResponse {
  ticketId: string;
  email: string;
  readyDate: string;
  description: string;
  createdAt: string;
}
