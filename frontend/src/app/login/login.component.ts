import { Component, inject, OnInit } from '@angular/core';
import { LoginFormComponent } from './login-form/login-form.component';
import { WavesBackgroundComponent } from '../shared/waves-background/waves-background.component';
import { environment } from '../../environments/environment';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-login',
  imports: [LoginFormComponent, WavesBackgroundComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent implements OnInit {
  private userservice = inject(UserService);

  public gitHashFE: string | undefined;
  public gitHashBE: string | undefined;
  public version: string = `Version: ${environment.version}`;
  public showCompanyLogo = environment.branding?.showCompanyLogo ?? false;
  public companyLogoPath = environment.branding?.companyLogoPath ?? '';

  ngOnInit() {
    this.getGitHashFE();
    this.getGitHashBE();
  }

  /**
   * getGitHashFE method
   * gets the git hash from FE
   * @return {void}
   */
  private getGitHashFE(): void {
    this.userservice.getGitHashFE().subscribe((response: { hash: string | undefined }) => {
      this.gitHashFE = response.hash;
    });
  }

  /**
   * getGitHashBE method
   * gets the git hash from BE
   * @return {void}
   */
  private getGitHashBE(): void {
    this.userservice.getGitHashBE().subscribe((response: { git: { commit: { id: string } } }) => {
      this.gitHashBE = response.git.commit.id; // Adjust this based on your BE response structure
    });
  }
}
