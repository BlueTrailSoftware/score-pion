import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TopbarComponent } from '../../shared/topbar/topbar.component';
import { SecondaryNavComponent } from '../../shared/secondary-nav/secondary-nav.component';
import { InternalFooterComponent } from '../../shared/internal-footer/internal-footer.component';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, TopbarComponent, SecondaryNavComponent, InternalFooterComponent],
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.scss'],
})
export class MainLayoutComponent {}
