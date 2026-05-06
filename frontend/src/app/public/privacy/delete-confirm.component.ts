import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';
import { PrivacyConfirmBaseComponent } from './privacy-confirm-base.component';
import { DeleteDataResponse } from '../../models/candidate.model';

@Component({
  selector: 'app-delete-confirm',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './delete-confirm.component.html',
  styleUrl: './delete-confirm.component.scss',
})
export class DeleteConfirmComponent extends PrivacyConfirmBaseComponent {
  message = '';

  protected processConfirmation(token: string): Observable<DeleteDataResponse> {
    return this.candidatesService.confirmErasure(token);
  }

  protected handleSuccess(response: DeleteDataResponse): void {
    this.message = response.message;
  }

  protected getInvalidTokenMessage(): string {
    return 'Invalid token. Please request a new deletion link.';
  }
}
