import { RolePermissions } from '../role-permissions.model';
import { RoleUsers } from '../role-users.model';

export interface RolePagination {
  currentPage: number;
  pages: number;
  items: RolePermissions[] | RoleUsers[];
}
