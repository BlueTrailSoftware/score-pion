import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { DeleteMyDataComponent } from '../../../app/public/privacy/delete-my-data.component';
import { CandidatesService } from '../../../app/services/candidates.service';
import { RecaptchaService } from '../../../app/services/recaptcha.service';

describe('DeleteMyDataComponent', () => {
  let component: DeleteMyDataComponent;
  let fixture: ComponentFixture<DeleteMyDataComponent>;
  let candidatesServiceSpy: jasmine.SpyObj<CandidatesService>;
  let recaptchaServiceSpy: jasmine.SpyObj<RecaptchaService>;

  beforeEach(async () => {
    candidatesServiceSpy = jasmine.createSpyObj('CandidatesService', ['createErasureRequest']);
    recaptchaServiceSpy = jasmine.createSpyObj('RecaptchaService', ['executeRecaptcha']);
    recaptchaServiceSpy.executeRecaptcha.and.returnValue(Promise.resolve('mock-token'));

    await TestBed.configureTestingModule({
      imports: [DeleteMyDataComponent],
      providers: [
        { provide: CandidatesService, useValue: candidatesServiceSpy },
        { provide: RecaptchaService, useValue: recaptchaServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DeleteMyDataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not submit when form is invalid', () => {
    component.onSubmit();
    expect(recaptchaServiceSpy.executeRecaptcha).not.toHaveBeenCalled();
  });

  it('should call createErasureRequest on valid submit', fakeAsync(() => {
    candidatesServiceSpy.createErasureRequest.and.returnValue(of({ message: 'OK' }));

    component.requestForm.patchValue({ email: 'user@example.com' });
    component.onSubmit();
    tick();

    expect(recaptchaServiceSpy.executeRecaptcha).toHaveBeenCalledWith('delete_data');
    expect(candidatesServiceSpy.createErasureRequest).toHaveBeenCalledWith({
      email: 'user@example.com',
      captchaToken: 'mock-token',
    });
    expect(component.success).toBeTrue();
    expect(component.loading).toBeFalse();
  }));

  it('should show CAPTCHA error on 400 with CAPTCHA message', fakeAsync(() => {
    candidatesServiceSpy.createErasureRequest.and.returnValue(
      throwError(() => ({ status: 400, error: { message: 'CAPTCHA failed' } })),
    );

    component.requestForm.patchValue({ email: 'user@example.com' });
    component.onSubmit();
    tick();

    expect(component.error).toContain('Security validation failed');
  }));

  it('should show generic error on other failures', fakeAsync(() => {
    candidatesServiceSpy.createErasureRequest.and.returnValue(throwError(() => ({ status: 500 })));

    component.requestForm.patchValue({ email: 'user@example.com' });
    component.onSubmit();
    tick();

    expect(component.error).toContain('Failed to send verification email');
  }));

  it('should handle recaptcha failure', fakeAsync(() => {
    spyOn(console, 'error');
    recaptchaServiceSpy.executeRecaptcha.and.returnValue(Promise.reject(new Error('fail')));

    component.requestForm.patchValue({ email: 'user@example.com' });
    component.onSubmit();
    tick();

    expect(component.error).toContain('Security validation failed');
    expect(component.loading).toBeFalse();
  }));
});
