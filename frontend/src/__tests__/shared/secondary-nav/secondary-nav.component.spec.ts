import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { SecondaryNavComponent } from '../../../app/shared/secondary-nav/secondary-nav.component';

describe('SecondaryNavComponent', () => {
  let component: SecondaryNavComponent;
  let fixture: ComponentFixture<SecondaryNavComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecondaryNavComponent],
      providers: [provideRouter([])],
    }).compileComponents();

    localStorage.clear();
  });

  afterEach(() => localStorage.clear());

  function createComponent(permissions?: string) {
    if (permissions) localStorage.setItem('permissions', permissions);
    fixture = TestBed.createComponent(SecondaryNavComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('should create', () => {
    createComponent();
    expect(component).toBeTruthy();
  });

  it('should show admin tabs for ADMIN user', () => {
    createComponent('ADMIN');
    const labels = component.visibleTabs.map(t => t.label);
    expect(labels).toContain('Manage Team');
    expect(labels).toContain('Review Candidates');
    expect(labels).toContain('Global Recipients');
  });

  it('should show recruiter-specific tabs for RECRUITER user', () => {
    createComponent('RECRUITER');
    const labels = component.visibleTabs.map(t => t.label);
    expect(labels).toContain('Manage Positions');
    expect(labels).not.toContain('Manage Team');
  });

  it('should show only non-restricted tabs when no permissions', () => {
    createComponent();
    const labels = component.visibleTabs.map(t => t.label);
    expect(labels).toContain('Manage Positions');
    expect(labels).not.toContain('Manage Team');
    expect(labels).not.toContain('Review Candidates');
  });
});
