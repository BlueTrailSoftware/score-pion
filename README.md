# Score Pion

Score Pion is an open-source candidate assessment management platform. It allows organizations to manage job positions, invite applicants, run technical assessments via Coderbyte, and track the full hiring pipeline — from invitation to decision.

## Repository Structure

```
/score-pion
├── /backend          Kotlin + Spring Boot WebFlux API (port 7070)
├── /frontend         Angular web application (port 4200)
├── /docker           Development infrastructure overrides
├── /docs             Internal documentation
├── docker-compose.yml
└── LICENSE
```

For detailed documentation see [`backend/AGENTS.md`](backend/AGENTS.md) and [`frontend/README.md`](frontend/README.md).

## Quick Start

### Prerequisites

- Docker and Docker Compose
- Accounts for required external services (see [Configuration](#configuration))

### For Local Development

```bash
git clone <repository-url>
cd score-pion

# Set up backend environment
cp backend/.env.example backend/.env
# Edit backend/.env and fill in your values

# Set up frontend environment
cp frontend/src/environments/environment.example.ts frontend/src/environments/environment.ts
cp frontend/src/environments/environment.example.ts frontend/src/environments/environment.prod.ts
# Edit both environment files and fill in Google OAuth and reCAPTCHA keys

# Start the full stack
docker-compose up
```

**Access services at:**

| Service | URL |
|---|---|
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:7070 |
| DynamoDB Admin UI | http://localhost:8001 |
| MinIO console | http://localhost:9001 |
| Mailpit (email) | http://localhost:8025 |

### For Production Deployment

```bash
# Set up production environment with real AWS credentials
cp backend/.env.production.example backend/.env.production
# Edit backend/.env.production with real AWS/service credentials

docker-compose up -d
```

## Environment Configuration

Score Pion uses **separate environment files** for local development and production:

- **`.env.local`** — Local development with DynamoDB Local, MinIO, and Mailpit
- **`.env.production`** — Production with AWS services (DynamoDB, S3, real SMTP)

See the `docs/` directory for detailed environment setup and configuration instructions.

## External Service Configuration

Provide credentials for the following services:

| Service | Purpose |
|---|---|
| Google OAuth | SSO authentication |
| reCAPTCHA | Bot protection on public forms |
| Brevo SMTP | Transactional email (invitations, notifications) |
| Coderbyte | Technical assessment platform |
| Asana | Ticket creation on application events |
| Google Chat | Internal and external webhook notifications |

> **Never commit `.env`, `.env.local`, or `.env.production` — they contain real credentials.**

### Note on MinIO

This project uses [MinIO](https://min.io) as a local S3-compatible object store for development. MinIO is licensed under [AGPLv3](https://www.gnu.org/licenses/agpl-3.0.html) and is used here as an infrastructure service. Score Pion's own source code remains under the MIT license.

## Development Setup

For backend-only or frontend-only development without Docker, see the per-package guides:

- [`backend/README.md`](backend/README.md) — Gradle, JDK 17, local DynamoDB setup
- [`frontend/README.md`](frontend/README.md) — Node, Angular CLI, environment configuration

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on reporting bugs, proposing features, and submitting merge requests.

## License

MIT — see [LICENSE](LICENSE).