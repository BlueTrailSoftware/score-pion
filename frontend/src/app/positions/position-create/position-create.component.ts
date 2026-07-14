import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';

import { PositionService } from '../../services/position.service';
import { AssessmentsService } from '../../services/assessments.service';
import { Assessment } from '../../models/assessment.model';
import { ApiResponse } from '../../models/responses/api-base-response.model';
import { FileHelper } from '../../helpers/fileHelper';
import { CreatePositionRequest } from '../../models/position.model';
import {
  addSkillToList,
  applyWorkModeChange,
  isExperienceRangeInvalid,
  JOB_TYPES,
  markFormGroupTouched,
  WORK_MODES,
} from '../position-form.utils';

@Component({
  selector: 'app-position-create',
  templateUrl: './position-create.component.html',
  styleUrls: ['./position-create.component.scss'],
  imports: [ReactiveFormsModule, FormsModule],
})
export class PositionCreateComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private positionService = inject(PositionService);
  private assessmentsService = inject(AssessmentsService);

  public positionForm: FormGroup;
  public availableAssessments: Assessment[] = [];
  public filteredAssessments: Assessment[] = [];
  public searchTerm: string = '';
  public selectedAssessmentIds: Set<string> = new Set();
  public loadingAssessments = false;
  public showAvailableAssessments = false;
  public saving = false;
  public isLoading = false;
  public error: string | null = null;
  public selectedFile: File | undefined;
  public fileError: string | null = null;

  readonly JOB_TYPES = JOB_TYPES;
  readonly WORK_MODES = WORK_MODES;

  public skills: string[] = [];
  public skillInput: string = '';

  private destroy$ = new Subject<void>();

  constructor() {
    this.positionForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]],
      isExternal: [false],
      jobType: [''],
      workMode: ['', [Validators.required]],
      experienceMin: [null, [Validators.min(0), Validators.max(20)]],
      experienceMax: [null, [Validators.min(0), Validators.max(20)]],
      location: ['', [Validators.required, Validators.maxLength(200)]],
    });
  }

  ngOnInit(): void {
    this.loadAvailableAssessments();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadAvailableAssessments(): void {
    this.isLoading = true;
    this.assessmentsService
      .getAssessments()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: ApiResponse<Assessment[]>) => {
          this.availableAssessments = response.data || [];
          this.filteredAssessments = this.availableAssessments;
          this.isLoading = false;
        },
        error: (error: unknown) => {
          console.error('Error loading assessments:', error);
          this.availableAssessments = [];
          this.filteredAssessments = [];
          this.isLoading = false;
          this.error = 'Failed to load assessments';
        },
      });
  }

  public searchAssessments(searchTerm: string): void {
    this.searchTerm = searchTerm;

    if (!searchTerm.trim()) {
      this.filteredAssessments = this.availableAssessments;
      return;
    }

    const term = searchTerm.toLowerCase();
    this.filteredAssessments = this.availableAssessments.filter(assessment => {
      return assessment.displayName?.toLowerCase().includes(term) || assessment.testID?.toLowerCase().includes(term);
    });
  }

  public toggleAvailableAssessments(): void {
    this.showAvailableAssessments = !this.showAvailableAssessments;
  }

  public onAssessmentToggle(testID: string, event: Event): void {
    const target = event.target as HTMLInputElement;
    if (target.checked) {
      this.selectedAssessmentIds.add(testID);
    } else {
      this.selectedAssessmentIds.delete(testID);
    }
  }

  public isAssessmentSelected(testID: string): boolean {
    return this.selectedAssessmentIds.has(testID);
  }

  public removeAssessment(assessmentId: string): void {
    this.selectedAssessmentIds.delete(assessmentId);
  }

  public getSelectedAssessments() {
    return Array.from(this.selectedAssessmentIds).map(testID => {
      const assessment = this.availableAssessments.find(a => a.testID === testID);
      return {
        testID,
        displayName: assessment?.displayName || testID,
      };
    });
  }

  public addSkill(value: string): void {
    const result = addSkillToList(this.skills, value);
    if (result !== null) this.skillInput = result;
  }

  public removeSkill(index: number): void {
    this.skills.splice(index, 1);
  }

  public onSkillKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === ',') {
      event.preventDefault();
      this.addSkill(this.skillInput);
    }
  }

  public isExperienceRangeInvalid(): boolean {
    return isExperienceRangeInvalid(this.positionForm);
  }

  public hasExperienceMinValueError(): boolean {
    return !!(this.experienceMin?.hasError('min') || this.experienceMax?.hasError('min'));
  }

  public hasExperienceMaxValueError(): boolean {
    return !!(this.experienceMin?.hasError('max') || this.experienceMax?.hasError('max'));
  }

  public onWorkModeChange(value: string): void {
    applyWorkModeChange(this.positionForm, value);
  }

  public createPosition(): void {
    if (this.positionForm.invalid || this.isExperienceRangeInvalid()) {
      markFormGroupTouched(this.positionForm);
      return;
    }

    if (this.selectedFile) {
      const validation = FileHelper.validateFile(this.selectedFile);
      if (!validation.valid) {
        this.error = validation.error || 'Invalid file';
        return;
      }
    }

    this.saving = true;
    this.error = null;

    const formValue = this.positionForm.getRawValue();
    const createPayload: CreatePositionRequest = {
      title: formValue.title,
      description: formValue.description,
      external: formValue.isExternal,
      assessments: Array.from(this.selectedAssessmentIds),
      location: formValue.location,
      workMode: formValue.workMode,
    };

    if (formValue.jobType) {
      createPayload.jobType = formValue.jobType;
    }
    if (formValue.experienceMin != null) {
      createPayload.experienceMin = formValue.experienceMin;
    }
    if (formValue.experienceMax != null) {
      createPayload.experienceMax = formValue.experienceMax;
    }
    if (this.skills.length > 0) {
      createPayload.skills = [...this.skills];
    }

    this.positionService
      .createPosition(createPayload, this.selectedFile)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.saving = false)),
      )
      .subscribe({
        next: () => {
          this.router.navigate(['/positions-manage']);
        },
        error: error => {
          this.error = error.message || 'Error creating position';
          console.error('Error creating position:', error);
        },
      });
  }

  public goBack(): void {
    this.router.navigate(['/positions-manage']);
  }

  public get title() {
    return this.positionForm.get('title');
  }
  public get description() {
    return this.positionForm.get('description');
  }
  public get isExternal() {
    return this.positionForm.get('isExternal');
  }
  public get location() {
    return this.positionForm.get('location');
  }
  public get experienceMin() {
    return this.positionForm.get('experienceMin');
  }
  public get experienceMax() {
    return this.positionForm.get('experienceMax');
  }

  public onFileSelected(event: Event): void {
    this.fileError = null;
    const result = FileHelper.handleFileSelection(event);

    if (result.error) {
      this.fileError = result.error;
      this.selectedFile = undefined;
      return;
    }

    this.selectedFile = result.file || undefined;
  }

  public getAllowedExtensions(): string {
    return FileHelper.getAcceptAttribute();
  }

  public getAllowedTypesString(): string {
    return FileHelper.getAllowedTypesDescription();
  }

  public getMaxFileSizeString(): string {
    return FileHelper.getMaxFileSizeDescription();
  }

  public formatFileSize(bytes: number): string {
    return FileHelper.formatFileSize(bytes);
  }
}
