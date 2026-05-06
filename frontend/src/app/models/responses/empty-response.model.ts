import { CustomResponse } from './custom-response.model';

export interface EmptyResponse extends CustomResponse {
  data: object;
}
