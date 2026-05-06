import { Routes } from '@angular/router';

export const recruiterRoutes: Routes = [
  {
    path: 'candidates',
    loadComponent: () =>
      import('./recruiter-candidates/recruiter-candidates.component').then(m => m.RecruiterCandidatesComponent),
  },
  { path: '', redirectTo: 'candidates', pathMatch: 'full' },
];
