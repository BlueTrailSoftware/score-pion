import { TestBed } from '@angular/core/testing';
import { NotificationService, Notification } from '../../app/services/notification.service';

describe('NotificationService', () => {
  let service: NotificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [NotificationService] });
    service = TestBed.inject(NotificationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('notifications$', () => {
    it('should emit a success notification', done => {
      service.notifications$.subscribe((n: Notification) => {
        expect(n.type).toBe('success');
        expect(n.message).toBe('All good!');
        expect(n.duration).toBe(3000);
        expect(n.id).toContain('notification-');
        done();
      });
      service.success('All good!');
    });

    it('should emit an error notification', done => {
      service.notifications$.subscribe((n: Notification) => {
        expect(n.type).toBe('error');
        expect(n.message).toBe('Something broke');
        done();
      });
      service.error('Something broke');
    });

    it('should emit an info notification', done => {
      service.notifications$.subscribe((n: Notification) => {
        expect(n.type).toBe('info');
        expect(n.message).toBe('FYI');
        done();
      });
      service.info('FYI');
    });

    it('should emit a warning notification', done => {
      service.notifications$.subscribe((n: Notification) => {
        expect(n.type).toBe('warning');
        expect(n.message).toBe('Watch out');
        done();
      });
      service.warning('Watch out');
    });

    it('should respect a custom duration', done => {
      service.notifications$.subscribe((n: Notification) => {
        expect(n.duration).toBe(5000);
        done();
      });
      service.success('Custom duration', 5000);
    });

    it('should increment notification id on each call', () => {
      const emitted: Notification[] = [];
      service.notifications$.subscribe(n => emitted.push(n));

      service.success('first');
      service.success('second');

      expect(emitted[0].id).not.toBe(emitted[1].id);
    });
  });
});
