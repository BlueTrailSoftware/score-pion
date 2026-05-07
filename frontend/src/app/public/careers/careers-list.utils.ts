import { PublicPosition } from '../../models/position.model';

export const ALL_LOCATIONS = 'All Locations';
export const ALL_JOB_TYPES = 'All Job Types';
export const ALL_WORK_MODES = 'All Work Modes';
export const ALL_SKILLS = 'All Skills';
export const REMOTE = 'Remote';

export function deriveLocationOptions(positions: PublicPosition[]): string[] {
  const locations = new Set<string>();

  for (const p of positions) {
    if (p.workMode === 'Remote') {
      locations.add(REMOTE);
    }
    if (p.location && p.location.trim()) {
      locations.add(p.location.trim());
    }
  }

  const sorted = Array.from(locations).sort((a, b) => a.localeCompare(b));
  return [ALL_LOCATIONS, ...sorted];
}

export function deriveJobTypeOptions(positions: PublicPosition[]): string[] {
  const types = new Set<string>();
  for (const p of positions) {
    if (p.jobType) {
      types.add(p.jobType);
    }
  }
  const sorted = Array.from(types).sort((a, b) => a.localeCompare(b));
  return [ALL_JOB_TYPES, ...sorted];
}

export function deriveWorkModeOptions(positions: PublicPosition[]): string[] {
  const modes = new Set<string>();
  for (const p of positions) {
    if (p.workMode) {
      modes.add(p.workMode);
    }
  }
  const sorted = Array.from(modes).sort((a, b) => a.localeCompare(b));
  return [ALL_WORK_MODES, ...sorted];
}

export function deriveSkillOptions(positions: PublicPosition[]): string[] {
  const skills = new Map<string, string>();
  for (const p of positions) {
    if (p.skills) {
      for (const s of p.skills) {
        if (s && s.trim()) {
          const key = s.trim().toLowerCase();
          if (!skills.has(key)) {
            skills.set(key, s.trim());
          }
        }
      }
    }
  }
  const sorted = Array.from(skills.values()).sort((a, b) => a.localeCompare(b, undefined, { sensitivity: 'base' }));
  return [ALL_SKILLS, ...sorted];
}

export function filterPositions(
  positions: PublicPosition[],
  selectedLocation: string,
  selectedJobType: string,
  selectedWorkMode: string,
  selectedSkill: string = ALL_SKILLS,
): PublicPosition[] {
  let result = positions;

  // Location filter
  if (selectedLocation && selectedLocation !== ALL_LOCATIONS) {
    if (selectedLocation === REMOTE) {
      result = result.filter(p => p.workMode === 'Remote');
    } else {
      result = result.filter(p => p.location?.trim().toLowerCase() === selectedLocation.trim().toLowerCase());
    }
  }

  // Job Type filter
  if (selectedJobType && selectedJobType !== ALL_JOB_TYPES) {
    result = result.filter(p => p.jobType === selectedJobType);
  }

  // Work Mode filter
  if (selectedWorkMode && selectedWorkMode !== ALL_WORK_MODES) {
    result = result.filter(p => p.workMode === selectedWorkMode);
  }

  // Skill filter
  if (selectedSkill && selectedSkill !== ALL_SKILLS) {
    result = result.filter(p => p.skills?.some(s => s.trim().toLowerCase() === selectedSkill.trim().toLowerCase()));
  }

  return result;
}

/** @deprecated Use filterPositions instead */
export function filterPositionsByLocation(positions: PublicPosition[], selectedLocation: string): PublicPosition[] {
  return filterPositions(positions, selectedLocation, ALL_JOB_TYPES, ALL_WORK_MODES, ALL_SKILLS);
}
