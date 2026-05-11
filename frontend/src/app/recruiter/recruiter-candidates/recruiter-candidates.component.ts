import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CandidatesService } from '../../services/candidates.service';
import { NotificationService } from '../../services/notification.service';
import { CandidateInvitation } from '../../models/candidate.model';
import { emptyPagedData } from '../../models/responses/api-base-response.model';
import { GradientPageHeaderComponent } from '../../shared/gradient-page-header/gradient-page-header.component';
import { AssessmentTableComponent } from '../../shared/assessment-table/assessment-table.component';
import { PaginationComponent } from '../../shared/pagination/pagination.component';
import { of } from 'rxjs';
import { catchError, finalize, tap } from 'rxjs/operators';

@Component({
  selector: 'app-recruiter-candidates',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    GradientPageHeaderComponent,
    DatePipe,
    AssessmentTableComponent,
    PaginationComponent,
  ],
  templateUrl: './recruiter-candidates.component.html',
  styleUrls: ['./recruiter-candidates.component.scss'],
})
export class RecruiterCandidatesComponent implements OnInit {
  private candidatesService = inject(CandidatesService);
  private notificationService = inject(NotificationService);

  candidates: CandidateInvitation[] = [];
  loading = false;

  filterSearch = '';

  currentPage = 1;
  pageSize = 10;
  total = 0;

  ngOnInit(): void {
    this.loadCandidates();
  }

  loadCandidates(): void {
    this.loading = true;
    this.candidatesService
      .getInvitedCandidates(undefined, this.filterSearch || undefined, {
        page: this.currentPage,
        pageSize: this.pageSize,
      })
      .pipe(
        tap(result => {
          this.candidates = result.items;
          this.total = result.total;
        }),
        catchError(error => {
          console.error('Error loading candidates:', error);
          this.notificationService.error('Failed to load candidates');
          return of(emptyPagedData<CandidateInvitation>(this.pageSize));
        }),
        finalize(() => {
          this.loading = false;
        }),
      )
      .subscribe();
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.loadCandidates();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadCandidates();
  }
}
