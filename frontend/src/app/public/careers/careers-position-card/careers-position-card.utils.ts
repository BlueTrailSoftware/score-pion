export function formatExperienceRange(min: number | undefined | null, max: number | undefined | null): string | null {
  const hasMin = min != null;
  const hasMax = max != null;
  if (hasMin && hasMax) {
    return min === max ? `${min} years` : `${min} – ${max} years`;
  }
  if (hasMin) return `${min}+ years`;
  if (hasMax) return `Up to ${max} years`;
  return null;
}
