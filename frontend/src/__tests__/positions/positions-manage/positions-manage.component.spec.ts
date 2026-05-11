import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';
import { PositionsManageComponent } from '../../../app/positions/positions-manage/positions-manage.component';
import { PositionService } from '../../../app/services/position.service';
import { NotificationService } from '../../../app/services/notification.service';
import { PositionListItem } from '../../../app/models/position.model';

const mockPosition: PositionListItem = {
  id: 'p-1',
  title: 'Dev',
  description: 'Desc',
  external: false,
  assessmentsCount: 2,
  isActive: true,
  createdAt: new Date(),
};

describe('PositionsManageComponent', () => {
  let component: PositionsManageComponent;
  let fixture: ComponentFixture<PositionsManageComponent>;
  let positionServiceSpy: jasmine.SpyObj<PositionService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let router: Router;

  beforeEach(async () => {
    positionServiceSpy = jasmine.createSpyObj('PositionService', ['getPositions', 'inviteCandidateToPosition']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    positionServiceSpy.getPositions.and.returnValue(
      of({ items: [mockPosition], total: 1, page: 0, pageSize: 50, totalPages: 1 }),
    );

    await TestBed.configureTestingModule({
      imports: [PositionsManageComponent],
      providers: [
        provideRouter([]),
        { provide: PositionService, useValue: positionServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PositionsManageComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => localStorage.clear());

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('validateEmail', () => {
    it('should return true for valid email', () => {
      expect(component.validateEmail('a@b.com')).toBeTrue();
    });
    it('should return false for invalid email', () => {
      expect(component.validateEmail('invalid')).toBeFalse();
    });
  });

  describe('validateName', () => {
    it('should return true for valid name', () => {
      expect(component.validateName('Jo')).toBeTrue();
    });
    it('should return false for empty name', () => {
      expect(component.validateName('')).toBeFalse();
    });
    it('should return false for single char', () => {
      expect(component.validateName('A')).toBeFalse();
    });
  });

  describe('onPositionsLoaded', () => {
    it('should set positions', () => {
      component.onPositionsLoaded([mockPosition]);
      expect(component.positions).toEqual([mockPosition]);
    });
  });

  describe('onViewPositionDetails', () => {
    it('should navigate to position detail', () => {
      spyOn(router, 'navigate');
      component.onViewPositionDetails(mockPosition);
      expect(router.navigate).toHaveBeenCalledWith(['/positions-manage', 'p-1']);
    });
  });

  describe('modal', () => {
    it('onInviteToPosition should open modal and set position', () => {
      component.onInviteToPosition(mockPosition);
      expect(component.showModal).toBeTrue();
      expect(component.selectedPosition).toEqual(mockPosition);
    });

    it('closeModal should reset all modal state', () => {
      component.onInviteToPosition(mockPosition);
      component.candidateEmail = 'test@test.com';
      component.closeModal();
      expect(component.showModal).toBeFalse();
      expect(component.selectedPosition).toBeNull();
      expect(component.candidateEmail).toBe('');
    });

    it('selectedPositionTitle should return position title', () => {
      component.selectedPosition = mockPosition;
      expect(component.selectedPositionTitle).toBe('Dev');
    });

    it('selectedPositionTitle should return empty when no position', () => {
      expect(component.selectedPositionTitle).toBe('');
    });
  });

  describe('validateModalForm', () => {
    it('should return false with invalid email', () => {
      component.candidateEmail = 'bad';
      component.candidateName = 'Valid Name';
      expect(component.validateModalForm()).toBeFalse();
      expect(component.emailError).toBeTruthy();
    });

    it('should return false with invalid name', () => {
      component.candidateEmail = 'a@b.com';
      component.candidateName = 'A';
      expect(component.validateModalForm()).toBeFalse();
      expect(component.nameError).toBeTruthy();
    });

    it('should return true with valid data', () => {
      component.candidateEmail = 'a@b.com';
      component.candidateName = 'Jane Doe';
      expect(component.validateModalForm()).toBeTrue();
    });
  });

  describe('onSubmitModal', () => {
    it('should call inviteCandidateToPosition on valid form', fakeAsync(() => {
      positionServiceSpy.inviteCandidateToPosition.and.returnValue(of({ status: 'success', message: 'OK' }));
      component.selectedPosition = mockPosition;
      component.candidateEmail = 'a@b.com';
      component.candidateName = 'Jane Doe';

      component.onSubmitModal();
      tick();

      expect(positionServiceSpy.inviteCandidateToPosition).toHaveBeenCalledWith('a@b.com', 'Jane Doe', 'p-1');
      expect(notificationServiceSpy.success).toHaveBeenCalled();
    }));

    it('should not call service when form is invalid', () => {
      component.selectedPosition = mockPosition;
      component.candidateEmail = '';
      component.onSubmitModal();
      expect(positionServiceSpy.inviteCandidateToPosition).not.toHaveBeenCalled();
    });
  });

  describe('goToCreatePosition', () => {
    it('should navigate to create page', () => {
      spyOn(router, 'navigate');
      component.goToCreatePosition();
      expect(router.navigate).toHaveBeenCalledWith(['/positions-manage/create']);
    });
  });
});
