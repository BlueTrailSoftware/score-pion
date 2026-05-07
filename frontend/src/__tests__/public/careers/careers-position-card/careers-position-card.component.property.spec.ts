import { ComponentFixture, TestBed } from '@angular/core/testing';
import * as fc from 'fast-check';
import { CareersPositionCardComponent } from '../../../../app/public/careers/careers-position-card/careers-position-card.component';
import { PublicPosition, JOB_TYPES, WORK_MODES } from '../../../../app/models/position.model';
import { formatExperienceRange } from '../../../../app/public/careers/careers-position-card/careers-position-card.utils';

// Use alphanumeric-based generators to avoid DOM text-content edge cases
const SAFE_CHARS = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
const safeString = (min: number, max: number): fc.Arbitrary<string> =>
  fc
    .array(fc.constantFrom(...SAFE_CHARS.split('')), { minLength: min, maxLength: max })
    .map((chars: string[]) => chars.join(''));

const safeUrl = fc.constantFrom(
  'https://example.com/file.pdf',
  'https://cdn.test.io/docs/resume.pdf',
  'https://storage.app.com/uploads/doc.pdf',
);

function publicPositionArb(
  overrides?: Partial<{
    fileUrl: fc.Arbitrary<string | null>;
    skills: fc.Arbitrary<string[] | undefined>;
  }>,
): fc.Arbitrary<PublicPosition> {
  return fc.record({
    id: fc.uuid(),
    title: safeString(1, 60),
    description: safeString(1, 200),
    fileUrl: overrides?.fileUrl ?? fc.option(safeUrl, { nil: null }),
    createdAt: fc
      .integer({ min: new Date('2020-01-01').getTime(), max: new Date('2025-12-31').getTime() })
      .map((ts: number) => new Date(ts)),
    jobType: fc.option(fc.constantFrom(...JOB_TYPES), { nil: undefined }),
    workMode: fc.option(fc.constantFrom(...WORK_MODES), { nil: undefined }),
    experienceMin: fc.option(fc.integer({ min: 0, max: 30 }), { nil: undefined }),
    experienceMax: fc.option(fc.integer({ min: 0, max: 30 }), { nil: undefined }),
    location: fc.option(safeString(1, 80), { nil: undefined }),
    skills:
      overrides?.skills ?? fc.option(fc.array(safeString(1, 20), { minLength: 0, maxLength: 12 }), { nil: undefined }),
  });
}

function createComponent(position: PublicPosition): ComponentFixture<CareersPositionCardComponent> {
  const fixture = TestBed.createComponent(CareersPositionCardComponent);
  fixture.componentInstance.position = position;
  fixture.detectChanges();
  return fixture;
}

