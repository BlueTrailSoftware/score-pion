import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter, Router, ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { of, throwError } from 'rxjs';
import { ApplyFormComponent } from '../../../app/public/careers/apply-form.component';
import { CandidatesService } from '../../../app/services/candidates.service';
import { RecaptchaService } from '../../../app/services/recaptcha.service';
import { NotificationService } from '../../../app/services/notification.service';

describe('ApplyFormComponent', () => {
  let component: ApplyFormComponent;
  let fixture: ComponentFixture<ApplyFormComponent>;
  let candidatesServiceSpy: jasmine.SpyObj<CandidatesService>;
  let recaptchaServiceSpy: jasmine.SpyObj<RecaptchaService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let router: Router;
  let location: Location;

  beforeEach(async () => {
    candidatesServiceSpy = jasmine.createSpyObj('CandidatesService', ['apply']);
    recaptchaServiceSpy = jasmine.createSpyObj('RecaptchaService', ['executeRecaptcha']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    recaptchaServiceSpy.executeRecaptcha.and.returnValue(Promise.resolve('mock-token'));

    await TestBed.configureTestingModule({
      imports: [ApplyFormComponent],
      providers: [
        provideRouter([]),
        { provide: CandidatesService, useValue: candidatesServiceSpy },
        { provide: RecaptchaService, useValue: recaptchaServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => 'pos-1' } } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplyFormComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    location = TestBed.inject(Location);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set positionId from route params on init', () => {
    expect(component.positionId).toBe('pos-1');
  });

  describe('form validation', () => {
    it('should be invalid when empty', () => {
      expect(component.applyForm.invalid).toBeTrue();
    });

    it('should be invalid without gdprConsent', () => {
      component.applyForm.patchValue({
        name: 'Jane',
        email: 'jane@example.com',
        gdprConsent: false,
      });
      expect(component.applyForm.invalid).toBeTrue();
    });

    it('should be valid with required fields and consent', () => {
      component.applyForm.patchValue({
        name: 'Jane',
        email: 'jane@example.com',
        gdprConsent: true,
      });
      expect(component.applyForm.valid).toBeTrue();
    });
  });

  describe('goBack', () => {
    it('should call location.back()', () => {
      spyOn(location, 'back');
      component.goBack();
      expect(location.back).toHaveBeenCalled();
    });
  });

  describe('onFileSelected', () => {
    it('should set selectedFile for valid file', () => {
      const file = new File(['content'], 'resume.pdf', { type: 'application/pdf' });
      const event = { target: { files: [file], value: 'resume.pdf' } } as unknown as Event;
      component.onFileSelected(event);
      expect(component.selectedFile).toBe(file);
      expect(component.error).toBe('');
    });

    it('should set error for invalid file', () => {
      const file = new File(['content'], 'script.exe', { type: 'application/x-msdownload' });
      const event = { target: { files: [file], value: 'script.exe' } } as unknown as Event;
      component.onFileSelected(event);
      expect(component.selectedFile).toBeNull();
      expect(component.error).toBeTruthy();
    });
  });

  describe('onSubmit', () => {
    beforeEach(() => {
      component.applyForm.patchValue({
        name: 'Jane',
        email: 'jane@example.com',
        gdprConsent: true,
      });
    });

    it('should not submit when form is invalid', () => {
      component.applyForm.patchValue({ name: '' });
      component.onSubmit();
      expect(recaptchaServiceSpy.executeRecaptcha).not.toHaveBeenCalled();
    });

    it('should show error when neither file nor LinkedIn URL is provided', () => {
      component.selectedFile = null;
      component.applyForm.patchValue({ linkedinUrl: '' });
      component.onSubmit();
      expect(component.error).toContain('Please upload your CV');
    });

    it('should show error for invalid LinkedIn URL', () => {
      component.applyForm.patchValue({ linkedinUrl: 'not-a-url' });
      component.onSubmit();
      expect(component.error).toContain('valid LinkedIn profile URL');
    });

    it('should accept valid LinkedIn URL', fakeAsync(() => {
      candidatesServiceSpy.apply.and.returnValue(of({ status: 'success', message: 'OK' }));
      spyOn(router, 'navigate');

      component.applyForm.patchValue({ linkedinUrl: 'https://www.linkedin.com/in/janedoe' });
      component.onSubmit();
      tick();

      expect(recaptchaServiceSpy.executeRecaptcha).toHaveBeenCalledWith('apply');
      expect(candidatesServiceSpy.apply).toHaveBeenCalled();
      expect(router.navigate).toHaveBeenCalledWith(['/careers']);
      expect(notificationServiceSpy.success).toHaveBeenCalled();
    }));

    it('should submit with file when provided', fakeAsync(() => {
      candidatesServiceSpy.apply.and.returnValue(of({ status: 'success', message: 'OK' }));
      spyOn(router, 'navigate');

      component.selectedFile = new File(['content'], 'resume.pdf', { type: 'application/pdf' });
      component.onSubmit();
      tick();

      expect(candidatesServiceSpy.apply).toHaveBeenCalled();
      expect(component.loading).toBeFalse();
    }));

    it('should show CAPTCHA error on 400 with CAPTCHA message', fakeAsync(() => {
      candidatesServiceSpy.apply.and.returnValue(
        throwError(() => ({ status: 400, error: { message: 'CAPTCHA validation failed' } })),
      );

      component.selectedFile = new File(['content'], 'resume.pdf', { type: 'application/pdf' });
      component.onSubmit();
      tick();

      expect(component.error).toContain('Security validation failed');
    }));

    it('should show generic error on other failures', fakeAsync(() => {
      candidatesServiceSpy.apply.and.returnValue(
        throwError(() => ({ status: 500, error: { message: 'Server error' } })),
      );

      component.selectedFile = new File(['content'], 'resume.pdf', { type: 'application/pdf' });
      component.onSubmit();
      tick();

      expect(component.error).toBe('Server error');
    }));

    it('should handle recaptcha failure', fakeAsync(() => {
      recaptchaServiceSpy.executeRecaptcha.and.returnValue(Promise.reject(new Error('reCAPTCHA failed')));

      component.selectedFile = new File(['content'], 'resume.pdf', { type: 'application/pdf' });
      component.onSubmit();
      tick();

      expect(component.error).toContain('reCAPTCHA failed');
      expect(component.loading).toBeFalse();
    }));
  });
});
