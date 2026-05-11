import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { PrivacyRequestBaseComponent } from './privacy-request-base.component';
import { DeleteDataResponse } from '../../models/candidate.model';

@Component({
  selector: 'app-download-my-data',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './download-my-data.component.html',
  styleUrl: './download-my-data.component.scss',
})
export class DownloadMyDataComponent extends PrivacyRequestBaseComponent {
  protected sendRequest(email: string, captchaToken: string): Observable<DeleteDataResponse> {
    return this.candidatesService.createExportRequest({ email, captchaToken });
  }

  protected getRecaptchaAction(): string {
    return 'download_data';
  }
}
