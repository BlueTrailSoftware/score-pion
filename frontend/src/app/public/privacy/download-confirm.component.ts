import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';
import { PrivacyConfirmBaseComponent } from './privacy-confirm-base.component';
import { FileDownloadService } from '../../services/file-download.service';

@Component({
  selector: 'app-download-confirm',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './download-confirm.component.html',
  styleUrl: './download-confirm.component.scss',
})
export class DownloadConfirmComponent extends PrivacyConfirmBaseComponent {
  private fileDownloadService = inject(FileDownloadService);

  protected processConfirmation(token: string): Observable<Blob> {
    return this.candidatesService.downloadExport(token);
  }

  protected handleSuccess(blob: Blob): void {
    const filename = this.fileDownloadService.generateFilename('my_data', 'json');
    this.fileDownloadService.downloadBlob(blob, filename);
  }

  protected getInvalidTokenMessage(): string {
    return 'Invalid token. Please request a new download link.';
  }
}
