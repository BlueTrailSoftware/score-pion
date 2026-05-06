import { Component, OnInit, OnDestroy, inject, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, NavigationStart, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { PositionService } from '../../services/position.service';
import { AssessmentsService } from '../../services/assessments.service';
import {
  Position,
  PositionAssessment,
  UpdatePositionRequest,
  UpdatePositionStatusRequest,
} from '../../models/position.model';
import {
  addSkillToList,
  applyWorkModeChange,
  isExperienceRangeInvalid,
  JOB_TYPES,
  markFormGroupTouched,
  setLocationControlState,
  WORK_MODES,
} from '../position-form.utils';
import { Assessment } from '../../models/assessment.model';
import { Subject } from 'rxjs';
import { filter, finalize, takeUntil } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { ApiResponse } from '../../models/responses/api-base-response.model';
import { Location, NgClass, DatePipe } from '@angular/common';
import { FileHelper } from '../../helpers/fileHelper';
import { RequestAssessmentModalComponent } from '../positions-manage/request-assessment-modal/request-assessment-modal.component';

@Component({
  selector: 'app-position-detail',
  templateUrl: './position-detail.component.html',
  styleUrls: ['./position-detail.component.scss'],
  imports: [NgClass, ReactiveFormsModule, FormsModule, DatePipe, RequestAssessmentModalComponent],
})
export class PositionDetailComponent implements OnInit, OnDestroy {
  private positionService = inject(PositionService);
  private assessmentsService = inject(AssessmentsService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private location = inject(Location);

  public position: Position | null = null;
  public isLoading = false;
  public error: string | null = null;
  public saving = false;
  public isEditing = false;
  public isAdmin = false;
  public availableAssessments: Assessment[] = [];
  public filteredAssessments: Assessment[] = [];
  public searchTerm: string = '';
  public selectedAssessmentIds: Set<string> = new Set();
  public loadingAssessments = false;
  public showAvailableAssessments = false;
  public positionForm: FormGroup;
  public selectedFile: File | undefined;
  public fileError: string | null = null;
  public fileToDelete = false;

  readonly JOB_TYPES = JOB_TYPES;
  readonly WORK_MODES = WORK_MODES;

  public skills: string[] = [];
  public skillInput: string = '';

  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;

  public showRequestAssessmentModal: boolean = false;

  private previousUrl: string = '';
  private destroy$ = new Subject<void>();
  private originalPosition: Position | null = null;
  private autoEditMode = false;

  constructor() {
    this.positionForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      isExternal: [false],
      isActive: [true],
      jobType: [''],
      workMode: ['', [Validators.required]],
      experienceMin: [null, [Validators.min(0), Validators.max(20)]],
      experienceMax: [null, [Validators.min(0), Validators.max(20)]],
      location: ['', [Validators.required, Validators.maxLength(200)]],
    });
    this.router.events.pipe(filter(event => event instanceof NavigationStart)).subscribe((event: NavigationStart) => {
      this.previousUrl = event.url;
    });
  }

  ngOnInit(): void {
    this.checkAdminRole();
    this.autoEditMode = this.route.snapshot.queryParamMap.get('edit') === 'true';
    this.loadPositionDetails();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private checkAdminRole(): void {
    this.isAdmin = this.authService.isAdmin();
  }

  private loadPositionDetails(): void {
    const positionId = this.route.snapshot.paramMap.get('id');

    if (!positionId) {
      this.error = 'Position ID not found';
      return;
    }

    this.isLoading = true;
    this.error = null;

    this.positionService
      .getPositionById(positionId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: ApiResponse<Position>) => {
          this.position = response.data ? response.data : null;
          this.originalPosition = this.position ? { ...this.position } : null;
          this.isLoading = false;
          this.initializeForm();
          this.initializeSelectedAssessments();
          this.resetFileState();

          if (this.autoEditMode && this.isAdmin) {
            this.autoEditMode = false;
            this.enterEditMode();
          }
        },
        error: error => {
          this.error = 'Error loading position details';
          this.isLoading = false;
          console.error('Error loading position:', error);
        },
      });
  }

  private initializeForm(): void {
    if (this.position) {
      this.positionForm.patchValue({
        title: this.position.title,
        description: this.position.description,
        isExternal: this.position.external,
        isActive: this.position.isActive,
        jobType: this.position.jobType || '',
        workMode: this.position.workMode || '',
        experienceMin: this.position.experienceMin ?? null,
        experienceMax: this.position.experienceMax ?? null,
        location: this.position.location || '',
      });
      this.skills = this.position.skills ? [...this.position.skills] : [];

      setLocationControlState(this.positionForm, this.position.workMode || '');
    }
  }

  private initializeSelectedAssessments(): void {
    if (this.position?.assessments) {
      this.selectedAssessmentIds = new Set(
        this.position.assessments.map((assessment: PositionAssessment) => assessment.assessmentId),
      );
    }
  }

  public enterEditMode(): void {
    this.isEditing = true;
    this.loadAvailableAssessments();
  }

  public cancelEdit(): void {
    this.isEditing = false;
    this.showAvailableAssessments = false;
    this.searchTerm = '';
    this.filteredAssessments = [];
    this.selectedFile = undefined;
    this.fileError = null;
    this.fileToDelete = false;
    this.skillInput = '';

    if (this.originalPosition) {
      this.position = { ...this.originalPosition };
      this.initializeForm();
      this.initializeSelectedAssessments();
    }
    this.resetFileState();
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

  public onWorkModeChange(value: string): void {
    applyWorkModeChange(this.positionForm, value);
  }

  public saveChanges(): void {
    if (this.positionForm.invalid || !this.position || this.isExperienceRangeInvalid()) {
      markFormGroupTouched(this.positionForm);
      return;
    }

    if (this.selectedFile) {
      const validation = FileHelper.validateFile(this.selectedFile);
      if (!validation.valid) {
        this.fileError = validation.error || 'Invalid file';
        return;
      }
    }

    this.saving = true;

    const formValue = this.positionForm.getRawValue();
    const updatePayload: UpdatePositionRequest = {
      title: formValue.title,
      description: formValue.description,
      external: formValue.isExternal,
      assessmentIds: Array.from(this.selectedAssessmentIds),
      deleteFile: this.fileToDelete,
      location: formValue.location,
      workMode: formValue.workMode,
    };

    if (formValue.jobType) {
      updatePayload.jobType = formValue.jobType;
    }
    if (formValue.experienceMin != null) {
      updatePayload.experienceMin = formValue.experienceMin;
    }
    if (formValue.experienceMax != null) {
      updatePayload.experienceMax = formValue.experienceMax;
    }
    if (this.skills.length > 0) {
      updatePayload.skills = [...this.skills];
    }

    this.positionService
      .updatePosition(this.position.id, updatePayload, this.selectedFile)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.saving = false;
        }),
      )
      .subscribe({
        next: () => {
          this.originalPosition = this.position ? { ...this.position } : null;

          this.isEditing = false;
          this.showAvailableAssessments = false;
          this.resetFileState();

          this.loadPositionDetails();
        },
        error: () => {
          this.error = 'Error updating position';
        },
      });
  }

  public toggleAvailableAssessments(): void {
    this.showAvailableAssessments = !this.showAvailableAssessments;
    if (this.showAvailableAssessments && this.availableAssessments.length === 0) {
      this.loadAvailableAssessments();
    }
  }

  private loadAvailableAssessments(): void {
    this.loadingAssessments = true;

    this.assessmentsService
      .getAssessments()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: ApiResponse<Assessment[]>) => {
          this.availableAssessments = response.data || [];
          this.filteredAssessments = this.availableAssessments;
          this.loadingAssessments = false;
        },
        error: (error: unknown) => {
          console.error('Error loading assessments:', error);
          this.availableAssessments = [];
          this.filteredAssessments = [];
          this.loadingAssessments = false;
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

  public clearSearch(): void {
    this.searchTerm = '';
    this.filteredAssessments = this.availableAssessments;
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

  public getCurrentAssessmentsForDisplay() {
    if (!this.isEditing) {
      return this.position?.assessments || [];
    } else {
      return Array.from(this.selectedAssessmentIds).map(assessmentId => {
        const originalAssessment = this.position?.assessments?.find(a => a.assessmentId === assessmentId);
        const availableAssessment = this.availableAssessments.find(a => a.testID === assessmentId);

        return {
          assessmentId: assessmentId,
          assessmentName: originalAssessment?.assessmentName || availableAssessment?.displayName || assessmentId,
        };
      });
    }
  }

  public onRequestNewExam(): void {
    this.showRequestAssessmentModal = true;
  }

  public closeRequestAssessmentModal(): void {
    this.showRequestAssessmentModal = false;
  }

  public updatePositionStatus(): void {
    if (!this.position || !this.isAdmin) return;

    this.saving = true;
    this.error = null;

    const request: UpdatePositionStatusRequest = { isActive: !this.position.isActive };

    this.positionService
      .updatePositionState(this.position.id, request)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.saving = false;
        }),
      )
      .subscribe({
        next: () => {
          this.isEditing = false;
          this.loadPositionDetails();
        },
        error: error => {
          this.error = 'Error updating position: ' + error.message;
        },
      });
  }

  public goBack(): void {
    if (window.history.length > 1) {
      this.location.back();
    } else if (this.previousUrl && this.previousUrl !== this.router.url) {
      this.router.navigateByUrl(this.previousUrl);
    } else {
      this.router.navigate(['/positions-manage']);
    }
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
  public get locationControl() {
    return this.positionForm.get('location');
  }

  public downloadFile(): void {
    if (this.position?.fileUrl) {
      window.open(this.position.fileUrl, '_blank');
    }
  }

  public getFileName(): string {
    if (this.position?.fileName) {
      return this.position.fileName;
    }

    if (this.position?.fileUrl) {
      try {
        const url = new URL(this.position.fileUrl);
        const pathParts = url.pathname.split('/');
        const fileName = pathParts[pathParts.length - 1];
        return decodeURIComponent(fileName) || 'Attached File';
      } catch {
        return 'Attached File';
      }
    }

    return 'Attached File';
  }

  public getFileIcon(): string {
    const fileName = this.getFileName().toLowerCase();

    if (fileName.endsWith('.pdf')) return 'bi-file-earmark-pdf';
    if (fileName.endsWith('.doc') || fileName.endsWith('.docx')) return 'bi-file-earmark-word';
    if (fileName.endsWith('.jpg') || fileName.endsWith('.jpeg') || fileName.endsWith('.png'))
      return 'bi-file-earmark-image';

    return 'bi-file-earmark';
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
    if (this.selectedFile) {
      this.fileToDelete = false;
    }
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

  public removeCurrentFile(): void {
    this.fileToDelete = true;
    this.selectedFile = undefined;
  }

  public cancelFileRemoval(): void {
    this.fileToDelete = false;
  }

  public hasFileAttachment(): boolean {
    return !!(this.position?.fileUrl && !this.fileToDelete && !this.selectedFile);
  }

  public hasNewFileSelected(): boolean {
    return !!this.selectedFile;
  }

  private resetFileState(): void {
    this.selectedFile = undefined;
    this.fileError = null;
    this.fileToDelete = false;
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }
}
