import { PermissionCategory } from './permission-category.model';

export interface RolesPermissionList extends PermissionCategory {
  newRole: string;
  roles: PermissionCategory[];
}
