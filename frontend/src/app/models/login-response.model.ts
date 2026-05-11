import { PermissionsResponse } from './responses/permissions-response.model';

export interface LoginResponse extends Response {
  data: {
    id: number;
    token: string;
    permissions: PermissionsResponse[];
  };
}
