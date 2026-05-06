export class FileHelper {
  public static readonly ALLOWED_MIME_TYPES = [
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'image/jpeg',
    'image/png',
    'text/plain',
  ];

  public static readonly ALLOWED_EXTENSIONS = ['.pdf', '.doc', '.docx', '.jpg', '.jpeg', '.png', '.txt'];
  public static readonly MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

  /**
   * Validates a file against configured rules
   */
  public static validateFile(file: File): { valid: boolean; error?: string } {
    if (!this.ALLOWED_MIME_TYPES.includes(file.type)) {
      return {
        valid: false,
        error: 'File type not allowed. Allowed types: PDF, Word, JPG/PNG images, or plain text.',
      };
    }

    if (file.size > this.MAX_FILE_SIZE) {
      return {
        valid: false,
        error: `File is too large. Maximum size: ${this.MAX_FILE_SIZE / (1024 * 1024)}MB`,
      };
    }

    const fileName = file.name.toLowerCase();
    const hasValidExtension = this.ALLOWED_EXTENSIONS.some(ext => fileName.endsWith(ext));

    if (!hasValidExtension) {
      return { valid: false, error: 'File extension not allowed.' };
    }

    return { valid: true };
  }

  /**
   * Handles file selection event
   */
  public static handleFileSelection(event: Event): { file: File | null; error?: string } {
    const input = event.target as HTMLInputElement | null;
    const file = input?.files?.[0] ?? null;

    if (!file) return { file: null };

    const validation = this.validateFile(file);
    if (!validation.valid) {
      if (input) input.value = '';
      return { file: null, error: validation.error };
    }
    return { file };
  }

  /**
   * Formats file size for display
   */
  public static formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  /**
   * Creates FormData for file upload
   */
  public static createFormData(data: any, file?: File): FormData {
    const formData = new FormData();
    formData.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));

    if (file) {
      formData.append('file', file);
    }
    return formData;
  }

  /**
   * Returns accept attribute string for file input
   */
  public static getAcceptAttribute(): string {
    return this.ALLOWED_EXTENSIONS.join(',');
  }

  /**
   * Returns human-readable list of allowed file types
   */
  public static getAllowedTypesDescription(): string {
    const extensions = this.ALLOWED_EXTENSIONS.map(ext => ext.replace('.', '').toUpperCase());
    return extensions.join(', ');
  }

  /**
   * Returns formatted maximum file size
   */
  public static getMaxFileSizeDescription(): string {
    return this.formatFileSize(this.MAX_FILE_SIZE);
  }
}
