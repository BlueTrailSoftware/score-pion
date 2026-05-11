import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import * as fc from 'fast-check';
import { CareersDetailComponent } from '../../../app/public/careers/careers-detail.component';
import { PositionService } from '../../../app/services/position.service';
import { PublicPosition } from '../../../app/models/position.model';

const SAFE_CHARS = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
const safeString = (min: number, max: number): fc.Arbitrary<string> =>
  fc
    .array(fc.constantFrom(...SAFE_CHARS.split('')), { minLength: min, maxLength: max })
    .map((chars: string[]) => chars.join(''));

// Feature: career-detail-fields, Property 1: All skills rendered as tags
// Validates: Requirements 5.1
describe('CareersDetailComponent - Property-Based Tests', () => {
  let positionServiceSpy: jasmine.SpyObj<PositionService>;

  beforeEach(async () => {
    positionServiceSpy = jasmine.createSpyObj('PositionService', ['getPublicPositionById']);

    await TestBed.configureTestingModule({
      imports: [CareersDetailComponent],
      providers: [
        provideRouter([]),
        { provide: PositionService, useValue: positionServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => 'test-id' } } },
        },
      ],
    }).compileComponents();
  });

  it('Property 1: for any non-empty array of unique skills, every skill appears exactly once as a .skill-tag', () => {
    const skillsArb = fc
      .array(safeString(1, 20), { minLength: 1, maxLength: 15 })
      .filter(skills => new Set(skills).size === skills.length);

    fc.assert(
      fc.property(skillsArb, skills => {
        const position: PublicPosition = {
          id: 'prop-test',
          title: 'Test Position',
          description: 'Description',
          fileUrl: null,
          createdAt: new Date('2024-01-01'),
          skills,
        };

        positionServiceSpy.getPublicPositionById.and.returnValue(
          of({ status: 'success', message: 'OK', data: position }),
        );

        const fixture: ComponentFixture<CareersDetailComponent> = TestBed.createComponent(CareersDetailComponent);
        fixture.detectChanges();

        const el: HTMLElement = fixture.nativeElement;
        const skillTags = Array.from(el.querySelectorAll('.skill-tag'));

        expect(skillTags.length).toBe(skills.length);

        const renderedSkills = skillTags.map(tag => tag.textContent?.trim());
        for (const skill of skills) {
          const count = renderedSkills.filter(s => s === skill).length;
          expect(count).toBe(1);
        }

        fixture.destroy();
      }),
      { numRuns: 100 },
    );
  });
});
