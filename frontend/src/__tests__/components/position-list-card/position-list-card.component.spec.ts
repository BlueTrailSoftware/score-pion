import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { PositionListCardComponent } from '../../../app/components/position-list-card/position-list-card.component';
import { PositionService } from '../../../app/services/position.service';
import { PositionListItem } from '../../../app/models/position.model';
import { PagedData } from '../../../app/models/responses/api-base-response.model';

const mockPosition: PositionListItem = {
  id: 'p-1',
  title: 'Frontend Dev',
  description: 'Build UIs',
  external: false,
  assessmentsCount: 2,
  isActive: true,
  createdAt: new Date('2024-01-01'),
};

const mockResponse: PagedData<PositionListItem> = {
  items: [mockPosition],
  total: 1,
  page: 0,
  pageSize: 50,
  totalPages: 1,
};

describe('PositionListCardComponent', () => {
  let component: PositionListCardComponent;
  let fixture: ComponentFixture<PositionListCardComponent>;
  let positionServiceSpy: jasmine.SpyObj<PositionService>;

  beforeEach(async () => {
    positionServiceSpy = jasmine.createSpyObj('PositionService', ['getPositions']);
    positionServiceSpy.getPositions.and.returnValue(of(mockResponse));

    await TestBed.configureTestingModule({
      imports: [PositionListCardComponent],
      providers: [{ provide: PositionService, useValue: positionServiceSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(PositionListCardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  describe('autoLoad', () => {
    it('should load positions on init when autoLoad is true (default)', fakeAsync(() => {
      fixture.detectChanges();
      tick();

      expect(positionServiceSpy.getPositions).toHaveBeenCalled();
      expect(component.positions).toEqual([mockPosition]);
      expect(component.loading).toBeFalse();
    }));

    it('should NOT load positions on init when autoLoad is false', () => {
      component.autoLoad = false;
      fixture.detectChanges();

      expect(positionServiceSpy.getPositions).not.toHaveBeenCalled();
      expect(component.positions).toEqual([]);
    });
  });

  describe('loadPositions', () => {
    it('should pass activeOnly=true when showInactive is false', fakeAsync(() => {
      component.showInactive = false;
      fixture.detectChanges();
      tick();

      const filters = positionServiceSpy.getPositions.calls.mostRecent().args[0];
      expect(filters?.activeOnly).toBeTrue();
    }));

    it('should pass activeOnly=false when showInactive is true', fakeAsync(() => {
      component.showInactive = true;
      fixture.detectChanges();
      tick();

      const filters = positionServiceSpy.getPositions.calls.mostRecent().args[0];
      expect(filters?.activeOnly).toBeFalse();
    }));

    it('should emit positionsLoaded after loading', fakeAsync(() => {
      spyOn(component.positionsLoaded, 'emit');
      fixture.detectChanges();
      tick();

      expect(component.positionsLoaded.emit).toHaveBeenCalledWith([mockPosition]);
    }));

    it('should set empty array when response data is undefined', fakeAsync(() => {
      positionServiceSpy.getPositions.and.returnValue(
        of({ items: [], total: 0, page: 0, pageSize: 50, totalPages: 0 }),
      );
      fixture.detectChanges();
      tick();

      expect(component.positions).toEqual([]);
    }));

    it('should set error message on failure', fakeAsync(() => {
      spyOn(console, 'error');
      positionServiceSpy.getPositions.and.returnValue(throwError(() => new Error('Network error')));
      fixture.detectChanges();
      tick();

      expect(component.error).toBe('Error loading positions');
      expect(component.loading).toBeFalse();
    }));
  });

  describe('onPositionClick', () => {
    it('should emit positionView event', () => {
      spyOn(component.positionView, 'emit');
      component.onPositionClick(mockPosition);
      expect(component.positionView.emit).toHaveBeenCalledWith(mockPosition);
    });
  });

  describe('onInvite', () => {
    it('should emit positionInvite and stop event propagation', () => {
      spyOn(component.positionInvite, 'emit');
      const event = new Event('click');
      spyOn(event, 'stopPropagation');

      component.onInvite(mockPosition, event);

      expect(event.stopPropagation).toHaveBeenCalled();
      expect(component.positionInvite.emit).toHaveBeenCalledWith(mockPosition);
    });
  });

  describe('getInviteButtonTooltip', () => {
    it('should return inactive message when position is not active', () => {
      expect(component.getInviteButtonTooltip({ ...mockPosition, isActive: false })).toBe('Position is inactive');
    });

    it('should return no assessments message when count is 0', () => {
      expect(component.getInviteButtonTooltip({ ...mockPosition, assessmentsCount: 0 })).toBe(
        'No assessments assigned to this position',
      );
    });

    it('should return invite message for active position with assessments', () => {
      expect(component.getInviteButtonTooltip(mockPosition)).toBe('Invite a candidate to this position');
    });
  });

  describe('setPositions', () => {
    it('should set positions directly and stop loading', () => {
      component.loading = true;
      const positions = [mockPosition];

      component.setPositions(positions);

      expect(component.positions).toEqual(positions);
      expect(component.loading).toBeFalse();
    });
  });

  describe('reloadPositions', () => {
    it('should call loadPositions again', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      positionServiceSpy.getPositions.calls.reset();

      component.reloadPositions();
      tick();

      expect(positionServiceSpy.getPositions).toHaveBeenCalledTimes(1);
    }));
  });
});
