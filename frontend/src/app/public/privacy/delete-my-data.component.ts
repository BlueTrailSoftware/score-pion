import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { PrivacyRequestBaseComponent } from './privacy-request-base.component';
import { DeleteDataResponse } from '../../models/candidate.model';

@Component({
  selector: 'app-delete-my-data',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './delete-my-data.component.html',
  styleUrl: './delete-my-data.component.scss',
})
export class DeleteMyDataComponent extends PrivacyRequestBaseComponent {
  protected sendRequest(email: string, captchaToken: string): Observable<DeleteDataResponse> {
    return this.candidatesService.createErasureRequest({ email, captchaToken });
  }

  protected getRecaptchaAction(): string {
    return 'delete_data';
  }
}
