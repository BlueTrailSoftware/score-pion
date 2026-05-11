import { Routes } from '@angular/router';
import { PositionDetailComponent } from './position-detail/position-detail.component';
import { PositionCreateComponent } from './position-create/position-create.component';
import { AdminGuardService } from '../services/guard/admin-guard.service';
import { PositionsManageComponent } from './positions-manage/positions-manage.component';

export const positionsRoutes: Routes = [
  { path: '', component: PositionsManageComponent },
  {
    path: 'create',
    component: PositionCreateComponent,
    canActivate: [AdminGuardService],
  },
  { path: ':id', component: PositionDetailComponent },
];
