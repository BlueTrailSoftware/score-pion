import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { GoogleSsoComponent } from './google-sso/google-sso.component';
import { LoginGuardService } from './services/guard/login-guard.service';
import { AuthGuardService } from './services/guard/auth-guard.service';
import { AdminGuardService } from './services/guard/admin-guard.service';
import { RecruiterGuardService } from './services/guard/recruiter-guard.service';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';
import { PublicLayoutComponent } from './layouts/public-layout/public-layout.component';
import { CareersListComponent } from './public/careers/careers-list.component';
import { CareersDetailComponent } from './public/careers/careers-detail.component';
import { ApplyFormComponent } from './public/careers/apply-form.component';
import { PrivacyPolicyComponent } from './public/privacy/privacy-policy.component';
import { DeleteMyDataComponent } from './public/privacy/delete-my-data.component';
import { DeleteConfirmComponent } from './public/privacy/delete-confirm.component';
import { DownloadMyDataComponent } from './public/privacy/download-my-data.component';
import { DownloadConfirmComponent } from './public/privacy/download-confirm.component';

export const routes: Routes = [
  // default route - must come before the empty-path PublicLayout
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login',
  },
  // public routes - wrapped in PublicLayoutComponent (waves bg + footer)
  {
    path: '',
    component: PublicLayoutComponent,
    children: [
      {
        path: 'careers',
        children: [
          {
            path: '',
            component: CareersListComponent,
          },
          {
            path: ':id',
            component: CareersDetailComponent,
          },
          {
            path: ':id/apply',
            component: ApplyFormComponent,
          },
        ],
      },
      {
        path: 'privacy-policy',
        component: PrivacyPolicyComponent,
      },
      {
        path: 'privacy',
        children: [
          {
            path: 'delete-my-data',
            component: DeleteMyDataComponent,
          },
          {
            path: 'erasures/:token',
            component: DeleteConfirmComponent,
          },
          {
            path: 'download-my-data',
            component: DownloadMyDataComponent,
          },
          {
            path: 'exports/:token',
            component: DownloadConfirmComponent,
          },
        ],
      },
    ],
  },
  {
    path: 'admin',
    component: MainLayoutComponent,
    canActivate: [AdminGuardService],
    children: [
      {
        path: '',
        loadChildren: () => import('./admin/admin.routes').then(m => m.adminRoutes),
      },
    ],
  },
  {
    path: 'recruiter',
    component: MainLayoutComponent,
    canActivate: [RecruiterGuardService],
    children: [
      {
        path: '',
        loadChildren: () => import('./recruiter/recruiter.routes').then(m => m.recruiterRoutes),
      },
    ],
  },
  {
    path: 'positions-manage',
    component: MainLayoutComponent,
    canActivate: [AuthGuardService],
    children: [
      {
        path: '',
        loadChildren: () => import('./positions/positions.routes').then(m => m.positionsRoutes),
      },
    ],
  },
  // login routes (standalone, no layout, no footer)
  {
    path: 'login',
    canActivate: [LoginGuardService],
    component: LoginComponent,
  },
  {
    path: 'google-sso',
    canActivate: [LoginGuardService],
    component: GoogleSsoComponent,
  },
  { path: 'robots.txt', children: [] },
];
