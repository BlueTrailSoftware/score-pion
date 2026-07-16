import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { PositionCreateComponent } from '../../../app/positions/position-create/position-create.component';
import { PositionService } from '../../../app/services/position.service';
import { AssessmentsService } from '../../../app/services/assessments.service';
import { Assessment } from '../../../app/models/assessment.model';
import { ApiResponse } from '../../../app/models/responses/api-base-response.model';
import { CreatePositionRequest } from '../../../app/models/position.model';

const mockAssessments: Assessment[] = [
  { displayName: 'JavaScript Test', testID: 'js-001' },
  { displayName: 'TypeScript Test', testID: 'ts-002' },
];

const mockAssessmentResponse: ApiResponse<Assessment[]> = {
  status: 'success',
  message: 'OK',
  data: mockAssessments,
};

describe('PositionCreateComponent', () => {
  let component: PositionCreateComponent;
  let fixture: ComponentFixture<PositionCreateComponent>;
  let positionServiceSpy: jasmine.SpyObj<PositionService>;
  let assessmentsServiceSpy: jasmine.SpyObj<AssessmentsService>;
  let router: Router;

  beforeEach(async () => {
    positionServiceSpy = jasmine.createSpyObj('PositionService', ['createPosition']);
    assessmentsServiceSpy = jasmine.createSpyObj('AssessmentsService', ['getAssessments']);

    assessmentsServiceSpy.getAssessments.and.returnValue(of(mockAssessmentResponse));

    await TestBed.configureTestingModule({
      imports: [PositionCreateComponent],
      providers: [
        provideRouter([]),
        { provide: PositionService, useValue: positionServiceSpy },
        { provide: AssessmentsService, useValue: assessmentsServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PositionCreateComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load assessments on init', () => {
    expect(assessmentsServiceSpy.getAssessments).toHaveBeenCalled();
    expect(component.availableAssessments).toEqual(mockAssessments);
    expect(component.filteredAssessments).toEqual(mockAssessments);
  });

  it('should handle assessment load error', fakeAsync(() => {
    spyOn(console, 'error');
    assessmentsServiceSpy.getAssessments.and.returnValue(throwError(() => new Error('fail')));

    // Re-create to trigger ngOnInit with error
    fixture = TestBed.createComponent(PositionCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    tick();

    expect(component.availableAssessments).toEqual([]);
    expect(component.error).toBe('Failed to load assessments');
  }));

  describe('form validation', () => {
    it('should be invalid when empty', () => {
      expect(component.positionForm.invalid).toBeTrue();
    });

    it('should be valid with proper values', () => {
      component.positionForm.setValue({
        title: 'Frontend Dev',
        description: 'A detailed description for the position',
        isExternal: false,
        jobType: '',
        workMode: 'Onsite',
        experienceMin: null,
        experienceMax: null,
        location: 'New York, NY',
      });
      expect(component.positionForm.valid).toBeTrue();
    });

    it('should be invalid when workMode is empty', () => {
      component.positionForm.setValue({
        title: 'Frontend Dev',
        description: 'A detailed description for the position',
        isExternal: false,
        jobType: '',
        workMode: '',
        experienceMin: null,
        experienceMax: null,
        location: 'New York, NY',
      });
      expect(component.positionForm.invalid).toBeTrue();
      expect(component.positionForm.get('workMode')?.hasError('required')).toBeTrue();
    });

    it('should be invalid when title is too short', () => {
      component.positionForm.patchValue({ title: 'ab' });
      expect(component.title?.hasError('minlength')).toBeTrue();
    });

    it('should be invalid when description is too short', () => {
      component.positionForm.patchValue({ description: 'short' });
      expect(component.description?.hasError('minlength')).toBeTrue();
    });
  });

  describe('searchAssessments', () => {
    it('should filter assessments by displayName', () => {
      component.searchAssessments('java');
      expect(component.filteredAssessments.length).toBe(1);
      expect(component.filteredAssessments[0].testID).toBe('js-001');
    });

    it('should filter assessments by testID', () => {
      component.searchAssessments('ts-002');
      expect(component.filteredAssessments.length).toBe(1);
      expect(component.filteredAssessments[0].testID).toBe('ts-002');
    });

    it('should return all assessments when search is empty', () => {
      component.searchAssessments('java');
      component.searchAssessments('');
      expect(component.filteredAssessments).toEqual(mockAssessments);
    });
  });

  describe('toggleAvailableAssessments', () => {
    it('should toggle showAvailableAssessments', () => {
      expect(component.showAvailableAssessments).toBeFalse();
      component.toggleAvailableAssessments();
      expect(component.showAvailableAssessments).toBeTrue();
      component.toggleAvailableAssessments();
      expect(component.showAvailableAssessments).toBeFalse();
    });
  });

  describe('assessment selection', () => {
    it('onAssessmentToggle should add when checked', () => {
      const event = { target: { checked: true } } as unknown as Event;
      component.onAssessmentToggle('js-001', event);
      expect(component.isAssessmentSelected('js-001')).toBeTrue();
    });

    it('onAssessmentToggle should remove when unchecked', () => {
      component.selectedAssessmentIds.add('js-001');
      const event = { target: { checked: false } } as unknown as Event;
      component.onAssessmentToggle('js-001', event);
      expect(component.isAssessmentSelected('js-001')).toBeFalse();
    });

    it('removeAssessment should remove from set', () => {
      component.selectedAssessmentIds.add('js-001');
      component.removeAssessment('js-001');
      expect(component.isAssessmentSelected('js-001')).toBeFalse();
    });

    it('getSelectedAssessments should return mapped array', () => {
      component.selectedAssessmentIds.add('js-001');
      component.selectedAssessmentIds.add('ts-002');
      const selected = component.getSelectedAssessments();
      expect(selected.length).toBe(2);
      expect(selected.find(a => a.testID === 'js-001')?.displayName).toBe('JavaScript Test');
    });

    it('getSelectedAssessments should use testID as fallback displayName', () => {
      component.selectedAssessmentIds.add('unknown-id');
      const selected = component.getSelectedAssessments();
      expect(selected[0].displayName).toBe('unknown-id');
    });
  });

  describe('createPosition', () => {
    beforeEach(() => {
      component.positionForm.setValue({
        title: 'Frontend Dev',
        description: 'A detailed description for the position',
        isExternal: false,
        jobType: '',
        workMode: 'Onsite',
        experienceMin: null,
        experienceMax: null,
        location: 'New York, NY',
      });
    });

    it('should not call service when form is invalid', () => {
      component.positionForm.patchValue({ title: '' });
      component.createPosition();
      expect(positionServiceSpy.createPosition).not.toHaveBeenCalled();
    });

    it('should mark all fields as touched when form is invalid', () => {
      component.positionForm.patchValue({ title: '' });
      component.createPosition();
      expect(component.title?.touched).toBeTrue();
      expect(component.description?.touched).toBeTrue();
    });

    it('should call positionService.createPosition and navigate on success', fakeAsync(() => {
      positionServiceSpy.createPosition.and.returnValue(of({ status: 'success', message: 'Created' }));
      spyOn(router, 'navigate');

      component.createPosition();
      tick();

      expect(positionServiceSpy.createPosition).toHaveBeenCalled();
      expect(router.navigate).toHaveBeenCalledWith(['/positions-manage']);
      expect(component.saving).toBeFalse();
    }));

    it('should set error on service failure', fakeAsync(() => {
      spyOn(console, 'error');
      positionServiceSpy.createPosition.and.returnValue(throwError(() => ({ message: 'Server error' })));

      component.createPosition();
      tick();

      expect(component.error).toBe('Server error');
      expect(component.saving).toBeFalse();
    }));
  });

  describe('dropdown options', () => {
    it('should have correct JOB_TYPES options', () => {
      expect(component.JOB_TYPES).toEqual(['Full Time', 'Part Time', 'Contract', 'Internship']);
    });

    it('should have correct WORK_MODES options', () => {
      expect(component.WORK_MODES).toEqual(['Onsite', 'Remote', 'Hybrid']);
    });
  });

  describe('form validity with optional fields', () => {
    it('should be valid when only required fields are filled', () => {
      component.positionForm.setValue({
        title: 'Frontend Dev',
        description: 'A detailed description for the position',
        isExternal: false,
        jobType: '',
        workMode: 'Onsite',
        experienceMin: null,
        experienceMax: null,
        location: 'New York, NY',
      });
      expect(component.positionForm.valid).toBeTrue();
    });

    it('should be valid with all optional fields filled', () => {
      component.positionForm.setValue({
        title: 'Frontend Dev',
        description: 'A detailed description for the position',
        isExternal: false,
        jobType: 'Full Time',
        workMode: 'Remote',
        experienceMin: 2,
        experienceMax: 5,
        location: 'New York, NY',
      });
      expect(component.positionForm.valid).toBeTrue();
    });

    it('should be invalid when location is missing', () => {
      component.positionForm.setValue({
        title: 'Frontend Dev',
        description: 'A detailed description for the position',
        isExternal: false,
        jobType: '',
        workMode: 'Onsite',
        experienceMin: null,
        experienceMax: null,
        location: '',
      });
      expect(component.positionForm.invalid).toBeTrue();
      expect(component.location?.hasError('required')).toBeTrue();
    });
  });

  describe('experience range validation', () => {
    it('should return true when min > max', () => {
      component.positionForm.patchValue({ experienceMin: 10, experienceMax: 5 });
      expect(component.isExperienceRangeInvalid()).toBeTrue();
    });

    it('should return false when min <= max', () => {
      component.positionForm.patchValue({ experienceMin: 3, experienceMax: 7 });
      expect(component.isExperienceRangeInvalid()).toBeFalse();
    });

    it('should return false when only min is set', () => {
      component.positionForm.patchValue({ experienceMin: 5, experienceMax: null });
      expect(component.isExperienceRangeInvalid()).toBeFalse();
    });

    it('should return false when neither is set', () => {
      component.positionForm.patchValue({ experienceMin: null, experienceMax: null });
      expect(component.isExperienceRangeInvalid()).toBeFalse();
    });
  });

  describe('location validation', () => {
    it('should require location', () => {
      component.positionForm.patchValue({ location: '' });
      expect(component.location?.hasError('required')).toBeTrue();
    });

    it('should enforce maxLength of 200', () => {
      component.positionForm.patchValue({ location: 'a'.repeat(201) });
      expect(component.location?.hasError('maxlength')).toBeTrue();
    });

    it('should accept location at exactly 200 characters', () => {
      component.positionForm.patchValue({ location: 'a'.repeat(200) });
      expect(component.location?.hasError('maxlength')).toBeFalsy();
    });
  });

  describe('skills management', () => {
    it('addSkill should add a trimmed skill', () => {
      component.addSkill('  Angular  ');
      expect(component.skills).toEqual(['Angular']);
    });

    it('addSkill should reject empty strings', () => {
      component.addSkill('');
      expect(component.skills.length).toBe(0);
    });

    it('addSkill should reject whitespace-only strings', () => {
      component.addSkill('   ');
      expect(component.skills.length).toBe(0);
    });

    it('addSkill should prevent duplicates (case-insensitive)', () => {
      component.addSkill('Angular');
      component.addSkill('angular');
      component.addSkill('ANGULAR');
      expect(component.skills).toEqual(['Angular']);
    });

    it('removeSkill should remove skill at given index', () => {
      component.skills = ['Angular', 'React', 'Vue'];
      component.removeSkill(1);
      expect(component.skills).toEqual(['Angular', 'Vue']);
    });

    it('onSkillKeydown should add skill on Enter', () => {
      component.skillInput = 'TypeScript';
      const event = new KeyboardEvent('keydown', { key: 'Enter' });
      spyOn(event, 'preventDefault');
      component.onSkillKeydown(event);
      expect(event.preventDefault).toHaveBeenCalled();
      expect(component.skills).toContain('TypeScript');
    });

    it('onSkillKeydown should add skill on comma', () => {
      component.skillInput = 'React';
      const event = new KeyboardEvent('keydown', { key: ',' });
      spyOn(event, 'preventDefault');
      component.onSkillKeydown(event);
      expect(event.preventDefault).toHaveBeenCalled();
      expect(component.skills).toContain('React');
    });
  });

  describe('payload construction with new fields', () => {
    beforeEach(() => {
      component.positionForm.setValue({
        title: 'Frontend Dev',
        description: 'A detailed description for the position',
        isExternal: false,
        jobType: 'Full Time',
        workMode: 'Remote',
        experienceMin: 2,
        experienceMax: 5,
        location: 'New York, NY',
      });
      component.skills = ['Angular', 'TypeScript'];
    });

    it('should include all new fields in the payload', fakeAsync(() => {
      positionServiceSpy.createPosition.and.returnValue(of({ status: 'success', message: 'Created' }));
      spyOn(router, 'navigate');

      component.createPosition();
      tick();

      const payload = positionServiceSpy.createPosition.calls.mostRecent().args[0] as CreatePositionRequest;
      expect(payload.jobType).toBe('Full Time');
      expect(payload.workMode).toBe('Remote');
      expect(payload.experienceMin).toBe(2);
      expect(payload.experienceMax).toBe(5);
      expect(payload.location).toBe('New York, NY');
      expect(payload.skills).toEqual(['Angular', 'TypeScript']);
    }));

    it('should omit optional fields when not provided but always include workMode', fakeAsync(() => {
      component.positionForm.patchValue({
        jobType: '',
        workMode: 'Hybrid',
        experienceMin: null,
        experienceMax: null,
      });
      component.skills = [];
      positionServiceSpy.createPosition.and.returnValue(of({ status: 'success', message: 'Created' }));
      spyOn(router, 'navigate');

      component.createPosition();
      tick();

      const payload = positionServiceSpy.createPosition.calls.mostRecent().args[0] as CreatePositionRequest;
      expect(payload.jobType).toBeUndefined();
      expect(payload.workMode).toBe('Hybrid');
      expect(payload.experienceMin).toBeUndefined();
      expect(payload.experienceMax).toBeUndefined();
      expect(payload.skills).toBeUndefined();
      expect(payload.location).toBe('New York, NY');
    }));

    it('should not submit when experience range is invalid', () => {
      component.positionForm.patchValue({ experienceMin: 10, experienceMax: 3 });
      component.createPosition();
      expect(positionServiceSpy.createPosition).not.toHaveBeenCalled();
    });
  });

  describe('work mode required validation', () => {
    it('should be invalid when workMode is empty', () => {
      component.positionForm.patchValue({ workMode: '' });
      expect(component.positionForm.get('workMode')?.hasError('required')).toBeTrue();
    });

    it('should be valid when workMode is set to a valid option', () => {
      component.positionForm.setValue({
        title: 'Frontend Dev',
        description: 'A detailed description for the position',
        isExternal: false,
        jobType: '',
        workMode: 'Hybrid',
        experienceMin: null,
        experienceMax: null,
        location: 'New York, NY',
      });
      expect(component.positionForm.get('workMode')?.valid).toBeTrue();
    });

    it('should prevent submission when workMode is empty', () => {
      component.positionForm.setValue({
        title: 'Frontend Dev',
        description: 'A detailed description for the position',
        isExternal: false,
        jobType: '',
        workMode: '',
        experienceMin: null,
        experienceMax: null,
        location: 'New York, NY',
      });
      component.createPosition();
      expect(positionServiceSpy.createPosition).not.toHaveBeenCalled();
    });
  });

  describe('Remote work mode and location interaction', () => {
    it('should not overwrite an empty location when work mode changes to Remote', () => {
      component.positionForm.get('workMode')?.setValue('Remote');
      expect(component.positionForm.get('location')?.value).toBe('');
      expect(component.positionForm.get('location')?.enabled).toBeTrue();
    });

    it('should preserve a user-entered location when work mode changes to Remote', () => {
      component.positionForm.get('location')?.setValue('New York, NY');
      component.positionForm.get('workMode')?.setValue('Remote');
      expect(component.positionForm.get('location')?.value).toBe('New York, NY');
      expect(component.positionForm.get('location')?.enabled).toBeTrue();
    });

    it('should keep location editable when switching between work modes', () => {
      component.positionForm.get('workMode')?.setValue('Remote');
      component.positionForm.get('workMode')?.setValue('Onsite');
      expect(component.positionForm.get('location')?.enabled).toBeTrue();

      component.positionForm.get('workMode')?.setValue('Hybrid');
      expect(component.positionForm.get('location')?.enabled).toBeTrue();
    });

    it('should include the user-entered location in payload when work mode is Remote', fakeAsync(() => {
      component.positionForm.setValue({
        title: 'Frontend Dev',
        description: 'A detailed description for the position',
        isExternal: false,
        jobType: '',
        workMode: 'Remote',
        experienceMin: null,
        experienceMax: null,
        location: 'New York, NY',
      });
      positionServiceSpy.createPosition.and.returnValue(of({ status: 'success', message: 'Created' }));
      spyOn(router, 'navigate');

      component.createPosition();
      tick();

      const payload = positionServiceSpy.createPosition.calls.mostRecent().args[0] as CreatePositionRequest;
      expect(payload.location).toBe('New York, NY');
      expect(payload.workMode).toBe('Remote');
    }));

    it('should always include workMode in payload', fakeAsync(() => {
      component.positionForm.setValue({
        title: 'Frontend Dev',
        description: 'A detailed description for the position',
        isExternal: false,
        jobType: '',
        workMode: 'Onsite',
        experienceMin: null,
        experienceMax: null,
        location: 'San Francisco, CA',
      });
      positionServiceSpy.createPosition.and.returnValue(of({ status: 'success', message: 'Created' }));
      spyOn(router, 'navigate');

      component.createPosition();
      tick();

      const payload = positionServiceSpy.createPosition.calls.mostRecent().args[0] as CreatePositionRequest;
      expect(payload.workMode).toBe('Onsite');
    }));
  });

  describe('goBack', () => {
    it('should navigate to /positions-manage', () => {
      spyOn(router, 'navigate');
      component.goBack();
      expect(router.navigate).toHaveBeenCalledWith(['/positions-manage']);
    });
  });

  describe('onFileSelected', () => {
    it('should set selectedFile for a valid file', () => {
      const file = new File(['content'], 'resume.pdf', { type: 'application/pdf' });
      const event = { target: { files: [file], value: 'resume.pdf' } } as unknown as Event;
      component.onFileSelected(event);
      expect(component.selectedFile).toBe(file);
      expect(component.fileError).toBeNull();
    });

    it('should set fileError for an invalid file', () => {
      const file = new File(['content'], 'script.exe', { type: 'application/x-msdownload' });
      const event = { target: { files: [file], value: 'script.exe' } } as unknown as Event;
      component.onFileSelected(event);
      expect(component.selectedFile).toBeUndefined();
      expect(component.fileError).toBeTruthy();
    });
  });

  describe('file helper delegates', () => {
    it('getAllowedExtensions should return extensions string', () => {
      expect(component.getAllowedExtensions()).toContain('.pdf');
    });

    it('getAllowedTypesString should return uppercase types', () => {
      expect(component.getAllowedTypesString()).toContain('PDF');
    });

    it('getMaxFileSizeString should return formatted size', () => {
      expect(component.getMaxFileSizeString()).toBe('10 MB');
    });

    it('formatFileSize should delegate to FileHelper', () => {
      expect(component.formatFileSize(1024)).toBe('1 KB');
    });
  });
});
