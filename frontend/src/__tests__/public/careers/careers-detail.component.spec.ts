import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter, Router, ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { of, throwError } from 'rxjs';
import { CareersDetailComponent } from '../../../app/public/careers/careers-detail.component';
import { PositionService } from '../../../app/services/position.service';
import { PublicPosition } from '../../../app/models/position.model';

const mockPosition: PublicPosition = {
  id: 'p-1',
  title: 'Frontend Dev',
  description: 'Build UIs',
  fileUrl: null,
  createdAt: new Date('2024-01-01'),
};

const mockPositionWithMetadata: PublicPosition = {
  id: 'p-2',
  title: 'Senior Engineer',
  description: 'Lead projects',
  fileUrl: null,
  createdAt: new Date('2024-06-15'),
  jobType: 'Full Time',
  workMode: 'Remote',
  experienceMin: 3,
  experienceMax: 5,
  location: 'New York, NY',
  skills: ['TypeScript', 'Angular', 'Node.js'],
};

describe('CareersDetailComponent', () => {
  let component: CareersDetailComponent;
  let fixture: ComponentFixture<CareersDetailComponent>;
  let positionServiceSpy: jasmine.SpyObj<PositionService>;
  let router: Router;
  let location: Location;

  function createComponent(paramId: string | null = 'p-1') {
    TestBed.configureTestingModule({
      imports: [CareersDetailComponent],
      providers: [
        provideRouter([]),
        { provide: PositionService, useValue: positionServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => paramId } } },
        },
      ],
    });

    fixture = TestBed.createComponent(CareersDetailComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    location = TestBed.inject(Location);
  }

  beforeEach(() => {
    positionServiceSpy = jasmine.createSpyObj('PositionService', ['getPublicPositionById']);
    positionServiceSpy.getPublicPositionById.and.returnValue(
      of({ status: 'success', message: 'OK', data: mockPosition }),
    );
  });

  it('should create', () => {
    createComponent();
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should load position from API when no navigation state', fakeAsync(() => {
    createComponent();
    fixture.detectChanges();
    tick();

    expect(positionServiceSpy.getPublicPositionById).toHaveBeenCalledWith('p-1');
    expect(component.position).toEqual(mockPosition);
    expect(component.loading).toBeFalse();
  }));

  it('should set error when position ID is missing', () => {
    createComponent(null);
    fixture.detectChanges();

    expect(component.error).toBe('Position ID not found');
    expect(positionServiceSpy.getPublicPositionById).not.toHaveBeenCalled();
  });

  it('should set error when API returns no data', fakeAsync(() => {
    positionServiceSpy.getPublicPositionById.and.returnValue(of({ status: 'success', message: 'OK' }));
    createComponent();
    fixture.detectChanges();
    tick();

    expect(component.error).toBe('Position not found or no longer available');
  }));

  it('should set 404 error message', fakeAsync(() => {
    spyOn(console, 'error');
    positionServiceSpy.getPublicPositionById.and.returnValue(throwError(() => ({ status: 404 })));
    createComponent();
    fixture.detectChanges();
    tick();

    expect(component.error).toBe('Position not found or no longer available');
  }));

  it('should set generic error message for non-404 errors', fakeAsync(() => {
    spyOn(console, 'error');
    positionServiceSpy.getPublicPositionById.and.returnValue(throwError(() => ({ status: 500 })));
    createComponent();
    fixture.detectChanges();
    tick();

    expect(component.error).toBe('Unable to load position details. Please try again later.');
  }));

  describe('goBack', () => {
    it('should call location.back()', () => {
      createComponent();
      fixture.detectChanges();
      spyOn(location, 'back');
      component.goBack();
      expect(location.back).toHaveBeenCalled();
    });
  });

  describe('onApply', () => {
    it('should navigate to apply route when position exists', fakeAsync(() => {
      createComponent();
      fixture.detectChanges();
      tick();

      spyOn(router, 'navigate');
      component.onApply();
      expect(router.navigate).toHaveBeenCalledWith(['/careers', 'p-1', 'apply']);
    }));

    it('should not navigate when position is null', () => {
      createComponent();
      fixture.detectChanges();
      component.position = null;

      spyOn(router, 'navigate');
      component.onApply();
      expect(router.navigate).not.toHaveBeenCalled();
    });
  });

  describe('metadata rendering', () => {
    function setPositionAndDetect(pos: PublicPosition) {
      positionServiceSpy.getPublicPositionById.and.returnValue(of({ status: 'success', message: 'OK', data: pos }));
      createComponent();
      fixture.detectChanges();
    }

    it('should render jobType when present', () => {
      setPositionAndDetect(mockPositionWithMetadata);
      const metadataItems = fixture.nativeElement.querySelectorAll('.metadata-item');
      const texts = Array.from(metadataItems).map((el: any) => el.textContent);
      expect(texts.some((t: string) => t.includes('Full Time'))).toBeTrue();
    });

    it('should omit jobType when absent', () => {
      setPositionAndDetect({ ...mockPositionWithMetadata, jobType: undefined });
      const metadataItems = fixture.nativeElement.querySelectorAll('.metadata-item');
      const texts = Array.from(metadataItems).map((el: any) => el.textContent);
      expect(texts.some((t: string) => t.includes('Job Type'))).toBeFalse();
    });

    it('should render workMode when present', () => {
      setPositionAndDetect(mockPositionWithMetadata);
      const metadataItems = fixture.nativeElement.querySelectorAll('.metadata-item');
      const texts = Array.from(metadataItems).map((el: any) => el.textContent);
      expect(texts.some((t: string) => t.includes('Remote'))).toBeTrue();
    });

    it('should omit workMode when absent', () => {
      setPositionAndDetect({ ...mockPositionWithMetadata, workMode: undefined });
      const metadataItems = fixture.nativeElement.querySelectorAll('.metadata-item');
      const texts = Array.from(metadataItems).map((el: any) => el.textContent);
      expect(texts.some((t: string) => t.includes('Work Mode'))).toBeFalse();
    });

    it('should render experience label from formatter', () => {
      setPositionAndDetect(mockPositionWithMetadata);
      const metadataItems = fixture.nativeElement.querySelectorAll('.metadata-item');
      const texts = Array.from(metadataItems).map((el: any) => el.textContent);
      expect(texts.some((t: string) => t.includes('3 – 5 years'))).toBeTrue();
    });

    it('should render location when present', () => {
      setPositionAndDetect(mockPositionWithMetadata);
      const metadataItems = fixture.nativeElement.querySelectorAll('.metadata-item');
      const texts = Array.from(metadataItems).map((el: any) => el.textContent);
      expect(texts.some((t: string) => t.includes('New York, NY'))).toBeTrue();
    });

    it('should omit location when absent', () => {
      setPositionAndDetect({ ...mockPositionWithMetadata, location: undefined });
      const metadataItems = fixture.nativeElement.querySelectorAll('.metadata-item');
      const texts = Array.from(metadataItems).map((el: any) => el.textContent);
      expect(texts.some((t: string) => t.includes('Location'))).toBeFalse();
    });

    it('should render skills as tags when array is non-empty', () => {
      setPositionAndDetect(mockPositionWithMetadata);
      const skillTags = fixture.nativeElement.querySelectorAll('.skill-tag');
      expect(skillTags.length).toBe(3);
      const skillTexts = Array.from(skillTags).map((el: any) => el.textContent.trim());
      expect(skillTexts).toEqual(['TypeScript', 'Angular', 'Node.js']);
    });

    it('should omit skills section when skills array is empty', () => {
      setPositionAndDetect({ ...mockPositionWithMetadata, skills: [] });
      expect(fixture.nativeElement.querySelector('.position-skills-section')).toBeNull();
    });

    it('should omit metadata section entirely when no metadata fields are set', () => {
      setPositionAndDetect(mockPosition);
      expect(fixture.nativeElement.querySelector('.position-metadata-section')).toBeNull();
      expect(fixture.nativeElement.querySelector('.position-skills-section')).toBeNull();
    });
  });
});
