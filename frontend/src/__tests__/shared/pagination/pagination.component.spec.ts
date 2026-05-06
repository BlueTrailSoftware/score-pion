import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PaginationComponent } from '../../../app/shared/pagination/pagination.component';

describe('PaginationComponent', () => {
  let component: PaginationComponent;
  let fixture: ComponentFixture<PaginationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaginationComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PaginationComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should calculate total pages', () => {
    component.totalItems = 50;
    component.pageSize = 10;
    component.ngOnChanges({
      totalItems: { currentValue: 50, previousValue: 0, firstChange: true, isFirstChange: () => true },
    });
    expect(component.totalPages).toBe(5);
    expect(component.pages).toEqual([1, 2, 3, 4, 5]);
  });

  it('should ceil total pages', () => {
    component.totalItems = 51;
    component.pageSize = 10;
    component.ngOnChanges({
      totalItems: { currentValue: 51, previousValue: 0, firstChange: true, isFirstChange: () => true },
    });
    expect(component.totalPages).toBe(6);
  });

  describe('onPageChange', () => {
    beforeEach(() => {
      component.totalItems = 50;
      component.pageSize = 10;
      component.currentPage = 1;
      component.ngOnChanges({
        totalItems: { currentValue: 50, previousValue: 0, firstChange: true, isFirstChange: () => true },
      });
    });

    it('should emit pageChange for valid page', () => {
      spyOn(component.pageChange, 'emit');
      component.onPageChange(2);
      expect(component.pageChange.emit).toHaveBeenCalledWith(2);
    });

    it('should not emit for current page', () => {
      spyOn(component.pageChange, 'emit');
      component.onPageChange(1);
      expect(component.pageChange.emit).not.toHaveBeenCalled();
    });

    it('should not emit for out-of-range page', () => {
      spyOn(component.pageChange, 'emit');
      component.onPageChange(0);
      expect(component.pageChange.emit).not.toHaveBeenCalled();
    });

    it('should not emit when disabled', () => {
      component.disabled = true;
      spyOn(component.pageChange, 'emit');
      component.onPageChange(2);
      expect(component.pageChange.emit).not.toHaveBeenCalled();
    });
  });

  describe('shouldShowPage', () => {
    beforeEach(() => {
      component.totalItems = 100;
      component.pageSize = 10;
      component.currentPage = 5;
      component.ngOnChanges({
        totalItems: { currentValue: 100, previousValue: 0, firstChange: true, isFirstChange: () => true },
      });
    });

    it('should show first page', () => {
      expect(component.shouldShowPage(1)).toBeTrue();
    });
    it('should show last page', () => {
      expect(component.shouldShowPage(10)).toBeTrue();
    });
    it('should show current page', () => {
      expect(component.shouldShowPage(5)).toBeTrue();
    });
    it('should show neighbor pages', () => {
      expect(component.shouldShowPage(4)).toBeTrue();
      expect(component.shouldShowPage(6)).toBeTrue();
    });
    it('should hide distant pages', () => {
      expect(component.shouldShowPage(8)).toBeFalse();
    });
  });

  describe('shouldShowEllipsis', () => {
    it('should show ellipsis at currentPage-2 and currentPage+2', () => {
      component.currentPage = 5;
      expect(component.shouldShowEllipsis(3)).toBeTrue();
      expect(component.shouldShowEllipsis(7)).toBeTrue();
      expect(component.shouldShowEllipsis(4)).toBeFalse();
    });
  });
});
