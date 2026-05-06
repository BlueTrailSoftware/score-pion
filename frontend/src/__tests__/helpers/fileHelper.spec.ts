import { FileHelper } from '../../app/helpers/fileHelper';

function createMockFile(name: string, size: number, type: string): File {
  const content = new Array(size).fill('a').join('');
  return new File([content], name, { type });
}

describe('FileHelper', () => {
  describe('validateFile', () => {
    it('should return valid for an allowed PDF file', () => {
      const file = createMockFile('resume.pdf', 1024, 'application/pdf');
      expect(FileHelper.validateFile(file)).toEqual({ valid: true });
    });

    it('should return valid for an allowed DOCX file', () => {
      const file = createMockFile(
        'doc.docx',
        1024,
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      );
      expect(FileHelper.validateFile(file)).toEqual({ valid: true });
    });

    it('should return valid for a JPEG image', () => {
      const file = createMockFile('photo.jpg', 1024, 'image/jpeg');
      expect(FileHelper.validateFile(file)).toEqual({ valid: true });
    });

    it('should reject a disallowed MIME type', () => {
      const file = createMockFile('script.js', 100, 'application/javascript');
      const result = FileHelper.validateFile(file);
      expect(result.valid).toBeFalse();
      expect(result.error).toContain('File type not allowed');
    });

    it('should reject a file exceeding MAX_FILE_SIZE', () => {
      const file = createMockFile('big.pdf', FileHelper.MAX_FILE_SIZE + 1, 'application/pdf');
      const result = FileHelper.validateFile(file);
      expect(result.valid).toBeFalse();
      expect(result.error).toContain('too large');
    });

    it('should reject a file with disallowed extension even if MIME is valid', () => {
      const file = createMockFile('file.exe', 100, 'application/pdf');
      const result = FileHelper.validateFile(file);
      expect(result.valid).toBeFalse();
      expect(result.error).toContain('extension not allowed');
    });
  });

  describe('handleFileSelection', () => {
    it('should return the file when valid', () => {
      const file = createMockFile('resume.pdf', 1024, 'application/pdf');
      const event = { target: { files: [file], value: 'resume.pdf' } } as unknown as Event;

      const result = FileHelper.handleFileSelection(event);
      expect(result.file).toBe(file);
      expect(result.error).toBeUndefined();
    });

    it('should return null file and error when invalid', () => {
      const file = createMockFile('script.js', 100, 'application/javascript');
      const target = { files: [file], value: 'script.js' };
      const event = { target } as unknown as Event;

      const result = FileHelper.handleFileSelection(event);
      expect(result.file).toBeNull();
      expect(result.error).toContain('File type not allowed');
      expect(target.value).toBe('');
    });

    it('should return null file when no file is selected', () => {
      const event = { target: { files: [] } } as unknown as Event;
      const result = FileHelper.handleFileSelection(event);
      expect(result.file).toBeNull();
      expect(result.error).toBeUndefined();
    });

    it('should return null file when target is null', () => {
      const event = { target: null } as unknown as Event;
      const result = FileHelper.handleFileSelection(event);
      expect(result.file).toBeNull();
    });
  });

  describe('formatFileSize', () => {
    it('should return "0 Bytes" for 0', () => {
      expect(FileHelper.formatFileSize(0)).toBe('0 Bytes');
    });

    it('should format bytes', () => {
      expect(FileHelper.formatFileSize(500)).toBe('500 Bytes');
    });

    it('should format kilobytes', () => {
      expect(FileHelper.formatFileSize(1024)).toBe('1 KB');
    });

    it('should format megabytes', () => {
      expect(FileHelper.formatFileSize(1048576)).toBe('1 MB');
    });

    it('should format with decimals', () => {
      expect(FileHelper.formatFileSize(1536)).toBe('1.5 KB');
    });
  });

  describe('createFormData', () => {
    it('should create FormData with JSON data blob', () => {
      const data = { title: 'Test' };
      const formData = FileHelper.createFormData(data);

      expect(formData.has('data')).toBeTrue();
      expect(formData.has('file')).toBeFalse();
    });

    it('should include file when provided', () => {
      const data = { title: 'Test' };
      const file = createMockFile('resume.pdf', 100, 'application/pdf');
      const formData = FileHelper.createFormData(data, file);

      expect(formData.has('data')).toBeTrue();
      expect(formData.has('file')).toBeTrue();
    });
  });

  describe('getAcceptAttribute', () => {
    it('should return comma-separated extensions', () => {
      const result = FileHelper.getAcceptAttribute();
      expect(result).toBe('.pdf,.doc,.docx,.jpg,.jpeg,.png,.txt');
    });
  });

  describe('getAllowedTypesDescription', () => {
    it('should return uppercase extensions without dots', () => {
      const result = FileHelper.getAllowedTypesDescription();
      expect(result).toBe('PDF, DOC, DOCX, JPG, JPEG, PNG, TXT');
    });
  });

  describe('getMaxFileSizeDescription', () => {
    it('should return formatted max file size', () => {
      expect(FileHelper.getMaxFileSizeDescription()).toBe('10 MB');
    });
  });
});
