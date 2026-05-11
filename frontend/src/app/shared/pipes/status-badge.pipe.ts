import { Pipe, PipeTransform } from '@angular/core';

/**
 * Maps a status string to a CSS badge class from _badges.scss.
 * This is the single standard way to apply status badges across the app.
 *
 * Badge classes use the outline design (colored border + subtle background).
 * See: src/app/styles/_badges.scss for the full list of classes.
 *
 * Status mapping:
 *   ACTIVE / INVITED  → badge-status-active  (green)
 *   PENDING           → badge-status-pending  (yellow)
 *   INACTIVE          → badge-status-inactive (gray)
 *   REJECTED          → badge-status-rejected (red)
 *   COMPLETED         → badge-status-completed (blue)
 *   unknown           → badge-status-unknown  (gray fallback)
 */
@Pipe({
  name: 'statusBadge',
  standalone: true,
})
export class StatusBadgePipe implements PipeTransform {
  transform(status: string | undefined | null): string {
    switch (status?.toUpperCase()) {
      case 'ACTIVE':
      case 'INVITED':
        return 'badge-status-active';
      case 'PENDING':
        return 'badge-status-pending';
      case 'INACTIVE':
        return 'badge-status-inactive';
      case 'REJECTED':
        return 'badge-status-rejected';
      case 'COMPLETED':
        return 'badge-status-completed';
      default:
        return 'badge-status-unknown';
    }
  }
}
