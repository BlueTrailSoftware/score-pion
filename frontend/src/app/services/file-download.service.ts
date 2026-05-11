import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class FileDownloadService {
  downloadBlob(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  generateFilename(prefix: string, extension: string): string {
    const date = new Date().toISOString().split('T')[0];
    return `${prefix}_${date}.${extension}`;
  }
}
