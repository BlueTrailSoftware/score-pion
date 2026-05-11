import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { UserProfile } from '../../models/user-profile.model';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './topbar.component.html',
  styleUrls: ['./topbar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TopbarComponent implements OnInit {
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  userProfile: UserProfile | null = null;
  userPermissions: string | null = null;
  isLoading = true;
  imageLoadError = false;

  ngOnInit() {
    this.userPermissions = localStorage.getItem('permissions');
    this.loadUserProfile();
  }

  getHomeRoute(): string {
    return this.userPermissions === 'ADMIN' ? '/admin/candidates' : '/positions-manage';
  }

  private loadUserProfile(): void {
    const userId = localStorage.getItem('id');

    if (userId) {
      // Use cached version - will only make HTTP call once
      this.userService.getUser(userId).subscribe({
        next: response => {
          if (response.data) {
            this.userProfile = response.data;
          }
          this.isLoading = false;
          this.cdr.markForCheck(); // Manually trigger change detection for OnPush
        },
        error: error => {
          console.error('Error loading user profile:', error);
          this.isLoading = false;
          this.cdr.markForCheck();
          // If token is invalid, redirect to login
          if (error.status === 401) {
            this.authService.logout();
          }
        },
      });
    } else {
      this.isLoading = false;
      this.cdr.markForCheck();
    }
  }

  logout(): void {
    this.userService.logOutUser().subscribe({
      next: () => {
        this.authService.logout();
      },
      error: () => {
        // Even if logout fails, clear local storage and redirect
        this.authService.logout();
      },
    });
  }

  getUserInitials(): string {
    if (!this.userProfile?.name) {
      return this.userProfile?.email?.charAt(0).toUpperCase() || '?';
    }
    const names = this.userProfile.name.split(' ');
    if (names.length >= 2) {
      return (names[0].charAt(0) + names[names.length - 1].charAt(0)).toUpperCase();
    }
    return names[0].charAt(0).toUpperCase();
  }

  onImageError(): void {
    this.imageLoadError = true;
  }
}
