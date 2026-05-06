import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { PrivacyPolicyComponent } from '../../../app/public/privacy/privacy-policy.component';
import { environment } from '../../../environments/environment';

describe('PrivacyPolicyComponent', () => {
  let component: PrivacyPolicyComponent;
  let fixture: ComponentFixture<PrivacyPolicyComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrivacyPolicyComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(PrivacyPolicyComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    fixture.detectChanges();
    httpMock.expectOne('/assets/privacy-policy.md').flush('# Privacy');
    expect(component).toBeTruthy();
  });

  it('should load and render markdown with placeholders replaced', fakeAsync(() => {
    fixture.detectChanges();

    const markdown =
      'Contact us at {{privacyEmail}}. Company: {{companyLegalName}}. Retention: {{dataRetentionMonths}} months. URL: {{baseUrl}}';
    httpMock.expectOne('/assets/privacy-policy.md').flush(markdown);
    tick();

    expect(component.htmlContent).toContain(environment.branding.privacy.contactEmail);
    expect(component.htmlContent).toContain(environment.branding.legalName);
    expect(component.htmlContent).toContain(environment.branding.privacy.dataRetentionMonths.toString());
    expect(component.loading).toBeFalse();
  }));

  it('should set error when markdown fails to load', fakeAsync(() => {
    fixture.detectChanges();

    httpMock.expectOne('/assets/privacy-policy.md').error(new ProgressEvent('error'));
    tick();

    expect(component.error).toBe('Failed to load privacy policy');
    expect(component.loading).toBeFalse();
  }));
});
