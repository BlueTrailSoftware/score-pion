import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { DownloadConfirmComponent } from '../../../app/public/privacy/download-confirm.component';
import { CandidatesService } from '../../../app/services/candidates.service';
import { FileDownloadService } from '../../../app/services/file-download.service';

describe('DownloadConfirmComponent', () => {
  let component: DownloadConfirmComponent;
  let fixture: ComponentFixture<DownloadConfirmComponent>;
  let candidatesServiceSpy: jasmine.SpyObj<CandidatesService>;
  let fileDownloadServiceSpy: jasmine.SpyObj<FileDownloadService>;

  function createComponent(token: string | null = 'valid-token') {
    TestBed.configureTestingModule({
      imports: [DownloadConfirmComponent],
      providers: [
        { provide: CandidatesService, useValue: candidatesServiceSpy },
        { provide: FileDownloadService, useValue: fileDownloadServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => token } } },
        },
      ],
    });

    fixture = TestBed.createComponent(DownloadConfirmComponent);
    component = fixture.componentInstance;
  }

  beforeEach(() => {
    candidatesServiceSpy = jasmine.createSpyObj('CandidatesService', ['downloadExport']);
    fileDownloadServiceSpy = jasmine.createSpyObj('FileDownloadService', ['downloadBlob', 'generateFilename']);
    fileDownloadServiceSpy.generateFilename.and.returnValue('my_data_2024-01-01.json');
  });

  it('should create', () => {
    candidatesServiceSpy.downloadExport.and.returnValue(of(new Blob(['data'])));
    createComponent();
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should download export and trigger file download on valid token', fakeAsync(() => {
    const mockBlob = new Blob(['{"data":"test"}'], { type: 'application/json' });
    candidatesServiceSpy.downloadExport.and.returnValue(of(mockBlob));
    createComponent();
    fixture.detectChanges();
    tick();

    expect(candidatesServiceSpy.downloadExport).toHaveBeenCalledWith('valid-token');
    expect(component.success).toBeTrue();
    expect(fileDownloadServiceSpy.generateFilename).toHaveBeenCalledWith('my_data', 'json');
    expect(fileDownloadServiceSpy.downloadBlob).toHaveBeenCalledWith(mockBlob, 'my_data_2024-01-01.json');
  }));

  it('should set error when token is missing', () => {
    createComponent(null);
    fixture.detectChanges();

    expect(component.error).toBe('Invalid token. Please request a new download link.');
    expect(component.loading).toBeFalse();
  });

  it('should set error on API failure', fakeAsync(() => {
    candidatesServiceSpy.downloadExport.and.returnValue(throwError(() => ({ status: 400 })));
    createComponent();
    fixture.detectChanges();
    tick();

    expect(component.error).toContain('Failed to process request');
  }));
});
