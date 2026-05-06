import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CareersListComponent } from '../../../app/public/careers/careers-list.component';
import { PositionService } from '../../../app/services/position.service';
import { PublicPosition } from '../../../app/models/position.model';

const mockPosition: PublicPosition = {
  id: 'p-1',
  title: 'Frontend Dev',
  description: 'Build UIs',
  fileUrl: null,
  createdAt: new Date('2024-01-01'),
};

describe('CareersListComponent', () => {
  let component: CareersListComponent;
  let fixture: ComponentFixture<CareersListComponent>;
  let positionServiceSpy: jasmine.SpyObj<PositionService>;
  let router: Router;

  beforeEach(async () => {
    positionServiceSpy = jasmine.createSpyObj('PositionService', ['getPublicPositions', 'clearPublicPositionsCache']);
    positionServiceSpy.getPublicPositions.and.returnValue(
      of({ status: 'success', message: 'OK', data: [mockPosition] }),
    );

    await TestBed.configureTestingModule({
      imports: [CareersListComponent],
      providers: [provideRouter([]), { provide: PositionService, useValue: positionServiceSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(CareersListComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load positions on init', () => {
    expect(positionServiceSpy.getPublicPositions).toHaveBeenCalled();
    expect(component.positions).toEqual([mockPosition]);
    expect(component.loading).toBeFalse();
  });

  it('should set empty array when response data is undefined', fakeAsync(() => {
    positionServiceSpy.getPublicPositions.and.returnValue(of({ status: 'success', message: 'OK' }));
    fixture = TestBed.createComponent(CareersListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    tick();

    expect(component.positions).toEqual([]);
  }));

  it('should set error on load failure', fakeAsync(() => {
    spyOn(console, 'error');
    positionServiceSpy.getPublicPositions.and.returnValue(throwError(() => new Error('fail')));
    fixture = TestBed.createComponent(CareersListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    tick();

    expect(component.error).toBe('Unable to load positions. Please try again later.');
    expect(component.positions).toEqual([]);
  }));

  describe('onPositionClick', () => {
    it('should navigate to position detail with state', () => {
      spyOn(router, 'navigate');
      component.onPositionClick(mockPosition);
      expect(router.navigate).toHaveBeenCalledWith(['/careers', 'p-1'], { state: { position: mockPosition } });
    });
  });

  describe('onRefresh', () => {
    it('should clear cache and reload positions', () => {
      positionServiceSpy.getPublicPositions.calls.reset();
      component.onRefresh();
      expect(positionServiceSpy.clearPublicPositionsCache).toHaveBeenCalled();
      expect(positionServiceSpy.getPublicPositions).toHaveBeenCalled();
    });
  });

  describe('trackByPositionId', () => {
    it('should return position id', () => {
      expect(component.trackByPositionId(0, mockPosition)).toBe('p-1');
    });
  });
});
