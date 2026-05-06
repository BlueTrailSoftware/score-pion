import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

export type NotificationType = 'success' | 'error' | 'info' | 'warning';

export interface Notification {
  type: NotificationType;
  message: string;
  duration?: number;
  id?: string;
}

/**
 * Service for displaying notifications across the application
 * Provides a centralized way to show success, error, info and warning messages
 * Automatically handles timeouts and cleanup
 */
@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private notificationSubject = new Subject<Notification>();
  private notificationId = 0;

  /**
   * Observable stream of notifications
   * Components can subscribe to this to display notifications
   */
  get notifications$(): Observable<Notification> {
    return this.notificationSubject.asObservable();
  }

  /**
   * Show a success notification
   * @param message Message to display
   * @param duration Duration in milliseconds (default: 3000)
   */
  success(message: string, duration: number = 3000): void {
    this.show('success', message, duration);
  }

  /**
   * Show an error notification
   * @param message Message to display
   * @param duration Duration in milliseconds (default: 3000)
   */
  error(message: string, duration: number = 3000): void {
    this.show('error', message, duration);
  }

  /**
   * Show an info notification
   * @param message Message to display
   * @param duration Duration in milliseconds (default: 3000)
   */
  info(message: string, duration: number = 3000): void {
    this.show('info', message, duration);
  }

  /**
   * Show a warning notification
   * @param message Message to display
   * @param duration Duration in milliseconds (default: 3000)
   */
  warning(message: string, duration: number = 3000): void {
    this.show('warning', message, duration);
  }

  /**
   * Internal method to emit notifications
   */
  private show(type: NotificationType, message: string, duration: number): void {
    const notification: Notification = {
      type,
      message,
      duration,
      id: `notification-${++this.notificationId}`,
    };
    this.notificationSubject.next(notification);
  }
}
