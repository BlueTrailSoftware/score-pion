import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-gradient-page-header',
  standalone: true,
  templateUrl: './gradient-page-header.component.html',
  styleUrl: './gradient-page-header.component.scss',
})
export class GradientPageHeaderComponent {
  @Input() title!: string;
  @Input() subtitle!: string;
}
