import { CustomResponse } from './custom-response.model';
import { UserProfile } from '../user-profile.model';

export interface ProfileResponse extends CustomResponse {
  data: UserProfile;
}