describe('CareersPositionCardComponent - Property-Based Tests', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CareersPositionCardComponent],
    }).compileComponents();
  });

  // Feature: careers-position-card, Property 1: Card click emission
  // Validates: Requirements 1.3, 2.6
  describe('Property 1: Card click emission', () => {
    it('for any valid PublicPosition, click/Enter/Space on the card emits that exact position', () => {
      fc.assert(
        fc.property(publicPositionArb(), position => {
          const fixture = createComponent(position);
          const el: HTMLElement = fixture.nativeElement;
          const card = el.querySelector('.position-card') as HTMLElement;
          const emitted: PublicPosition[] = [];
          fixture.componentInstance.cardClick.subscribe((p: PublicPosition) => emitted.push(p));

          // click on card
          card.click();
          expect(emitted.length).toBe(1);
          expect(emitted[0]).toBe(position);

          // Enter key on card
          card.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
          expect(emitted.length).toBe(2);
          expect(emitted[1]).toBe(position);

          // Space key on card
          card.dispatchEvent(new KeyboardEvent('keydown', { key: ' ', bubbles: true }));
          expect(emitted.length).toBe(3);
          expect(emitted[2]).toBe(position);

          // View Details button click bubbles up to the card's (click) handler
          const viewBtn = el.querySelector('.view-details-btn') as HTMLElement;
          viewBtn.click();
          expect(emitted.length).toBe(4);
          expect(emitted[3]).toBe(position);

          fixture.destroy();
        }),
        { numRuns: 100 },
      );
    });
  });

  // Feature: careers-position-card, Property 2: Core content display
  // Validates: Requirements 2.1, 2.2, 2.3
  describe('Property 2: Core content display', () => {
    it('for any valid PublicPosition, the card displays title, description, and formatted date', () => {
      fc.assert(
        fc.property(publicPositionArb(), position => {
          const fixture = createComponent(position);
          const el: HTMLElement = fixture.nativeElement;

          const titleEl = el.querySelector('.position-title');
          expect(titleEl?.textContent?.trim()).toBe(position.title.trim());

          const descEl = el.querySelector('.position-description');
          expect(descEl?.textContent?.trim()).toBe(position.description.trim());

          const dateEl = el.querySelector('.position-date');
          const dateText = dateEl?.textContent ?? '';
          expect(dateText.trim().length).toBeGreaterThan(0);

          fixture.destroy();
        }),
        { numRuns: 100 },
      );
    });
  });

  // Feature: careers-position-card, Property 3: File link rendering
  // Validates: Requirements 2.4, 2.5
  describe('Property 3: File link rendering', () => {
    it('for any PublicPosition with a fileUrl, the card renders an active link; without, a disabled span', () => {
      fc.assert(
        fc.property(publicPositionArb(), position => {
          const fixture = createComponent(position);
          const el: HTMLElement = fixture.nativeElement;

          if (position.fileUrl) {
            const anchor = el.querySelector('a.file-icon-link.active') as HTMLAnchorElement;
            expect(anchor).toBeTruthy();
            // Use getAttribute to avoid browser URL normalization
            expect(anchor.getAttribute('href')).toBe(position.fileUrl);
            expect(anchor.target).toBe('_blank');
            expect(el.querySelector('span.file-icon-link.disabled')).toBeNull();
          } else {
            const disabledSpan = el.querySelector('span.file-icon-link.disabled');
            expect(disabledSpan).toBeTruthy();
            expect(el.querySelector('a.file-icon-link.active')).toBeNull();
          }

          fixture.destroy();
        }),
        { numRuns: 100 },
      );
    });
  });

  // Feature: careers-position-card, Property 4: Metadata fields display
  // Validates: Requirements 3.1, 3.2, 4.1, 4.2, 6.1, 6.2
  describe('Property 4: Metadata fields display', () => {
    it('for any PublicPosition, defined metadata fields appear in the card and undefined ones do not', () => {
      fc.assert(
        fc.property(publicPositionArb(), position => {
          const fixture = createComponent(position);
          const el: HTMLElement = fixture.nativeElement;
          const metadataItems = Array.from(el.querySelectorAll('.metadata-item'));
          const metadataText = metadataItems.map(item => item.textContent?.trim() ?? '');

          if (position.jobType) {
            expect(metadataText.some(t => t.includes(position.jobType!))).toBeTrue();
          }

          if (position.workMode) {
            expect(metadataText.some(t => t.includes(position.workMode!))).toBeTrue();
          }

          if (position.location) {
            expect(metadataText.some(t => t.includes(position.location!.trim()))).toBeTrue();
          }

          const expLabel = formatExperienceRange(position.experienceMin, position.experienceMax);
          if (expLabel) {
            expect(metadataText.some(t => t.includes(expLabel))).toBeTrue();
          }

          // If no metadata fields are defined, the metadata section should not exist
          if (!position.jobType && !position.workMode && !position.location && !expLabel) {
            expect(el.querySelector('.position-metadata')).toBeNull();
          }

          fixture.destroy();
        }),
        { numRuns: 100 },
      );
    });
  });

  // Feature: careers-position-card, Property 6: Skills display and truncation
  // Validates: Requirements 7.1, 7.2, 7.3
  describe('Property 6: Skills display and truncation', () => {
    it('for any PublicPosition, skills are displayed with truncation at 5 and "+N more" indicator', () => {
      fc.assert(
        fc.property(publicPositionArb(), position => {
          const fixture = createComponent(position);
          const el: HTMLElement = fixture.nativeElement;
          const skills = position.skills ?? [];

          if (skills.length === 0) {
            expect(el.querySelector('.position-skills')).toBeNull();
          } else {
            const skillsSection = el.querySelector('.position-skills');
            expect(skillsSection).toBeTruthy();

            const tags = Array.from(el.querySelectorAll('.skill-tag:not(.more-tag)'));
            const expectedVisible = Math.min(skills.length, 5);
            expect(tags.length).toBe(expectedVisible);

            for (let i = 0; i < expectedVisible; i++) {
              expect(tags[i].textContent?.trim()).toBe(skills[i].trim());
            }

            const moreTag = el.querySelector('.more-tag');
            if (skills.length > 5) {
              expect(moreTag).toBeTruthy();
              expect(moreTag?.textContent?.trim()).toBe(`+${skills.length - 5} more`);
            } else {
              expect(moreTag).toBeNull();
            }
          }

          fixture.destroy();
        }),
        { numRuns: 100 },
      );
    });
  });
});
