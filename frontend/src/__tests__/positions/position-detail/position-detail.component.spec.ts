import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { of, throwError } from 'rxjs';
import { PositionDetailComponent } from '../../../app/positions/position-detail/position-detail.component';
import { PositionService } from '../../../app/services/position.service';
import { AssessmentsService } from '../../../app/services/assessments.service';
import { AuthService } from '../../../app/services/auth.service';
import { Position } from '../../../app/models/position.model';
import { Assessment } from '../../../app/models/assessment.model';
import { ApiResponse } from '../../../app/models/responses/api-base-response.model';

const mockPosition: Position = {
  id: 'p-1',
  title: 'Frontend Dev',
  description: 'Build great UIs',
  external: false,
  createdBy: 'admin-1',
  createdAt: new Date('2024-01-01'),
  isActive: true,
  assessments: [{ assessmentId: 'a-1', assessmentName: 'JS Test' }],
  fileUrl: 'http://example.com/files/resume.pdf',
  fileName: 'resume.pdf',
  location: 'New York, NY',
  workMode: 'Onsite',
};

const mockAssessments: Assessment[] = [
  { displayName: 'JS Test', testID: 'a-1' },
  { displayName: 'TS Test', testID: 'a-2' },
];

describe('PositionDetailComponent', () => {
  let component: PositionDetailComponent;
  let fixture: ComponentFixture<PositionDetailComponent>;
  let positionServiceSpy: jasmine.SpyObj<PositionService>;
  let assessmentsServiceSpy: jasmine.SpyObj<AssessmentsService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let location: Location;

  beforeEach(async () => {
    positionServiceSpy = jasmine.createSpyObj('PositionService', [
      'getPositionById',
      'updatePosition',
      'updatePositionState',
    ]);
    assessmentsServiceSpy = jasmine.createSpyObj('AssessmentsService', ['getAssessments']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isAdmin']);

    positionServiceSpy.getPositionById.and.returnValue(
      of({ status: 'success', message: 'OK', data: mockPosition } as ApiResponse<Position>),
    );
    assessmentsServiceSpy.getAssessments.and.returnValue(
      of({ status: 'success', message: 'OK', data: mockAssessments } as ApiResponse<Assessment[]>),
    );
    authServiceSpy.isAdmin.and.returnValue(true);

    await TestBed.configureTestingModule({
      imports: [PositionDetailComponent],
      providers: [
        provideRouter([]),
        { provide: PositionService, useValue: positionServiceSpy },
        { provide: AssessmentsService, useValue: assessmentsServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: { get: (key: string) => (key === 'id' ? 'p-1' : null) },
              queryParamMap: { get: (key: string) => (key === 'edit' ? 'false' : null) },
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PositionDetailComponent);
    component = fixture.componentInstance;
    location = TestBed.inject(Location);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load position on init', () => {
    expect(positionServiceSpy.getPositionById).toHaveBeenCalledWith('p-1');
    expect(component.position).toEqual(mockPosition);
    expect(component.isAdmin).toBeTrue();
  });

  it('should populate form from position data', () => {
    expect(component.positionForm.value.title).toBe('Frontend Dev');
    expect(component.positionForm.value.description).toBe('Build great UIs');
  });

  it('should initialize selected assessments from position', () => {
    expect(component.selectedAssessmentIds.has('a-1')).toBeTrue();
  });

  it('should set error when position ID is missing', () => {
    const route = TestBed.inject(ActivatedRoute);
    (route.snapshot.paramMap as any).get = () => null;

    // Re-create component
    fixture = TestBed.createComponent(PositionDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.error).toBe('Position ID not found');
  });

  it('should set error on load failure', fakeAsync(() => {
    spyOn(console, 'error');
    positionServiceSpy.getPositionById.and.returnValue(throwError(() => new Error('fail')));

    fixture = TestBed.createComponent(PositionDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    tick();

    expect(component.error).toBe('Error loading position details');
  }));

  describe('edit mode', () => {
    it('enterEditMode should set isEditing and load assessments', () => {
      component.enterEditMode();
      expect(component.isEditing).toBeTrue();
      expect(assessmentsServiceSpy.getAssessments).toHaveBeenCalled();
    });

    it('cancelEdit should restore original position and exit edit mode', () => {
      component.enterEditMode();
      component.positionForm.patchValue({ title: 'Changed' });
      component.cancelEdit();

      expect(component.isEditing).toBeFalse();
      expect(component.positionForm.value.title).toBe('Frontend Dev');
    });
  });

  describe('saveChanges', () => {
    it('should not save when form is invalid', () => {
      component.isEditing = true;
      component.positionForm.patchValue({ title: '' });
      component.saveChanges();
      expect(positionServiceSpy.updatePosition).not.toHaveBeenCalled();
    });

    it('should call updatePosition and reload on success', fakeAsync(() => {
      positionServiceSpy.updatePosition.and.returnValue(of({ status: 'success', message: 'OK' }));
      positionServiceSpy.getPositionById.calls.reset();

      component.isEditing = true;
      component.saveChanges();
      tick();

      expect(positionServiceSpy.updatePosition).toHaveBeenCalled();
      expect(positionServiceSpy.getPositionById).toHaveBeenCalled();
      expect(component.isEditing).toBeFalse();
    }));

    it('should set error on save failure', fakeAsync(() => {
      positionServiceSpy.updatePosition.and.returnValue(throwError(() => new Error('fail')));

      component.isEditing = true;
      component.saveChanges();
      tick();

      expect(component.error).toBe('Error updating position');
    }));
  });

  describe('assessment management', () => {
    beforeEach(() => {
      component.enterEditMode();
    });

    it('searchAssessments should filter by name', () => {
      component.searchAssessments('TS');
      expect(component.filteredAssessments.length).toBe(1);
      expect(component.filteredAssessments[0].testID).toBe('a-2');
    });

    it('searchAssessments should return all when empty', () => {
      component.searchAssessments('TS');
      component.searchAssessments('');
      expect(component.filteredAssessments.length).toBe(2);
    });

    it('clearSearch should reset search', () => {
      component.searchAssessments('TS');
      component.clearSearch();
      expect(component.searchTerm).toBe('');
      expect(component.filteredAssessments.length).toBe(2);
    });

    it('onAssessmentToggle should add/remove', () => {
      component.onAssessmentToggle('a-2', { target: { checked: true } } as any);
      expect(component.isAssessmentSelected('a-2')).toBeTrue();

      component.onAssessmentToggle('a-2', { target: { checked: false } } as any);
      expect(component.isAssessmentSelected('a-2')).toBeFalse();
    });

    it('removeAssessment should remove from set', () => {
      component.removeAssessment('a-1');
      expect(component.isAssessmentSelected('a-1')).toBeFalse();
    });

    it('toggleAvailableAssessments should toggle visibility', () => {
      component.showAvailableAssessments = false;
      component.toggleAvailableAssessments();
      expect(component.showAvailableAssessments).toBeTrue();
    });
  });

  describe('getCurrentAssessmentsForDisplay', () => {
    it('should return position assessments when not editing', () => {
      component.isEditing = false;
      const result = component.getCurrentAssessmentsForDisplay();
      expect(result.length).toBe(1);
    });

    it('should return mapped selected assessments when editing', () => {
      component.enterEditMode();
      component.selectedAssessmentIds.add('a-2');
      const result = component.getCurrentAssessmentsForDisplay();
      expect(result.length).toBe(2);
    });
  });

  describe('updatePositionStatus', () => {
    it('should call updatePositionState and reload', fakeAsync(() => {
      positionServiceSpy.updatePositionState.and.returnValue(of({ status: 'success', message: 'OK' }));
      positionServiceSpy.getPositionById.calls.reset();

      component.updatePositionStatus();
      tick();

      expect(positionServiceSpy.updatePositionState).toHaveBeenCalledWith('p-1', { isActive: false });
      expect(positionServiceSpy.getPositionById).toHaveBeenCalled();
    }));

    it('should set error on failure', fakeAsync(() => {
      positionServiceSpy.updatePositionState.and.returnValue(throwError(() => ({ message: 'Server error' })));

      component.updatePositionStatus();
      tick();

      expect(component.error).toContain('Error updating position');
    }));

    it('should do nothing when position is null', () => {
      component.position = null;
      component.updatePositionStatus();
      expect(positionServiceSpy.updatePositionState).not.toHaveBeenCalled();
    });

    it('should do nothing when not admin', () => {
      component.isAdmin = false;
      component.updatePositionStatus();
      expect(positionServiceSpy.updatePositionState).not.toHaveBeenCalled();
    });
  });

  describe('goBack', () => {
    it('should call location.back when history exists', () => {
      spyOn(location, 'back');
      spyOnProperty(window.history, 'length').and.returnValue(3);
      component.goBack();
      expect(location.back).toHaveBeenCalled();
    });
  });

  describe('onRequestNewExam', () => {
    it('should open the request assessment modal', () => {
      expect(component.showRequestAssessmentModal).toBeFalse();
      component.onRequestNewExam();
      expect(component.showRequestAssessmentModal).toBeTrue();
    });

    it('should close the request assessment modal', () => {
      component.showRequestAssessmentModal = true;
      component.closeRequestAssessmentModal();
      expect(component.showRequestAssessmentModal).toBeFalse();
    });
  });

  describe('file management', () => {
    it('getFileName should return fileName from position', () => {
      expect(component.getFileName()).toBe('resume.pdf');
    });

    it('getFileName should extract from URL when no fileName', () => {
      component.position = { ...mockPosition, fileName: undefined };
      expect(component.getFileName()).toBe('resume.pdf');
    });

    it('getFileName should return fallback when no file info', () => {
      component.position = { ...mockPosition, fileName: undefined, fileUrl: undefined };
      expect(component.getFileName()).toBe('Attached File');
    });

    it('getFileIcon should return pdf icon for .pdf', () => {
      expect(component.getFileIcon()).toBe('bi-file-earmark-pdf');
    });

    it('getFileIcon should return word icon for .docx', () => {
      component.position = { ...mockPosition, fileName: 'doc.docx' };
      expect(component.getFileIcon()).toBe('bi-file-earmark-word');
    });

    it('getFileIcon should return generic icon for unknown', () => {
      component.position = { ...mockPosition, fileName: 'file.xyz' };
      expect(component.getFileIcon()).toBe('bi-file-earmark');
    });

    it('onFileSelected should set selectedFile for valid file', () => {
      const file = new File(['content'], 'resume.pdf', { type: 'application/pdf' });
      component.onFileSelected({ target: { files: [file], value: 'resume.pdf' } } as any);
      expect(component.selectedFile).toBe(file);
      expect(component.fileToDelete).toBeFalse();
    });

    it('onFileSelected should set fileError for invalid file', () => {
      const file = new File([new Array(11 * 1024 * 1024).fill('a').join('')], 'large.pdf', { type: 'application/pdf' });
      component.onFileSelected({ target: { files: [file], value: 'large.pdf' } } as any);
      expect(component.fileError).toContain('too large');
      expect(component.selectedFile).toBeUndefined();
    });

    it('removeCurrentFile should set fileToDelete', () => {
      component.removeCurrentFile();
      expect(component.fileToDelete).toBeTrue();
      expect(component.selectedFile).toBeUndefined();
    });

    it('cancelFileRemoval should reset fileToDelete', () => {
      component.removeCurrentFile();
      component.cancelFileRemoval();
      expect(component.fileToDelete).toBeFalse();
    });

    it('hasFileAttachment should return true when position has file and not deleting', () => {
      expect(component.hasFileAttachment()).toBeTrue();
    });

    it('hasFileAttachment should return false when fileToDelete is true', () => {
      component.fileToDelete = true;
      expect(component.hasFileAttachment()).toBeFalse();
    });

    it('hasNewFileSelected should return true when file is selected', () => {
      component.selectedFile = new File([''], 'test.pdf', { type: 'application/pdf' });
      expect(component.hasNewFileSelected()).toBeTrue();
    });

    it('hasNewFileSelected should return false when no file selected', () => {
      expect(component.hasNewFileSelected()).toBeFalse();
    });
  });

  describe('file helper delegates', () => {
    it('getAllowedExtensions', () => {
      expect(component.getAllowedExtensions()).toContain('.pdf');
    });
    it('getAllowedTypesString', () => {
      expect(component.getAllowedTypesString()).toContain('PDF');
    });
    it('getMaxFileSizeString', () => {
      expect(component.getMaxFileSizeString()).toBe('10 MB');
    });
    it('formatFileSize', () => {
      expect(component.formatFileSize(1024)).toBe('1 KB');
    });
  });

  describe('downloadFile', () => {
    it('should open file URL in new tab', () => {
      spyOn(window, 'open');
      component.downloadFile();
      expect(window.open).toHaveBeenCalledWith('http://example.com/files/resume.pdf', '_blank');
    });

    it('should not open when no fileUrl', () => {
      component.position = { ...mockPosition, fileUrl: undefined };
      spyOn(window, 'open');
      component.downloadFile();
      expect(window.open).not.toHaveBeenCalled();
    });
  });
});
