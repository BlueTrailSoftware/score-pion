# Score Pion — AI Agent Developer Guide

This guide helps developers integrate AI tools and agents (Claude, GitHub Copilot, ChatGPT, Cursor, or any other AI-powered tool) into their workflow for the score-pion project.

## Overview

Score Pion is a monorepo with tool-agnostic developer guidance:

- **Backend developers:** See `backend/AGENTS.md` and `backend/DEVELOPER_GUIDE.md`
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

1. **Provide context** — Share the relevant `DEVELOPER_GUIDE.md` or module README with your AI tool
2. **Ask for code generation** — Request features, fixes, or documentation
3. **Follow conventions** — Let your AI tool know about code style and architecture patterns
4. **Test thoroughly** — AI-generated code needs review; always test before committing
5. **Update docs** — If your changes affect architecture or setup, update the relevant guide

### Example Workflows

#### Using Claude Code
```bash
# With Claude Code CLI
claude-code --work-dir ./backend
# Share backend/CLAUDE.md and backend/DEVELOPER_GUIDE.md for context
```

#### Using Cursor
- Paste `backend/DEVELOPER_GUIDE.md` into cursor rules
- Use `.cursorrules` to enforce project conventions

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
| **Backend** | `backend/AGENTS.md`, `backend/DEVELOPER_GUIDE.md` |
| **Frontend** | `frontend/README.md` |
| **Environment setup** | `CLAUDE.md` → Environment section |
| **Docker/Infra** | `docker-compose.yml`, `docker/` |
| **CI/CD** | `.github/workflows/`, `.gitlab-ci.yml` |

## Project Structure

```
score-pion/
├── backend/              # Kotlin/Spring Boot API
│   ├── AGENTS.md         # Backend AI developer guide
│   ├── DEVELOPER_GUIDE.md # Setup, architecture, testing
│   ├── AGENTS_SKILLS.md  # AI-powered tooling (if using Claude Code)
│   └── src/
├── frontend/             # Angular SPA
│   ├── README.md         # Frontend setup and conventions
│   └── src/
├── docker/               # Infra and compose files
├── docs/                 # Business logic and roles
├── CLAUDE.md             # Root-level Claude Code guide
└── AGENTS.md            # This file — tool-agnostic guide
```

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

## For Claude Code Users

If you're using Claude Code, see:
- `backend/CLAUDE.md` — Backend-specific skills and workflows
- `CLAUDE.md` — Project-level setup and CI/CD info

Additional resources:
- `/help` — Claude Code help
- `backend/AGENTS_SKILLS.md` — Specialized tasks and automation

## Questions?

- Check the relevant module's `DEVELOPER_GUIDE.md` first
- Review existing code for patterns and conventions
- Ask team members familiar with the module