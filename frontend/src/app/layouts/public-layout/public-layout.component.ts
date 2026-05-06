import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet, ActivatedRoute } from '@angular/router';
import { WavesBackgroundComponent } from '../../shared/waves-background/waves-background.component';
import { FooterComponent } from '../../shared/footer/footer.component';

@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [RouterOutlet, WavesBackgroundComponent, FooterComponent],
  templateUrl: './public-layout.component.html',
})
export class PublicLayoutComponent implements OnInit {
  private route = inject(ActivatedRoute);

  showFooter = true;

  ngOnInit(): void {
    // Read showFooter from route data; defaults to true if not specified
    const data = this.route.snapshot.data;
    this.showFooter = data['showFooter'] !== false;
  }
}
