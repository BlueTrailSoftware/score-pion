import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-google-sso',
  imports: [],
  templateUrl: './google-sso.component.html',
})
export class GoogleSsoComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  private readonly defaultRoutesByRole: Record<string, string> = {
    ADMIN: '/admin/candidates',
    RECRUITER: '/recruiter/candidates',
  };

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const id = params['id'];
      const token = params['token'];
      const permissions = params['permissions'];

      if (token) {
        localStorage.setItem('id', id.toString());
        localStorage.setItem('auth', token);
        localStorage.setItem('permissions', permissions);

        const defaultRoute = this.defaultRoutesByRole[permissions] || '/positions-manage';
        this.router.navigate([defaultRoute]);
      } else {
        this.router.navigate(['/login'], {
          queryParams: { gisError: params['error'] || 'No token received' },
        });
      }
    });
  }
}
