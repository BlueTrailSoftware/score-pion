import { TestBed } from '@angular/core/testing';
import { FileDownloadService } from '../../app/services/file-download.service';

describe('FileDownloadService', () => {
  let service: FileDownloadService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [FileDownloadService] });
    service = TestBed.inject(FileDownloadService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('generateFilename', () => {
    it("should generate a filename with today's date", () => {
      const today = new Date().toISOString().split('T')[0];
      expect(service.generateFilename('export', 'csv')).toBe(`export_${today}.csv`);
    });

    it('should use the provided prefix and extension', () => {
      const today = new Date().toISOString().split('T')[0];
      expect(service.generateFilename('report', 'pdf')).toBe(`report_${today}.pdf`);
    });
  });

  describe('downloadBlob', () => {
    it('should create an anchor element, set href and download, click it, and revoke the URL', () => {
      const mockUrl = 'blob:http://localhost/mock-url';
      const mockLink = jasmine.createSpyObj('a', ['click']);
      mockLink.href = '';
      mockLink.download = '';

      spyOn(window.URL, 'createObjectURL').and.returnValue(mockUrl);
      spyOn(window.URL, 'revokeObjectURL');
      spyOn(document, 'createElement').and.returnValue(mockLink);

      const blob = new Blob(['data'], { type: 'text/csv' });
      service.downloadBlob(blob, 'test.csv');

      expect(window.URL.createObjectURL).toHaveBeenCalledWith(blob);
      expect(mockLink.href).toBe(mockUrl);
      expect(mockLink.download).toBe('test.csv');
      expect(mockLink.click).toHaveBeenCalled();
      expect(window.URL.revokeObjectURL).toHaveBeenCalledWith(mockUrl);
    });
  });
});
