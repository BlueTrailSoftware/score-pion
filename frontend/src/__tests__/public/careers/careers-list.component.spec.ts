import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { By } from '@angular/platform-browser';
import { of, throwError } from 'rxjs';
import { CareersListComponent } from '../../../app/public/careers/careers-list.component';
import { CareersPositionCardComponent } from '../../../app/public/careers/careers-position-card/careers-position-card.component';
import { PositionService } from '../../../app/services/position.service';
import { PublicPosition } from '../../../app/models/position.model';
import { ALL_LOCATIONS } from '../../../app/public/careers/careers-list.utils';

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
    expect(component.allPositions).toEqual([mockPosition]);
    expect(component.filteredPositions).toEqual([mockPosition]);
    expect(component.selectedLocation).toBe(ALL_LOCATIONS);
    expect(component.loading).toBeFalse();
  });

  it('should set empty array when response data is undefined', fakeAsync(() => {
    positionServiceSpy.getPublicPositions.and.returnValue(of({ status: 'success', message: 'OK' }));
    fixture = TestBed.createComponent(CareersListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    tick();

    expect(component.allPositions).toEqual([]);
  }));

  it('should set error on load failure', fakeAsync(() => {
    spyOn(console, 'error');
    positionServiceSpy.getPublicPositions.and.returnValue(throwError(() => new Error('fail')));
    fixture = TestBed.createComponent(CareersListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    tick();

    expect(component.error).toBe('Unable to load positions. Please try again later.');
    expect(component.allPositions).toEqual([]);
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

  describe('CareersPositionCardComponent integration', () => {
    it('should render app-careers-position-card elements for each position', () => {
      const cardElements = fixture.debugElement.queryAll(By.directive(CareersPositionCardComponent));
      expect(cardElements.length).toBe(1);
      expect(cardElements[0].componentInstance.position).toEqual(mockPosition);
    });

    it('should render multiple cards when multiple positions exist', fakeAsync(() => {
      const secondPosition: PublicPosition = {
        id: 'p-2',
        title: 'Backend Dev',
        description: 'Build APIs',
        fileUrl: 'https://example.com/file.pdf',
        createdAt: new Date('2024-02-01'),
      };
      positionServiceSpy.getPublicPositions.and.returnValue(
        of({ status: 'success', message: 'OK', data: [mockPosition, secondPosition] }),
      );
      fixture = TestBed.createComponent(CareersListComponent);
      fixture.detectChanges();
      tick();

      const cardElements = fixture.debugElement.queryAll(By.directive(CareersPositionCardComponent));
      expect(cardElements.length).toBe(2);
    }));

    it('should navigate to detail page when cardClick is emitted', () => {
      spyOn(router, 'navigate');
      const cardDebug = fixture.debugElement.query(By.directive(CareersPositionCardComponent));
      cardDebug.componentInstance.cardClick.emit(mockPosition);

      expect(router.navigate).toHaveBeenCalledWith(['/careers', 'p-1'], { state: { position: mockPosition } });
    });
  });

  describe('Location filter integration', () => {
    const nycPosition: PublicPosition = {
      id: 'p-nyc',
      title: 'NYC Dev',
      description: 'Work in NYC',
      fileUrl: null,
      createdAt: new Date('2024-01-01'),
      location: 'New York',
      workMode: 'Onsite',
    };

    const remotePosition: PublicPosition = {
      id: 'p-remote',
      title: 'Remote Dev',
      description: 'Work remotely',
      fileUrl: null,
      createdAt: new Date('2024-01-01'),
      location: 'Anywhere',
      workMode: 'Remote',
    };

    const sfPosition: PublicPosition = {
      id: 'p-sf',
      title: 'SF Dev',
      description: 'Work in SF',
      fileUrl: null,
      createdAt: new Date('2024-01-01'),
      location: 'San Francisco',
      workMode: 'Onsite',
    };

    it('should default selectedLocation to "All Locations"', () => {
      expect(component.selectedLocation).toBe(ALL_LOCATIONS);
    });

    it('should hide filter dropdown when locationOptions has only "All Locations"', fakeAsync(() => {
      positionServiceSpy.getPublicPositions.and.returnValue(
        of({ status: 'success', message: 'OK', data: [mockPosition] }),
      );
      fixture = TestBed.createComponent(CareersListComponent);
      fixture.detectChanges();
      tick();

      const filterEl = fixture.debugElement.query(By.css('.location-filter'));
      expect(filterEl).toBeNull();
    }));

    it('should show filter dropdown when there are multiple location options', fakeAsync(() => {
      positionServiceSpy.getPublicPositions.and.returnValue(
        of({ status: 'success', message: 'OK', data: [nycPosition, sfPosition] }),
      );
      fixture = TestBed.createComponent(CareersListComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      tick();

      fixture.detectChanges();
      const filterEl = fixture.debugElement.query(By.css('.location-filter'));
      expect(filterEl).toBeTruthy();
    }));

    it('should filter positions when a location is selected', fakeAsync(() => {
      positionServiceSpy.getPublicPositions.and.returnValue(
        of({ status: 'success', message: 'OK', data: [nycPosition, sfPosition, remotePosition] }),
      );
      fixture = TestBed.createComponent(CareersListComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      tick();

      component.onLocationFilterChange('New York');
      expect(component.filteredPositions.length).toBe(1);
      expect(component.filteredPositions[0].id).toBe('p-nyc');
    }));

    it('should show "no matches" empty state when filter yields zero results', fakeAsync(() => {
      positionServiceSpy.getPublicPositions.and.returnValue(
        of({ status: 'success', message: 'OK', data: [nycPosition] }),
      );
      fixture = TestBed.createComponent(CareersListComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      tick();

      component.onLocationFilterChange('San Francisco');
      fixture.detectChanges();

      expect(component.filteredPositions.length).toBe(0);
      expect(component.allPositions.length).toBe(1);
      const noMatchEl = fixture.debugElement.query(By.css('.empty-state'));
      expect(noMatchEl).toBeTruthy();
      expect(noMatchEl.nativeElement.textContent).toContain('No Positions Match');
    }));

    it('should reset filter on reload', fakeAsync(() => {
      positionServiceSpy.getPublicPositions.and.returnValue(
        of({ status: 'success', message: 'OK', data: [nycPosition, sfPosition] }),
      );
      fixture = TestBed.createComponent(CareersListComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      tick();

      component.onLocationFilterChange('New York');
      expect(component.selectedLocation).toBe('New York');

      positionServiceSpy.getPublicPositions.and.returnValue(
        of({ status: 'success', message: 'OK', data: [sfPosition, remotePosition] }),
      );
      component.onRefresh();
      tick();

      expect(component.selectedLocation).toBe(ALL_LOCATIONS);
      expect(component.filteredPositions).toEqual(component.allPositions);
      expect(component.locationOptions).toContain('San Francisco');
      expect(component.locationOptions).toContain('Remote');
    }));
  });
});
