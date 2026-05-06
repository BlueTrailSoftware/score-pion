import { CustomResponse } from './custom-response.model';
import { PermissionsList } from '../roles-permissions-list.model';

export interface PermissionsResponse extends PermissionsList, CustomResponse {
  data: PermissionsList[];
}
