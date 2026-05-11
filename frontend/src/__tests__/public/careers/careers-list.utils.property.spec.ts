import * as fc from 'fast-check';
import {
  deriveLocationOptions,
  filterPositionsByLocation,
  ALL_LOCATIONS,
  REMOTE,
} from '../../../app/public/careers/careers-list.utils';
import { PublicPosition, WorkMode, JobType, JOB_TYPES, WORK_MODES } from '../../../app/models/position.model';

// --- Generators ---

const workModeArb: fc.Arbitrary<WorkMode> = fc.constantFrom(...WORK_MODES);
const jobTypeArb: fc.Arbitrary<JobType> = fc.constantFrom(...JOB_TYPES);

const locationStringArb = fc.oneof(
  fc.constant(''),
  fc.constant('  '),
  fc.string({ minLength: 1, maxLength: 30 }).filter(s => s.trim().length > 0),
);

const publicPositionArb: fc.Arbitrary<PublicPosition> = fc.record({
  id: fc.uuid(),
  title: fc.string({ minLength: 1, maxLength: 50 }),
  description: fc.string({ maxLength: 100 }),
  fileUrl: fc.option(fc.webUrl(), { nil: null }),
  createdAt: fc.date(),
  jobType: fc.option(jobTypeArb, { nil: undefined }),
  workMode: fc.option(workModeArb, { nil: undefined }),
  experienceMin: fc.option(fc.nat({ max: 50 }), { nil: undefined }),
  experienceMax: fc.option(fc.nat({ max: 50 }), { nil: undefined }),
  location: fc.option(locationStringArb, { nil: undefined }),
  skills: fc.option(fc.array(fc.string({ minLength: 1, maxLength: 20 }), { maxLength: 5 }), { nil: undefined }),
});

const positionsArb = fc.array(publicPositionArb, { maxLength: 30 });

// Feature: careers-location-filter, Property 1: Location options derivation
// Validates: Requirements 1.1, 1.2, 1.3, 1.4
describe('deriveLocationOptions - Property-Based Tests', () => {
  it('Property 1: for any array of PublicPosition, deriveLocationOptions returns a correctly structured list', () => {
    fc.assert(
      fc.property(positionsArb, positions => {
        const result = deriveLocationOptions(positions);

        // First element is always "All Locations"
        expect(result[0]).toBe(ALL_LOCATIONS);

        // No duplicates
        const unique = new Set(result);
        expect(unique.size).toBe(result.length);

        // Remaining elements (after "All Locations") are sorted alphabetically
        const rest = result.slice(1);
        const sorted = [...rest].sort((a, b) => a.localeCompare(b));
        expect(rest).toEqual(sorted);

        // Every distinct non-empty trimmed location appears
        const expectedLocations = new Set<string>();
        for (const p of positions) {
          if (p.location && p.location.trim()) {
            expectedLocations.add(p.location.trim());
          }
        }
        for (const loc of expectedLocations) {
          expect(result).toContain(loc);
        }

        // "Remote" appears iff at least one position has workMode === 'Remote'
        const hasRemotePosition = positions.some(p => p.workMode === 'Remote');
        if (hasRemotePosition) {
          expect(result).toContain(REMOTE);
        } else {
          // "Remote" should only appear if a position has location "Remote"
          const hasRemoteLocation = positions.some(p => p.location?.trim() === REMOTE);
          if (!hasRemoteLocation) {
            expect(result).not.toContain(REMOTE);
          }
        }
      }),
      { numRuns: 100 },
    );
  });
});

// Feature: careers-location-filter, Property 2: Position filtering by location
// Validates: Requirements 2.2, 2.3, 2.4
describe('filterPositionsByLocation - Property-Based Tests', () => {
  it('Property 2: for any positions and selected location, filterPositionsByLocation returns the correct subset', () => {
    const selectedLocationArb = fc.oneof(
      fc.constant(ALL_LOCATIONS),
      fc.constant(REMOTE),
      fc.constant(''),
      locationStringArb,
    );

    fc.assert(
      fc.property(positionsArb, selectedLocationArb, (positions, selectedLocation) => {
        const result = filterPositionsByLocation(positions, selectedLocation);

        // Result is always a subset of input (no new elements)
        for (const r of result) {
          expect(positions).toContain(r);
        }

        // Relative order is preserved
        const resultIndices = result.map(r => positions.indexOf(r));
        for (let i = 1; i < resultIndices.length; i++) {
          expect(resultIndices[i]).toBeGreaterThan(resultIndices[i - 1]);
        }

        if (!selectedLocation || selectedLocation === ALL_LOCATIONS) {
          // Returns full array
          expect(result).toEqual(positions);
        } else if (selectedLocation === REMOTE) {
          // Only remote positions
          for (const r of result) {
            expect(r.workMode).toBe('Remote');
          }
          // All remote positions are included
          const allRemote = positions.filter(p => p.workMode === 'Remote');
          expect(result.length).toBe(allRemote.length);
        } else {
          // Case-insensitive location match
          const normalised = selectedLocation.trim().toLowerCase();
          for (const r of result) {
            expect(r.location?.trim().toLowerCase()).toBe(normalised);
          }
          // All matching positions are included
          const allMatching = positions.filter(p => p.location?.trim().toLowerCase() === normalised);
          expect(result.length).toBe(allMatching.length);
        }
      }),
      { numRuns: 100 },
    );
  });
});
