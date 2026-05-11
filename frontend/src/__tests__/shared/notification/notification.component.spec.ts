import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { NotificationComponent } from '../../../app/shared/notification/notification.component';
import { NotificationService } from '../../../app/services/notification.service';

describe('NotificationComponent', () => {
  let component: NotificationComponent;
  let fixture: ComponentFixture<NotificationComponent>;
  let notificationService: NotificationService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NotificationComponent],
      providers: [NotificationService],
    }).compileComponents();

    fixture = TestBed.createComponent(NotificationComponent);
    component = fixture.componentInstance;
    notificationService = TestBed.inject(NotificationService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should add notification when service emits', () => {
    notificationService.success('Hello');
    expect(component.activeNotifications.length).toBe(1);
    expect(component.activeNotifications[0].message).toBe('Hello');
    expect(component.activeNotifications[0].type).toBe('success');
    expect(component.activeNotifications[0].hiding).toBeFalse();
  });

  it('should accumulate multiple notifications', () => {
    notificationService.success('First');
    notificationService.error('Second');
    expect(component.activeNotifications.length).toBe(2);
  });

  it('should auto-hide notification after duration', fakeAsync(() => {
    notificationService.success('Auto hide', 1000);
    expect(component.activeNotifications.length).toBe(1);

    tick(1000);

    expect(component.activeNotifications[0].hiding).toBeTrue();
  }));

  describe('closeNotification', () => {
    it('should mark notification as hiding and clear timeout', () => {
      notificationService.success('Close me');
      const notification = component.activeNotifications[0];

      expect(notification.timeout).toBeDefined();
      component.closeNotification(notification);

      expect(notification.hiding).toBeTrue();
    });
  });

  describe('onAnimationEnd', () => {
    it('should remove notification from list when hiding', () => {
      notificationService.success('Remove me');
      const notification = component.activeNotifications[0];
      notification.hiding = true;

      component.onAnimationEnd(notification);

      expect(component.activeNotifications.length).toBe(0);
    });

    it('should not remove notification when not hiding', () => {
      notificationService.success('Keep me');
      const notification = component.activeNotifications[0];

      component.onAnimationEnd(notification);

      expect(component.activeNotifications.length).toBe(1);
    });
  });

  describe('getAlertClass', () => {
    it('should return alert-success for success', () => {
      expect(component.getAlertClass('success')).toBe('alert-success');
    });

    it('should return alert-danger for error', () => {
      expect(component.getAlertClass('error')).toBe('alert-danger');
    });

    it('should return alert-warning for warning', () => {
      expect(component.getAlertClass('warning')).toBe('alert-warning');
    });

    it('should return alert-info for info', () => {
      expect(component.getAlertClass('info')).toBe('alert-info');
    });

    it('should return alert-info for unknown type', () => {
      expect(component.getAlertClass('unknown')).toBe('alert-info');
    });
  });

  it('should clean up on destroy', fakeAsync(() => {
    notificationService.success('Cleanup', 5000);
    expect(component.activeNotifications.length).toBe(1);

    component.ngOnDestroy();

    // After destroy, the timeout should be cleared (no auto-hide fires)
    tick(5000);
    // Notification is still in the array but timeout was cleared
    expect(component.activeNotifications[0].hiding).toBeFalse();
  }));
});
