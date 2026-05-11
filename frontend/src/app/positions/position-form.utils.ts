import { FormGroup } from '@angular/forms';
import { JOB_TYPES, WORK_MODES } from '../models/position.model';

export { JOB_TYPES, WORK_MODES };

export function isExperienceRangeInvalid(form: FormGroup): boolean {
  const min = form.get('experienceMin')?.value;
  const max = form.get('experienceMax')?.value;
  return min != null && max != null && min > max;
}

export function setLocationControlState(form: FormGroup, workMode: string): void {
  const locationControl = form.get('location');
  if (workMode === 'Remote') {
    locationControl?.disable();
  } else {
    locationControl?.enable();
  }
}

export function applyWorkModeChange(form: FormGroup, value: string): void {
  const locationControl = form.get('location');
  if (value === 'Remote') {
    locationControl?.setValue('Remote');
  } else {
    locationControl?.setValue('');
  }
  setLocationControlState(form, value);
}

export function addSkillToList(skills: string[], input: string): string | null {
  const trimmed = input.trim();
  if (!trimmed || skills.some(s => s.toLowerCase() === trimmed.toLowerCase())) return null;
  skills.push(trimmed);
  return '';
}

export function markFormGroupTouched(formGroup: FormGroup): void {
  Object.keys(formGroup.controls).forEach(key => {
    formGroup.get(key)?.markAsTouched();
  });
}
