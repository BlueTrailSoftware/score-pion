# Score Pion — AI Agent Developer Guide

This guide helps developers integrate AI tools and agents (Claude, GitHub Copilot, ChatGPT, Cursor, or any other AI-powered tool) into their workflow for the score-pion project.

## Overview

Score Pion is a monorepo with tool-agnostic developer guidance:

- **Backend developers:** See `backend/AGENTS.md` and `backend/src/` for architecture
- **Frontend developers:** See `frontend/README.md`
- **DevOps/Infra developers:** See Docker and CI/CD docs

All guidance is written to work with any AI agent or tool of your choice.

## Using AI Tools in This Project

### What's Documented

Each module includes:
- **Architecture overview** — How components fit together
- **Setup & build** — How to get the project running locally
- **Code conventions** — Naming, structure, and patterns to follow
- **Testing requirements** — What tests to write and how
- **External integrations** — API clients, credentials, environments

### How to Use This with Your AI Tool

1. **Provide context** — Share the relevant module `AGENTS.md` or README with your AI tool
2. **Ask for code generation** — Request features, fixes, or documentation
3. **Follow conventions** — Let your AI tool know about code style and architecture patterns
4. **Test thoroughly** — AI-generated code needs review; always test before committing
5. **Update docs** — If your changes affect architecture or setup, update the relevant guide

### Example Workflows

#### Using Claude Code
```bash
# With Claude Code CLI
claude-code --work-dir ./backend
# Claude Code will automatically use backend/AGENTS.md for context
```

#### Using Cursor
- Check `.cursorrules` for project conventions
- Reference `backend/AGENTS.md` and `CONTRIBUTING.md` for architecture and patterns

#### Using GitHub Copilot
- Enable Copilot in VS Code
- Reference code style and patterns from existing files
- Use chat to ask about architecture

#### Using any other tool
- Extract the relevant `DEVELOPER_GUIDE.md` or README
- Share code examples and conventions
- Ask your tool to follow the documented patterns

## Quick Links

| Topic | Location |
|-------|----------|
| **Agent Skills setup** | `scripts/setup-agent-skills.sh` (root), `backend/scripts/setup-agent-skills.sh` (backend) |
| **Backend guide** | `backend/AGENTS.md` |
| **Frontend guide** | `frontend/README.md` |
| **Environment setup** | `backend/.env.example`, `frontend/README.md` |
| **Contribution guidelines** | `CONTRIBUTING.md` |
| **Docker/Infra** | `docker-compose.yml`, `docs/` |
| **CI/CD** | `.github/workflows/`, `.gitlab-ci.yml` |

## Project Structure

```
score-pion/
├── .agents/                   # Agent Skills (monorepo-wide)
│   └── skills/
│       ├── sync-docs/         # Generate docs/roles/ and docs/flows/
│       ├── ticket/            # Asana ticket lifecycle
│       └── find-skills/       # Discover and install skills
│
├── backend/                   # Kotlin/Spring Boot API
│   ├── AGENTS.md              # Backend AI developer guide (security-review skill)
│   ├── .agents/skills/        # Backend-specific skills
│   │   └── security-review/   # OWASP/JWT/DynamoDB audit
│   ├── .env.example           # Backend configuration template
│   ├── .env.production.example # Production configuration template
│   ├── scripts/
│   │   └── setup-agent-skills.sh # Setup backend skills
│   └── src/
│
├── frontend/                  # Angular SPA
│   ├── README.md              # Frontend setup and conventions
│   ├── src/environments/      # Angular environment configuration
│   └── ...
│
├── docker/                    # Infra and compose file overrides
├── docs/                      # Internal documentation
├── scripts/
│   └── setup-agent-skills.sh  # Setup root-level skills
├── docker-compose.yml         # Full stack local development
└── AGENTS.md                  # This file — tool-agnostic guide
```

## Local Development

```bash
# Start the full stack
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

## Environment Setup

- **Backend:** Copy `backend/.env.example` → `backend/.env` and fill in credentials
- **Frontend:** Copy `frontend/src/environments/environment.example.ts` → `environment.ts` and `environment.prod.ts`

See `backend/README.md` for detailed backend setup instructions.

## Contributing

When adding features or fixes:

1. **Code** — Follow patterns in existing code
2. **Test** — Write tests (see module DEVELOPER_GUIDE)
3. **Document** — Update README or DEVELOPER_GUIDE if behavior changes
4. **Review** — Get code reviewed by team before merging

## Tips for Success

- **Read the architecture first** — Understanding the design prevents rework
- **Follow existing patterns** — Use existing code as examples, not documentation
- **Test thoroughly** — AI tools can generate code that looks good but has bugs
- **Communicate with your team** — Let them know you're using AI tools in your workflow
- **Keep secrets secret** — Never paste credentials, API keys, or internal URLs into public AI tools

## Agent Skills Setup

This project includes reusable Agent Skills — modular capabilities that extend AI agent functionality.

### Available Skills

**Root-level** (entire monorepo):
- `sync-docs` — Generate architectural documentation from code
- `ticket` — Manage Asana tickets tied to git branches
- `find-skills` — Discover and install skills from the open ecosystem

**Backend-specific** (Spring Boot + Kotlin):
- `security-review` — OWASP/JWT/DynamoDB vulnerability audit

### Setup (One-Time)

**Claude Code:**
```bash
# Setup root-level skills
bash scripts/setup-agent-skills.sh

# Setup backend-specific skills (if working in backend/)
bash backend/scripts/setup-agent-skills.sh
```

**Other tools** (Cursor, Copilot, Windsurf, Cline, etc.):
- Skills are in `.agents/skills/` — your tool reads them automatically
- No setup script needed

---

## Questions?

- Check the relevant module's `DEVELOPER_GUIDE.md` first
- Review existing code for patterns and conventions
- Ask team members familiar with the module