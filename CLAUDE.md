# CLAUDE.md — score-pion monorepo

## Structure

```
score-pion/
├── backend/          # Kotlin/Spring Boot API (score-pion-backend)
├── frontend/         # Angular SPA
├── docker/           # Shared infra compose files
├── .agents/          # Agent Skills (monorepo-wide)
├── .github/          # GitHub Actions workflows + PR templates
└── .gitlab/          # GitLab CI merge request templates
```

See `backend/CLAUDE.md` for backend development details.
See `frontend/CLAUDE.md` for frontend development details.

## Root-level commands

```bash
# Local development: Start the full stack
cp backend/.env.example backend/.env
# Edit backend/.env with your credentials, then:
docker-compose up

# Stop all containers
docker-compose down
```

## CI/CD

**GitHub Actions** (`.github/workflows/`) — runs on pull requests and pushes to main/master.
- `backend-ci.yml` — lints, tests, and builds on `backend/**` changes
- `backend-build-and-push.yml` — pushes Docker image on main/master push
- `frontend-ci.yml` — lints, tests, and builds on `frontend/**` changes

**GitLab CI** (`.gitlab-ci.yml`) — jobs are path-filtered with `rules: changes:`.
- Backend jobs: trigger on `backend/**`
- Frontend jobs: trigger on `frontend/**`

## Environment

Environment files are managed per service:

- **Backend:** Copy `backend/.env.example` → `backend/.env` and fill in credentials
- **Frontend:** Copy `frontend/src/environments/environment.example.ts` → `environment.ts` and `environment.prod.ts`

See `backend/AGENTS.md` and `frontend/CLAUDE.md` for detailed setup instructions.