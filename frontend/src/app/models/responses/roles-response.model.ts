import { RolePagination } from '../shared/role-pagination.model';
import { CustomResponse } from './custom-response.model';

export interface RolesResponse extends CustomResponse {
  data: RolePagination;
}
