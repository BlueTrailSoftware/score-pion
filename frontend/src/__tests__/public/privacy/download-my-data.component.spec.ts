import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { DownloadMyDataComponent } from '../../../app/public/privacy/download-my-data.component';
import { CandidatesService } from '../../../app/services/candidates.service';
import { RecaptchaService } from '../../../app/services/recaptcha.service';

describe('DownloadMyDataComponent', () => {
  let component: DownloadMyDataComponent;
  let fixture: ComponentFixture<DownloadMyDataComponent>;
  let candidatesServiceSpy: jasmine.SpyObj<CandidatesService>;
  let recaptchaServiceSpy: jasmine.SpyObj<RecaptchaService>;

  beforeEach(async () => {
    candidatesServiceSpy = jasmine.createSpyObj('CandidatesService', ['createExportRequest']);
    recaptchaServiceSpy = jasmine.createSpyObj('RecaptchaService', ['executeRecaptcha']);
    recaptchaServiceSpy.executeRecaptcha.and.returnValue(Promise.resolve('mock-token'));

    await TestBed.configureTestingModule({
      imports: [DownloadMyDataComponent],
      providers: [
        { provide: CandidatesService, useValue: candidatesServiceSpy },
        { provide: RecaptchaService, useValue: recaptchaServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DownloadMyDataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call createExportRequest on valid submit', fakeAsync(() => {
    candidatesServiceSpy.createExportRequest.and.returnValue(of({ message: 'OK' }));

    component.requestForm.patchValue({ email: 'user@example.com' });
    component.onSubmit();
    tick();

    expect(recaptchaServiceSpy.executeRecaptcha).toHaveBeenCalledWith('download_data');
    expect(candidatesServiceSpy.createExportRequest).toHaveBeenCalledWith({
      email: 'user@example.com',
      captchaToken: 'mock-token',
    });
    expect(component.success).toBeTrue();
  }));

  it('should show error on failure', fakeAsync(() => {
    candidatesServiceSpy.createExportRequest.and.returnValue(throwError(() => ({ status: 500 })));

    component.requestForm.patchValue({ email: 'user@example.com' });
    component.onSubmit();
    tick();

    expect(component.error).toContain('Failed to send verification email');
  }));
});
