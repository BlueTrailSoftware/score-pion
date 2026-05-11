import * as fc from 'fast-check';
import { formatExperienceRange } from '../../../../app/public/careers/careers-position-card/careers-position-card.utils';

// Feature: careers-position-card, Property 5: Experience range formatting
// Validates: Requirements 5.1, 5.2, 5.3, 5.4
describe('formatExperienceRange - Property-Based Tests', () => {
  const nonNegInt = fc.integer({ min: 0, max: 100 });

  it('Property 5: for any pair of optional non-negative integers, formatExperienceRange returns the correct format', () => {
    const inputArb = fc.tuple(fc.option(nonNegInt, { nil: undefined }), fc.option(nonNegInt, { nil: undefined }));

    fc.assert(
      fc.property(inputArb, ([min, max]) => {
        const result = formatExperienceRange(min, max);
        const hasMin = min != null;
        const hasMax = max != null;

        if (hasMin && hasMax && min === max) {
          expect(result).toBe(`${min} years`);
        } else if (hasMin && hasMax) {
          expect(result).toBe(`${min} – ${max} years`);
        } else if (hasMin) {
          expect(result).toBe(`${min}+ years`);
        } else if (hasMax) {
          expect(result).toBe(`Up to ${max} years`);
        } else {
          expect(result).toBeNull();
        }
      }),
      { numRuns: 100 },
    );
  });

  // Feature: career-detail-fields, Property 2: Experience range formatting with equal min/max
  // Validates: Requirements 7.5
  it('Property 2: for any non-negative integer X, formatExperienceRange(X, X) returns "X years"', () => {
    fc.assert(
      fc.property(nonNegInt, x => {
        const result = formatExperienceRange(x, x);
        expect(result).toBe(`${x} years`);
      }),
      { numRuns: 100 },
    );
  });
});
