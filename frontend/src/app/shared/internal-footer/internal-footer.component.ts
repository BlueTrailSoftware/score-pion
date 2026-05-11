import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-internal-footer',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './internal-footer.component.html',
  styleUrl: './internal-footer.component.scss',
})
export class InternalFooterComponent {
  currentYear = new Date().getFullYear();
  companyName = environment.branding?.companyName || 'Score-Pion';
  companyUrl = environment.branding?.companyUrl || '#';
}
