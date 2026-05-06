export interface HealthCheckResponse {
  git: {
    commit: {
      id: string;
      time: string;
    };
    branch: string;
  };
  build: {
    artifact: string;
    name: string;
    time: string;
    version: string;
    group: string;
  };
}
