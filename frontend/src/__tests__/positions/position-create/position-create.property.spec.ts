import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';
import * as fc from 'fast-check';
import { PositionCreateComponent } from '../../../app/positions/position-create/position-create.component';
import { PositionService } from '../../../app/services/position.service';
import { AssessmentsService } from '../../../app/services/assessments.service';
import { CreatePositionRequest, JobType, WorkMode } from '../../../app/models/position.model';

describe('PositionCreateComponent - Property-Based Tests', () => {
  let component: PositionCreateComponent;
  let fixture: ComponentFixture<PositionCreateComponent>;
  let positionServiceSpy: jasmine.SpyObj<PositionService>;
  let assessmentsServiceSpy: jasmine.SpyObj<AssessmentsService>;
  let router: Router;

  beforeEach(async () => {
    positionServiceSpy = jasmine.createSpyObj('PositionService', ['createPosition']);
    assessmentsServiceSpy = jasmine.createSpyObj('AssessmentsService', ['getAssessments']);
    assessmentsServiceSpy.getAssessments.and.returnValue(of({ status: 'success', message: 'OK', data: [] }));

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

  function fillRequiredFields(): void {
    component.positionForm.patchValue({
      title: 'Test Position',
      description: 'A valid description for testing',
      isExternal: false,
      location: 'Test City',
      workMode: 'Onsite',
    });
  }

  // Feature: position-form-enhancements, Property 1: Dropdown selection payload inclusion
  // Validates: Requirements 1.3, 2.3
  describe('Property 1: Dropdown selection payload inclusion', () => {
    it('for any valid jobType or workMode option, selecting it should include it in the payload', fakeAsync(() => {
      const jobTypes = [...component.JOB_TYPES];
      const workModes = [...component.WORK_MODES];
      spyOn(router, 'navigate').and.stub();

      fc.assert(
        fc.property(
          fc.constantFrom(...jobTypes),
          fc.constantFrom(...workModes),
          (jobType: JobType, workMode: WorkMode) => {
            positionServiceSpy.createPosition.calls.reset();
            positionServiceSpy.createPosition.and.returnValue(of({ status: 'success', message: 'Created' }));

            fillRequiredFields();
            component.positionForm.patchValue({ jobType, workMode });
            component.createPosition();
            tick();

            const payload = positionServiceSpy.createPosition.calls.mostRecent().args[0] as CreatePositionRequest;
            expect(payload.jobType).toBe(jobType);
            expect(payload.workMode).toBe(workMode);
          },
        ),
        { numRuns: 100 },
      );
    }));
  });

  // Feature: position-form-enhancements, Property 2: Experience range bounds validation
  // Validates: Requirements 3.2
  describe('Property 2: Experience range bounds validation', () => {
    it('for any number outside [0,20] or non-integer, the form control should be invalid', () => {
      const outOfBoundsArb = fc.oneof(fc.integer({ min: -1000, max: -1 }), fc.integer({ min: 21, max: 1000 }));

      fc.assert(
        fc.property(outOfBoundsArb, (value: number) => {
          component.positionForm.patchValue({ experienceMin: value });
          const minCtrl = component.positionForm.get('experienceMin');
          expect(minCtrl?.valid).toBeFalse();
        }),
        { numRuns: 100 },
      );

      fc.assert(
        fc.property(outOfBoundsArb, (value: number) => {
          component.positionForm.patchValue({ experienceMax: value });
          const maxCtrl = component.positionForm.get('experienceMax');
          expect(maxCtrl?.valid).toBeFalse();
        }),
        { numRuns: 100 },
      );
    });
  });

  // Feature: position-form-enhancements, Property 3: Experience min must not exceed max
  // Validates: Requirements 3.3
  describe('Property 3: Experience min must not exceed max', () => {
    it('for any pair where min > max in [0,20], isExperienceRangeInvalid should return true', () => {
      const pairArb = fc
        .tuple(fc.integer({ min: 0, max: 20 }), fc.integer({ min: 0, max: 20 }))
        .filter(([a, b]: [number, number]) => a > b);

      fc.assert(
        fc.property(pairArb, ([min, max]: [number, number]) => {
          component.positionForm.patchValue({ experienceMin: min, experienceMax: max });
          expect(component.isExperienceRangeInvalid()).toBeTrue();
        }),
        { numRuns: 100 },
      );
    });
  });

  // Feature: position-form-enhancements, Property 4: Valid experience range in payload
  // Validates: Requirements 3.4
  describe('Property 4: Valid experience range in payload', () => {
    it('for any pair where min <= max in [0,20], the payload should contain both values', fakeAsync(() => {
      const pairArb = fc
        .tuple(fc.integer({ min: 0, max: 20 }), fc.integer({ min: 0, max: 20 }))
        .filter(([a, b]: [number, number]) => a <= b);

      spyOn(router, 'navigate').and.stub();

      fc.assert(
        fc.property(pairArb, ([min, max]: [number, number]) => {
          positionServiceSpy.createPosition.calls.reset();
          positionServiceSpy.createPosition.and.returnValue(of({ status: 'success', message: 'Created' }));

          fillRequiredFields();
          component.positionForm.patchValue({ experienceMin: min, experienceMax: max });
          component.createPosition();
          tick();

          const payload = positionServiceSpy.createPosition.calls.mostRecent().args[0] as CreatePositionRequest;
          expect(payload.experienceMin).toBe(min);
          expect(payload.experienceMax).toBe(max);
        }),
        { numRuns: 100 },
      );
    }));
  });

  // Feature: position-form-enhancements, Property 5: Location max length validation
  // Validates: Requirements 4.2
  describe('Property 5: Location max length validation', () => {
    it('for any string longer than 200 characters, the location control should be invalid', () => {
      const longStringArb = fc.string({ minLength: 201, maxLength: 500 }).filter((s: string) => s.length > 200);

      fc.assert(
        fc.property(longStringArb, (location: string) => {
          component.positionForm.patchValue({ location });
          const ctrl = component.positionForm.get('location');
          expect(ctrl?.hasError('maxlength')).toBeTrue();
        }),
        { numRuns: 100 },
      );
    });
  });

  // Feature: position-form-enhancements, Property 6: Location in payload
  // Validates: Requirements 4.4
  describe('Property 6: Location in payload', () => {
    it('for any non-empty string of at most 200 chars, the payload should contain that location', fakeAsync(() => {
      const locationArb = fc.string({ minLength: 1, maxLength: 200 }).filter((s: string) => s.trim().length > 0);

      spyOn(router, 'navigate').and.stub();

      fc.assert(
        fc.property(locationArb, (location: string) => {
          positionServiceSpy.createPosition.calls.reset();
          positionServiceSpy.createPosition.and.returnValue(of({ status: 'success', message: 'Created' }));

          fillRequiredFields();
          component.positionForm.patchValue({ location });
          component.createPosition();
          tick();

          const payload = positionServiceSpy.createPosition.calls.mostRecent().args[0] as CreatePositionRequest;
          expect(payload.location).toBe(location);
        }),
        { numRuns: 100 },
      );
    }));
  });

  // Feature: position-form-enhancements, Property 7: Skill addition and whitespace rejection
  // Validates: Requirements 5.2, 5.5
  describe('Property 7: Skill addition and whitespace rejection', () => {
    it('for any string, addSkill adds it iff the trimmed string is non-empty', () => {
      fc.assert(
        fc.property(fc.string({ minLength: 0, maxLength: 50 }), (input: string) => {
          component.skills = [];
          component.addSkill(input);

          if (input.trim().length > 0) {
            expect(component.skills.length).toBe(1);
            expect(component.skills[0]).toBe(input.trim());
          } else {
            expect(component.skills.length).toBe(0);
          }
        }),
        { numRuns: 100 },
      );
    });
  });

  // Feature: position-form-enhancements, Property 8: Skill removal
  // Validates: Requirements 5.3
  describe('Property 8: Skill removal', () => {
    it('for any skills array and valid index, removeSkill reduces length by 1 and removes that element', () => {
      const skillsArb = fc.array(
        fc.string({ minLength: 1, maxLength: 20 }).filter((s: string) => s.trim().length > 0),
        {
          minLength: 1,
          maxLength: 10,
        },
      );

      fc.assert(
        fc.property(skillsArb, (skills: string[]) => {
          const uniqueSkills = [...new Set(skills.map(s => s.trim()))];
          if (uniqueSkills.length === 0) return;

          const index = Math.floor(Math.random() * uniqueSkills.length);
          const removedSkill = uniqueSkills[index];
          const originalLength = uniqueSkills.length;

          component.skills = [...uniqueSkills];
          component.removeSkill(index);

          expect(component.skills.length).toBe(originalLength - 1);
          // The removed element at that index should no longer be at that position
          if (index < component.skills.length) {
            expect(component.skills[index]).not.toBe(removedSkill);
          }
        }),
        { numRuns: 100 },
      );
    });
  });

  // Feature: position-form-enhancements, Property 9: Skill duplicate prevention (idempotence)
  // Validates: Requirements 5.4
  describe('Property 9: Skill duplicate prevention (idempotence)', () => {
    it('for any skill string, addSkill(s); addSkill(s) produces the same result as addSkill(s)', () => {
      const skillArb = fc.string({ minLength: 1, maxLength: 30 }).filter((s: string) => s.trim().length > 0);

      fc.assert(
        fc.property(skillArb, (skill: string) => {
          component.skills = [];
          component.addSkill(skill);
          const afterFirst = [...component.skills];

          component.addSkill(skill);
          expect(component.skills).toEqual(afterFirst);

          // Also test case-insensitive
          component.addSkill(skill.toUpperCase());
          expect(component.skills).toEqual(afterFirst);

          component.addSkill(skill.toLowerCase());
          expect(component.skills).toEqual(afterFirst);
        }),
        { numRuns: 100 },
      );
    });
  });

  // Feature: position-form-enhancements, Property 10: Skills list in payload
  // Validates: Requirements 5.6
  describe('Property 10: Skills list in payload', () => {
    it('for any non-empty skills array, the payload should contain that skills array', fakeAsync(() => {
      const skillsArb = fc.uniqueArray(
        fc
          .string({ minLength: 1, maxLength: 20 })
          .filter((s: string) => s.trim().length > 0)
          .map((s: string) => s.trim()),
        { minLength: 1, maxLength: 5, comparator: (a: string, b: string) => a.toLowerCase() === b.toLowerCase() },
      );

      spyOn(router, 'navigate').and.stub();

      fc.assert(
        fc.property(skillsArb, (skills: string[]) => {
          positionServiceSpy.createPosition.calls.reset();
          positionServiceSpy.createPosition.and.returnValue(of({ status: 'success', message: 'Created' }));

          fillRequiredFields();
          component.skills = [...skills];
          component.createPosition();
          tick();

          const payload = positionServiceSpy.createPosition.calls.mostRecent().args[0] as CreatePositionRequest;
          expect(payload.skills).toEqual(skills);
        }),
        { numRuns: 100 },
      );
    }));
  });
});

describe('PositionCreateComponent - Work Mode Property Tests', () => {
  let component: PositionCreateComponent;
  let fixture: ComponentFixture<PositionCreateComponent>;
  let positionServiceSpy: jasmine.SpyObj<PositionService>;
  let assessmentsServiceSpy: jasmine.SpyObj<AssessmentsService>;

  beforeEach(async () => {
    positionServiceSpy = jasmine.createSpyObj('PositionService', ['createPosition']);
    assessmentsServiceSpy = jasmine.createSpyObj('AssessmentsService', ['getAssessments']);
    assessmentsServiceSpy.getAssessments.and.returnValue(of({ status: 'success', message: 'OK', data: [] }));

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
    fixture.detectChanges();
  });

  // Feature: position-form-enhancements, Property 11: Remote work mode sets location to "Remote" and read-only
  // Validates: Requirements 2.4
  describe('Property 11: Remote work mode sets location to "Remote" and read-only', () => {
    it('for any form state, when work mode is changed to "Remote", location should be "Remote" and disabled', () => {
      const locationArb = fc.string({ minLength: 0, maxLength: 100 });

      fc.assert(
        fc.property(locationArb, (initialLocation: string) => {
          // Set up an arbitrary initial location value
          component.positionForm.get('location')?.enable();
          component.positionForm.patchValue({ location: initialLocation });

          // Trigger work mode change to Remote
          component.onWorkModeChange('Remote');

          const locationCtrl = component.positionForm.get('location');
          expect(locationCtrl?.value).toBe('Remote');
          expect(locationCtrl?.disabled).toBeTrue();
        }),
        { numRuns: 100 },
      );
    });
  });

  // Feature: position-form-enhancements, Property 12: Changing from Remote restores location editability
  // Validates: Requirements 2.5
  describe('Property 12: Changing from Remote restores location editability', () => {
    it('for any form state where work mode is "Remote", changing to another mode clears and enables location', () => {
      const nonRemoteModes = ['Onsite', 'Hybrid'] as const;
      const modeArb = fc.constantFrom(...nonRemoteModes);

      fc.assert(
        fc.property(modeArb, (newMode: string) => {
          // First set to Remote so location is "Remote" and disabled
          component.onWorkModeChange('Remote');
          expect(component.positionForm.get('location')?.value).toBe('Remote');
          expect(component.positionForm.get('location')?.disabled).toBeTrue();

          // Now change to a non-Remote mode
          component.onWorkModeChange(newMode);

          const locationCtrl = component.positionForm.get('location');
          expect(locationCtrl?.value).toBe('');
          expect(locationCtrl?.enabled).toBeTrue();
        }),
        { numRuns: 100 },
      );
    });
  });

  // Feature: position-form-enhancements, Property 13: Work mode required validation
  // Validates: Requirements 2.2
  describe('Property 13: Work mode required validation', () => {
    it('for any form state where work mode is empty, the form should be invalid', () => {
      const emptyArb = fc.constantFrom('', null as unknown as string);

      fc.assert(
        fc.property(emptyArb, (emptyValue: string) => {
          component.positionForm.patchValue({
            title: 'Valid Title',
            description: 'A valid description for testing',
            isExternal: false,
            location: 'Test City',
            workMode: emptyValue,
          });

          const workModeCtrl = component.positionForm.get('workMode');
          expect(workModeCtrl?.valid).toBeFalse();
          expect(component.positionForm.valid).toBeFalse();
        }),
        { numRuns: 100 },
      );
    });
  });
});
