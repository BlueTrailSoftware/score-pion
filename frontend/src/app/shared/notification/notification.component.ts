import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { NotificationService, Notification } from '../../services/notification.service';

interface ActiveNotification extends Notification {
  hiding: boolean;
  timeout?: any;
}

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.scss'],
})
export class NotificationComponent implements OnInit, OnDestroy {
  private notificationService = inject(NotificationService);

  activeNotifications: ActiveNotification[] = [];
  private subscription?: Subscription;

  /**
   * Subscribe to notification service on component initialization
   */
  ngOnInit(): void {
    this.subscription = this.notificationService.notifications$.subscribe(notification => {
      this.addNotification(notification);
    });
  }

  /**
   * Clean up timeouts and unsubscribe on component destruction
   */
  ngOnDestroy(): void {
    this.activeNotifications.forEach(n => {
      if (n.timeout) {
        clearTimeout(n.timeout);
      }
    });
    this.subscription?.unsubscribe();
  }

  /**
   * Add new notification to active list and set auto-hide timer
   */
  private addNotification(notification: Notification): void {
    const activeNotification: ActiveNotification = {
      ...notification,
      hiding: false,
    };

    this.activeNotifications.push(activeNotification);

    // Auto-hide after duration
    if (notification.duration && notification.duration > 0) {
      activeNotification.timeout = setTimeout(() => {
        this.closeNotification(activeNotification);
      }, notification.duration);
    }
  }

  /**
   * Mark notification as hiding and clear its timeout
   */
  closeNotification(notification: ActiveNotification): void {
    notification.hiding = true;
    if (notification.timeout) {
      clearTimeout(notification.timeout);
    }
  }

  /**
   * Remove notification from list when hide animation completes
   */
  onAnimationEnd(notification: ActiveNotification): void {
    if (notification.hiding) {
      const index = this.activeNotifications.indexOf(notification);
      if (index > -1) {
        this.activeNotifications.splice(index, 1);
      }
    }
  }

  /**
   * Get Bootstrap alert CSS class based on notification type
   */
  getAlertClass(type: string): string {
    switch (type) {
      case 'success':
        return 'alert-success';
      case 'error':
        return 'alert-danger';
      case 'warning':
        return 'alert-warning';
      case 'info':
        return 'alert-info';
      default:
        return 'alert-info';
    }
  }
}
