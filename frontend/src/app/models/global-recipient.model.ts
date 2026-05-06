export interface GlobalRecipient {
  email: string;
  enabled: boolean;
}

export interface GlobalRecipientsSettings {
  emails: string[];
  description: string;
  updatedAt: string;
  updatedBy: string | null;
}

export interface CreateGlobalRecipientRequest {
  email: string;
  name?: string;
  enabled?: boolean;
}

export interface UpdateGlobalRecipientRequest {
  email?: string;
  name?: string;
  enabled?: boolean;
}
