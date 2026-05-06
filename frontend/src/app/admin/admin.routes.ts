import { Routes } from '@angular/router';

export const adminRoutes: Routes = [
  {
    path: 'candidates',
    loadComponent: () => import('./candidate-review/candidate-review.component').then(m => m.CandidateReviewComponent),
  },
  {
    path: 'manage-team',
    loadComponent: () =>
      import('./manage-recruiters/manage-recruiters.component').then(m => m.ManageRecruitersComponent),
  },
  {
    path: 'global-recipients',
    loadComponent: () =>
      import('./global-recipients/global-recipients.component').then(m => m.GlobalRecipientsComponent),
  },
  { path: '', redirectTo: 'candidates', pathMatch: 'full' },
];
