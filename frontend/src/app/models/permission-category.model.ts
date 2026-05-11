import { Permissions } from './permissions.model';

export interface PermissionCategory extends Permissions {
  name: string;
  permissions: Permissions[];
}
