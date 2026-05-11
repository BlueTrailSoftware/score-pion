# Score Pion — Backend

Spring Boot + Kotlin WebFlux API for the Score Pion candidate assessment platform.
Runs on port **7070** by default.

## Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 1.9 |
| Framework | Spring Boot 3.2 + WebFlux (reactive) |
| Database | Amazon DynamoDB (single-table design) |
| File storage | AWS S3 / MinIO (local dev) |
| Auth | Google OAuth + Auth0 (JWT) |
| Email | Brevo SMTP |
| Build | Gradle 8 |
| Runtime | JDK 17 |

## Prerequisites

- **JDK 17+** — [Download Temurin](https://adoptium.net)
- **Docker + Docker Compose** — required for DynamoDB Local, MinIO, and Mailpit
- **ngrok** (optional) — for testing Coderbyte and Asana webhooks locally

## Quick Setup

```bash
# 1. Copy the environment template and fill in your values
cp backend/.env.example backend/.env

# 2. Start the full stack from the repo root
docker-compose up

# 3. Initialize DynamoDB tables (first time only)
bash backend/scripts/init-dynamodb-local.sh
```

See [DYNAMODB_SETUP.md](DYNAMODB_SETUP.md) for detailed DynamoDB local setup instructions.

## Environment Variables

Copy `backend/.env.example` to `backend/.env`. The example file has safe placeholder values
for all local development variables so the app starts without real credentials.

For production, copy `backend/.env.production.example` to `backend/.env.production`
and fill in real AWS, Auth0, Brevo, and other service credentials.

> Never commit `.env` or `.env.production` — they are gitignored.

## Development Commands

```bash
# Run the backend locally (requires DynamoDB and MinIO running via Docker)
./gradlew bootRun

# Run all tests
./gradlew test

# Full build (compile + test)
./gradlew build

# Static analysis
./gradlew detekt

# Build Docker image
./gradlew bootBuildImage
```

## API Documentation

Swagger UI is available at **http://localhost:7070/swagger-ui.html** when the server is running.
The OpenAPI spec is at `http://localhost:7070/v3/api-docs`.

## Scripts

Operational scripts are in `backend/scripts/`:

| Script | Purpose |
|---|---|
| `init-dynamodb-local.sh` | Create DynamoDB tables for local development |
| `add-admin-user.sh` | Insert an admin user into DynamoDB |
| `setup-ngrok.sh` / `.ps1` | Configure ngrok tunnel for webhook testing |
| `start-ngrok.sh` / `.ps1` | Start the ngrok tunnel |
| `setup-asana-webhook.sh` / `.ps1` | Register the Asana webhook endpoint |
| `deploy.sh` / `.ps1` | SSH-based deployment to production server |

## Architecture

The backend follows **Clean Hexagonal Architecture** with three layers:

```
src/main/kotlin/org/example/notifier/
├── domain/          Entities, value objects, repository interfaces (ports)
├── application/     Use cases, services, application models
└── infrastructure/  Controllers, persistence adapters, external clients, config
```

Key design decisions:
- Fully reactive with Kotlin Coroutines (`suspend` functions throughout)
- Single-table DynamoDB with PK/SK patterns + 2 GSIs
- Command/Query pattern for use cases
- GDPR: automatic anonymization after 9 months, data export/erasure endpoints

## Agent Skills

This backend uses [Agent Skills](https://agentskills.io) for AI-assisted development.
See [AGENTS.md](AGENTS.md) for available skills and setup instructions.

```bash
# One-time setup for Claude Code users
bash backend/scripts/setup-agent-skills.sh
```

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) in the repository root.
