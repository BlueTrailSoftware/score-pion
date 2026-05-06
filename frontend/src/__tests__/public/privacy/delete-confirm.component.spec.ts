import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { DeleteConfirmComponent } from '../../../app/public/privacy/delete-confirm.component';
import { CandidatesService } from '../../../app/services/candidates.service';

describe('DeleteConfirmComponent', () => {
  let component: DeleteConfirmComponent;
  let fixture: ComponentFixture<DeleteConfirmComponent>;
  let candidatesServiceSpy: jasmine.SpyObj<CandidatesService>;

  function createComponent(token: string | null = 'valid-token') {
    TestBed.configureTestingModule({
      imports: [DeleteConfirmComponent],
      providers: [
        { provide: CandidatesService, useValue: candidatesServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => token } } },
        },
      ],
    });

    fixture = TestBed.createComponent(DeleteConfirmComponent);
    component = fixture.componentInstance;
  }

  beforeEach(() => {
    candidatesServiceSpy = jasmine.createSpyObj('CandidatesService', ['confirmErasure']);
  });

  it('should create', () => {
    candidatesServiceSpy.confirmErasure.and.returnValue(of({ message: 'Deleted' }));
    createComponent();
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should confirm erasure and set success on valid token', fakeAsync(() => {
    candidatesServiceSpy.confirmErasure.and.returnValue(of({ message: 'Data deleted successfully' }));
    createComponent();
    fixture.detectChanges();
    tick();

    expect(candidatesServiceSpy.confirmErasure).toHaveBeenCalledWith('valid-token');
    expect(component.success).toBeTrue();
    expect(component.message).toBe('Data deleted successfully');
    expect(component.loading).toBeFalse();
  }));

  it('should set error when token is missing', () => {
    createComponent(null);
    fixture.detectChanges();

    expect(component.error).toBe('Invalid token. Please request a new deletion link.');
    expect(component.loading).toBeFalse();
    expect(candidatesServiceSpy.confirmErasure).not.toHaveBeenCalled();
  });

  it('should set error on API failure', fakeAsync(() => {
    candidatesServiceSpy.confirmErasure.and.returnValue(throwError(() => ({ status: 400 })));
    createComponent();
    fixture.detectChanges();
    tick();

    expect(component.error).toContain('Failed to process request');
    expect(component.loading).toBeFalse();
  }));
});
