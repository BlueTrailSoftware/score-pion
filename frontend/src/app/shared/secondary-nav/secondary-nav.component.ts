import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

interface NavTab {
  label: string;
  icon: string;
  route: string;
  requiredPermissions?: string[];
}

@Component({
  selector: 'app-secondary-nav',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './secondary-nav.component.html',
  styleUrls: ['./secondary-nav.component.scss'],
})
export class SecondaryNavComponent implements OnInit {
  userPermissions: string | null = null;

  private readonly allTabs: NavTab[] = [
    {
      label: 'Review Candidates',
      icon: 'bi-person-check',
      route: '/admin/candidates',
      requiredPermissions: ['ADMIN'],
    },
    {
      label: 'Review Candidates',
      icon: 'bi-person-check',
      route: '/recruiter/candidates',
      requiredPermissions: ['RECRUITER'],
    },
    {
      label: 'Manage Positions',
      icon: 'bi-briefcase',
      route: '/positions-manage',
    },
    {
      label: 'Manage Team',
      icon: 'bi-people',
      route: '/admin/manage-team',
      requiredPermissions: ['ADMIN'],
    },
    {
      label: 'Global Recipients',
      icon: 'bi-envelope',
      route: '/admin/global-recipients',
      requiredPermissions: ['ADMIN'],
    },
  ];

  get visibleTabs(): NavTab[] {
    return this.allTabs.filter(
      tab => !tab.requiredPermissions || tab.requiredPermissions.includes(this.userPermissions ?? ''),
    );
  }

  ngOnInit(): void {
    this.userPermissions = localStorage.getItem('permissions');
  }
}
