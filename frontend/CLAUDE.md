# Frontend — Claude Code Guide

This guide helps Claude Code users work effectively in the frontend directory.

## Setup

**First time only:**

```bash
cd frontend
npm run setup                    # Install deps and set up git hooks
```

**Environment configuration (required before running):**

```bash
# Copy the environment template
cp src/environments/environment.example.ts src/environments/environment.ts
cp src/environments/environment.example.ts src/environments/environment.prod.ts

# Edit both files and fill in:
# - googleSsoClientId: Your Google OAuth client ID
# - recaptchaSiteKey: Your reCAPTCHA v3 site key
# - apiUrl: Backend API URL (http://localhost:7070 for local dev)
```

See `frontend/README.md` for full setup details and configuration options.

## Development

| Command | Purpose |
|---------|---------|
| `npm start` | Start dev server at http://localhost:4200 |
| `npm run build` | Production build |
| `npm run lint` | Check code style |
| `npm run lint:fix` | Auto-fix code style |
| `npm run format` | Format code |
| `npm run test` | Run unit tests |

## Stack

- **Angular 17+** — Framework
- **TypeScript** — Language
- **SCSS** — Styles
- **Husky** — Git hooks

## Key Files

- `src/environments/` — Configuration files (Angular build-time env)
- `src/app/` — Application code
- `angular.json` — Build and dev configuration
- `package.json` — Dependencies and scripts

## Architecture Notes

- Env vars are baked into the Angular build (no runtime `.env` files)
- `environment.ts` is used for `ng serve` (local dev)
- `environment.prod.ts` is used for production builds
- Changes to env files require rebuild

## Common Tasks

**Add a new feature:**
1. Create components/services in `src/app/`
2. Follow existing naming and structure patterns
3. Write tests if adding business logic
4. Update environment files if new config needed

**Fix a bug:**
1. Write a test that reproduces the issue
2. Fix the code to pass the test
3. Run full test suite before committing

**Update dependencies:**
1. Update `package.json`
2. Run `npm install`
3. Test thoroughly (breaking changes are common)

## Linting & Code Style

```bash
npm run check      # Run all checks (lint, format, types)
npm run fix        # Fix auto-fixable issues
```

The project uses ESLint, Prettier, and TypeScript strict mode.

## See Also

- `frontend/README.md` — Full frontend documentation
- `../CONTRIBUTING.md` — Contribution guidelines
- `../AGENTS.md` — Tool-agnostic AI developer guide
