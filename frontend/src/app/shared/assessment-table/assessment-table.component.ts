import { Component, Input, ChangeDetectionStrategy, inject, Renderer2 } from '@angular/core';
import { AssessmentInvitationDetail } from '../../models/candidate.model';

type AggregateStatus = 'Invited' | 'In Progress' | 'Completed';

const COMPLETED_STATUSES = ['completed', 'submitted'];
const IN_PROGRESS_STATUSES = ['in_progress', 'in progress'];
const INVITED_STATUSES = ['invited', 'pending'];

@Component({
  selector: 'app-assessment-table',
  standalone: true,
  templateUrl: './assessment-table.component.html',
  styleUrls: ['./assessment-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AssessmentTableComponent {
  private readonly renderer = inject(Renderer2);

  @Input() assessments: AssessmentInvitationDetail[] = [];

  showModal = false;

  getAggregateStatus(): AggregateStatus {
    const all = this.assessments;
    if (!all.length) return 'Invited';

    const allCompleted = all.every(a => COMPLETED_STATUSES.includes(a.status?.toLowerCase() ?? ''));
    if (allCompleted) return 'Completed';

    const allInvited = all.every(a => INVITED_STATUSES.includes(a.status?.toLowerCase() ?? ''));
    return allInvited ? 'Invited' : 'In Progress';
  }

  getStatusClass(): string {
    const status = this.getAggregateStatus();
    if (status !== 'Completed') {
      return status === 'In Progress' ? 'status-in-progress' : 'status-invited';
    }
    const allQualified = this.assessments.every(a => a.qualified);
    if (allQualified) return 'status-all-passed';
    const noneQualified = this.assessments.every(a => !a.qualified);
    return noneQualified ? 'status-all-failed' : 'status-some-failed';
  }

  getAggregateIcon(): string {
    switch (this.getAggregateStatus()) {
      case 'Completed':
        return 'bi-check-circle-fill';
      case 'In Progress':
        return 'bi-arrow-repeat';
      default:
        return 'bi-hourglass';
    }
  }

  hasCompletedAssessments(): boolean {
    return this.assessments.some(a => COMPLETED_STATUSES.includes(a.status?.toLowerCase() ?? ''));
  }

  getCompletedCount(): number {
    return this.assessments.filter(a => COMPLETED_STATUSES.includes(a.status?.toLowerCase() ?? '')).length;
  }

  isCompleted(assessment: AssessmentInvitationDetail): boolean {
    return COMPLETED_STATUSES.includes(assessment.status?.toLowerCase() ?? '');
  }

  isInProgress(assessment: AssessmentInvitationDetail): boolean {
    return IN_PROGRESS_STATUSES.includes(assessment.status?.toLowerCase() ?? '');
  }

  getTooltipText(): string {
    return this.assessments
      .map(a => {
        const name = a.assessmentName || a.assessmentId;
        const s = a.status?.toLowerCase() ?? '';
        if (COMPLETED_STATUSES.includes(s)) {
          const q = a.qualified ? '✓' : '✗';
          return `${name}: ${a.finalScore ?? 0}% ${q}`;
        }
        if (IN_PROGRESS_STATUSES.includes(s)) {
          return `${name}: In Progress`;
        }
        return `${name}: Invited`;
      })
      .join('\n');
  }

  openModal(): void {
    if (!this.hasCompletedAssessments()) return;
    this.showModal = true;
    this.renderer.addClass(document.body, 'modal-open');
  }

  closeModal(): void {
    this.showModal = false;
    this.renderer.removeClass(document.body, 'modal-open');
  }

  getCheatingLabel(assessment: AssessmentInvitationDetail): string {
    const flags: string[] = [];
    if (assessment.plagiarism && assessment.plagiarism !== 'Not detected')
      flags.push(`Plagiarism: ${assessment.plagiarism}`);
    if (assessment.pastedCode && assessment.pastedCode !== 'Not detected')
      flags.push(`Pasted Code: ${assessment.pastedCode}`);
    if (assessment.suspiciousActivity) flags.push('Suspicious Activity');
    if (assessment.aiUsage) flags.push('AI Usage');
    if (assessment.tabSwitchCount && assessment.tabSwitchCount > 0)
      flags.push(`Tab Switches: ${assessment.tabSwitchCount}`);
    return flags.length ? flags.join(' · ') : 'Clean';
  }
}
