import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { PublicPosition } from '../../../models/position.model';
import { formatExperienceRange } from './careers-position-card.utils';

@Component({
  selector: 'app-careers-position-card',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './careers-position-card.component.html',
  styleUrls: ['./careers-position-card.component.scss'],
})
export class CareersPositionCardComponent {
  @Input({ required: true }) position!: PublicPosition;
  @Output() cardClick = new EventEmitter<PublicPosition>();

  readonly maxVisibleSkills = 5;

  onCardClick(): void {
    this.cardClick.emit(this.position);
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.onCardClick();
    }
  }

  getExperienceLabel(): string | null {
    return formatExperienceRange(this.position.experienceMin, this.position.experienceMax);
  }

  get visibleSkills(): string[] {
    return (this.position.skills ?? []).slice(0, this.maxVisibleSkills);
  }

  get remainingSkillsCount(): number {
    const total = (this.position.skills ?? []).length;
    return Math.max(total - this.maxVisibleSkills, 0);
  }
}
