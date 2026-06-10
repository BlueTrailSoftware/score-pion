# Contributing to Score Pion

Thank you for your interest in contributing. This guide explains how to get involved effectively — whether by reporting bugs, proposing features, or submitting code.

> **Note:** Contributions are reviewed by the core team and must align with the product roadmap and architectural decisions.

---

## Table of Contents

- [Ways to Contribute](#ways-to-contribute)
- [Before You Start](#before-you-start)
- [Reporting Bugs](#reporting-bugs)
- [Proposing Features](#proposing-features)
- [Setting Up the Development Environment](#setting-up-the-development-environment)
- [Code Contribution Workflow](#code-contribution-workflow)
- [Coding Standards](#coding-standards)
- [Commit Message Convention](#commit-message-convention)
- [Pull Request Guidelines](#pull-request-guidelines)
- [Testing Requirements](#testing-requirements)
- [Review Process](#review-process)
- [Architecture Overview](#architecture-overview)

---

## Ways to Contribute

Contributing is not limited to writing code. You can help by:

- **Reporting bugs** — clear, reproducible bug reports are extremely valuable.
- **Improving documentation** — fixing inaccuracies, clarifying flows, or adding missing context in `docs/`.
- **Writing tests** — increasing coverage for untested use cases or edge cases.
- **Reviewing pull requests** — providing constructive feedback on open PRs.
- **Implementing features** — only after the proposal has been accepted (see [Proposing Features](#proposing-features)).

If you are unsure where to start, look for issues labeled `good first issue` or `help wanted`.

---

## Before You Start

To avoid wasted effort, follow these steps before writing any code:

1. **Search existing issues** to verify the bug or feature has not already been reported or is in progress.
2. **Read the relevant documentation** in `docs/roles/` and `docs/flows/` to understand intended behavior before assuming something is a bug.
3. **Open an issue first** for non-trivial changes. Submitting a large PR without prior discussion risks rejection if it conflicts with architectural decisions or product direction.

The core team reserves the right to close contributions that do not align with the project goals, regardless of code quality.

---

## Reporting Bugs

A good bug report allows the team to reproduce and understand the problem without needing follow-up questions.

### Before reporting

- Confirm the bug is reproducible on the latest version of `main`.
- Check whether the issue has already been reported.
- If the behavior is described in `docs/flows/`, verify you are not misunderstanding expected behavior.

### What to include

- **Environment:** OS, Java version, Docker version if relevant.
- **Steps to reproduce:** Minimal, numbered steps from a clean state.
- **Expected behavior:** What should happen according to the documented flow.
- **Actual behavior:** What happens instead, including full stack traces or error logs.
- **Configuration:** Sanitized `application.properties` excerpts if relevant — never include credentials.

### What not to report as bugs

- Behavior that is intentional but undocumented — open a documentation issue instead.
- Integration-specific issues (Coderbyte, Auth0, Brevo) that are outside the scope of this service.
- Issues that only reproduce with custom or unsupported configurations.

---

## Proposing Features

Feature proposals must be submitted as issues before any implementation begins.

### Guidelines

- Clearly describe the **problem** the feature solves, not just the solution.
- Explain how the feature fits into an existing role or flow defined in `docs/roles/` and `docs/flows/`.
- Consider the impact on the DynamoDB single-table design — new access patterns may require GSI changes.
- Proposals that require breaking changes to the API or database schema require additional justification.

The team will label the issue `accepted`, `needs discussion`, or `out of scope`. **Do not begin implementation until the issue is labeled `accepted`.**

---

## Setting Up the Development Environment

### Prerequisites

- **JDK 17** (for backend)
- **Node.js 20+** (for frontend)
- **Docker and Docker Compose**
- **Git** and a GitHub account

### Local setup

**Option 1: Full Stack with Docker Compose (recommended)**

Best for working on both backend and frontend simultaneously.

```bash
# 1. Clone the repository
git clone <repository-url>
cd score-pion

# 2. Set up backend environment
cp backend/.env.example backend/.env
# Edit backend/.env with your credentials (see backend/README.md for details)

# 3. Set up frontend environment
cp frontend/src/environments/environment.example.ts frontend/src/environments/environment.ts
cp frontend/src/environments/environment.example.ts frontend/src/environments/environment.prod.ts
# Edit both files with your API keys (Google OAuth, reCAPTCHA, etc.)

# 4. Start the full stack
docker-compose up
# Backend available at http://localhost:7070
# Frontend available at http://localhost:4200
```

**Option 2: Backend only (without Docker)**

For backend-focused work with local services.

```bash
# 1. Clone and navigate to backend
git clone <repository-url>
cd score-pion/backend

# 2. Set up environment
cp .env.example .env
# Edit .env with values (use local services: DynamoDB Local, MinIO, Mailpit)

# 3. Start local services (from project root)
docker-compose up dynamodb-local init-dynamodb minio create-buckets mailpit

# 4. Run the backend application (from backend/ directory)
./gradlew bootRun
# Backend available at http://localhost:7070
```

**Option 3: Frontend only**

For frontend-focused work, assuming backend is already running (either locally or remote).

```bash
# 1. Navigate to frontend directory
cd score-pion/frontend

# 2. Install dependencies
npm install

# 3. Set up environment
cp src/environments/environment.example.ts src/environments/environment.ts
# Edit environment.ts with your API base URL and keys

# 4. Start the development server
npm start
# Frontend available at http://localhost:4200
```

### Environment variables and credentials

See [README.md](README.md#environment-configuration) for detailed environment setup and credential requirements. 

**Important:** Never commit `.env` files, `environment.ts` files with real credentials, or any other secrets to version control. All credentials must be managed outside the repository.

---

## Code Contribution Workflow

```
main
 └── feature/short-description     ← your branch
      └── (commits)
           └── Pull Request → main
```

1. **Create a branch** from `main` using the naming convention below.
2. **Make focused commits** — one logical change per commit.
3. **Open a Pull Request** on GitHub against `main`.
4. **Address review feedback** — push follow-up commits; do not force-push after review has begun.
5. A maintainer will squash-merge or rebase once approved.

### Branch naming

| Type | Pattern | Example |
|---|---|---|
| Feature | `feature/short-description` | `feature/recruiter-filter-by-status` |
| Bug fix | `fix/short-description` | `fix/invitation-expiry-check` |
| Documentation | `docs/short-description` | `docs/gdpr-flow-clarification` |
| Refactor | `refactor/short-description` | `refactor/applicant-mapper` |

---

## Coding Standards

All code must follow the standards specific to its stack. See the detailed guides below, then refer to `backend/README.md` and `frontend/README.md` for stack-specific conventions.

### General principles (all stacks)

- **Layer separation** — do not leak implementation details across architectural boundaries.
- **Naming clarity** — function and variable names must be self-documenting.
- **Single responsibility** — one function/component should have one reason to change.
- **DRY principle** — avoid duplication; extract reusable logic appropriately.
- **Comments only for the non-obvious** — document *why*, not *what*. Routine code should be self-explanatory.

### Backend (Kotlin / Spring Boot WebFlux)

The backend uses **Kotlin** with **Spring Boot WebFlux** and **Coroutines**. Follow [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) layer boundaries strictly:

| Layer | Package | Rule |
|---|---|---|
| Domain | `domain/` | Pure Kotlin. No framework dependencies. |
| Application | `application/` | Use cases and services. No HTTP or persistence types. |
| Infrastructure | `infrastructure/` | Controllers, persistence, external integrations. |

**Do not leak infrastructure types** (HTTP DTOs, DynamoDB items) into the application or domain layers.

#### Mappers

Use explicit **Mapper classes** — never map inline inside controllers or use cases. Organize by translation boundary:

| Location | Purpose |
|---|---|
| `infrastructure/dto/mapper/` | Use case result → HTTP response DTO |
| `infrastructure/persistence/mapper/` | Domain entity → DynamoDB item (and back) |
| `infrastructure/client/mapper/` | External API response → domain types |
| `infrastructure/adapter/*/mapper/` | Adapter-specific translations (e.g., Coderbyte) |

#### Reactive model

All service and repository functions must be `suspend`. Blocking calls are not acceptable in the reactive pipeline.

#### Logging

Inject `LoggerPort` via constructor — do **not** use `LoggerFactory` or SLF4J annotations directly.

```kotlin
// Correct
class MyService(private val logger: LoggerPort) {
    suspend fun doSomething() {
        logger.info("doing something")
    }
}

// Wrong
class MyService {
    private val logger = LoggerFactory.getLogger(MyService::class.java)
}
```

#### Style limits

| Rule | Limit |
|---|---|
| Line length | 120 characters |
| Function length | 60 lines |
| Cyclomatic complexity | 15 per function |
| Nested block depth | 4 levels |

#### Static analysis

All backend code must pass Detekt with zero issues before a PR will be reviewed:

```bash
./gradlew detekt
```

The CI pipeline will reject PRs that fail linting.

### Frontend (Angular / TypeScript)

The frontend uses **Angular** with **TypeScript**. Follow these conventions:

- **Component organization** — one component per file; keep templates, styles, and logic together in the component folder.
- **Smart/dumb components** — separate container components (handle state/logic) from presentational components (UI-focused).
- **Service injection** — use constructor injection for all dependencies; services should be singletons.
- **Reactive patterns** — leverage RxJS Observables; avoid mutable state when possible.
- **Strong typing** — always use TypeScript interfaces; avoid `any` type.

#### Style limits

| Rule | Limit |
|---|---|
| Line length | 120 characters |
| Function/method length | 40 lines |
| Component template lines | 50 lines (extract to separate components if longer) |

#### Linting and formatting

All frontend code must pass linting before a PR will be reviewed:

```bash
cd frontend
npm run lint
```

The CI pipeline will reject PRs that fail linting or have unformatted code.

---

## Automated Security Checks

Every pull request is scanned automatically before review begins:

| Check | Tool | What it catches |
|---|---|---|
| Static analysis (Kotlin) | Detekt | Code quality, potential bugs, unsafe patterns |
| Static analysis (TS/JS) | ESLint | Type safety, Angular anti-patterns |
| SAST — Kotlin/Java | CodeQL (`security-extended`) | Injection, path traversal, crypto misuse |
| SAST — TypeScript | CodeQL (`security-extended`) | XSS, prototype pollution, injection |
| Secret detection | GitHub Secret Scanning | Accidentally committed credentials or API keys |
| Dependency CVEs | npm audit (`--audit-level=high`) | Known vulnerabilities in frontend packages |
| Dependency updates | Dependabot | Weekly PRs for outdated dependencies |

**PRs that fail any of these checks will not be merged.** If a check flags a false positive, explain it in the PR description — do not disable or bypass the tool.

To report a security vulnerability in the project itself, see [SECURITY.md](SECURITY.md) — use private reporting, not a public issue.

---

## Commit Message Convention

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <short summary>

[optional body — explain why, not what]

[optional footer — Closes #42]
```

### Types

| Type | When to use |
|---|---|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `refactor` | Code restructure without behavior change |
| `test` | Adding or correcting tests |
| `chore` | Build scripts, CI, dependency updates |

### Examples

```
feat(applicant): add filter by assessment status

fix(invitation): correct expiry date calculation for extended deadlines

docs(flows): clarify GDPR anonymization trigger conditions

test(useCases): add missing edge cases for position deactivation
```

### Rules

- Use the **imperative mood**: "add", "fix", "update" — not "added", "fixes", "updating".
- Keep the summary under **72 characters**.
- Reference the GitHub issue in the body when applicable: `Closes #42`.
- Do not reference internal Asana ticket IDs in commit messages.

---

## Pull Request Guidelines

### Scope

- Each PR should address a **single concern**. Avoid bundling unrelated changes.
- If a fix requires a refactor, split them into separate PRs unless the refactor is trivially small.
- PRs must not include commented-out code, debug logs, or temporary workarounds unless explicitly marked with a `TODO` and a linked issue.
- PRs affecting both backend and frontend are acceptable **only if tightly coupled** (e.g., API contract change + frontend integration). Otherwise, split into separate PRs.

### Description template

When opening a PR, fill in:

```
## What
Brief description of what this PR changes.

## Why
The problem or requirement being addressed. Link to the issue.

## How
Key implementation decisions, especially if non-obvious.

## Testing
How the change was tested (unit tests, manual verification steps, screenshots for UI changes).

## Checklist (Backend)
- [ ] Detekt passes (`./gradlew detekt`)
- [ ] Tests pass (`./gradlew test`)
- [ ] No credentials or environment-specific values committed
- [ ] Docs updated if behavior changed

## Checklist (Frontend)
- [ ] Linting passes (`npm run lint`)
- [ ] Tests pass (`npm run test`)
- [ ] No credentials or environment-specific values committed
- [ ] Responsive design verified (if UI changes)
- [ ] Docs updated if behavior changed

## Checklist (Both stacks)
- [ ] No secrets or environment files committed
- [ ] Branch is up to date with `main`
```

### Size guidance

| Lines changed | Expectation |
|---|---|
| < 200 | Reviewed quickly |
| 200–500 | Provide clear context in the description |
| > 500 | Split if possible; expect a longer review cycle |

**Note:** Frontend changes tend to be larger due to template code. A 400-line UI feature is acceptable; a 400-line backend refactor should be split.

---

## Testing Requirements

All new code — backend and frontend — must have corresponding unit tests. Tests must be deterministic and must not depend on external services or network calls.

### Backend (Kotlin)

- Tests live in `src/test/kotlin/` mirroring the main source structure.
- Use **JUnit 5** and **Mockito-Kotlin** for mocking.
- Test behavior and outcomes, not mock interactions.
- All new use cases and services must have unit test coverage.

```bash
# Run all tests
./gradlew test

# Run a specific test
./gradlew test --tests "org.example.notifier.application.useCases.MyUseCaseTest.shouldReturnErrorWhenNotFound"

# Run with coverage report
./gradlew test jacocoTestReport
```

### Frontend (Angular / TypeScript)

- Tests live in `src/app/` alongside the components/services they test (`.spec.ts` files).
- Use **Jasmine** and **Karma** for unit testing; **Protractor** or **Cypress** for E2E tests (if applicable).
- Test component interaction, service logic, and user workflows.
- Mock HTTP requests using `HttpClientTestingModule` and `HttpTestingController`.

```bash
cd frontend

# Run all unit tests
npm run test

# Run unit tests with coverage
npm run test:coverage

# Run E2E tests (if configured)
npm run e2e
```

### General testing rules

- Do not write tests that only verify mocks were called — test behavior and outcomes.
- Tests must pass deterministically; avoid flaky tests that depend on timing or external state.
- Aim for meaningful coverage of business logic and edge cases, not 100% line coverage.

**The CI pipeline runs all tests on every PR. PRs with failing tests will not be merged.**

---

## Review Process

### What to expect

- The team aims to provide an initial response within **3 business days**.
- Reviewers may request changes, ask questions, or reject the PR with an explanation.
- An approved PR does not guarantee immediate merge — it may wait for a release window or dependent changes.

### Responding to feedback

- Address each comment explicitly — implement the change or explain why you disagree.
- Do not resolve reviewer comments yourself; let the reviewer dismiss them after verifying.
- Push follow-up commits rather than amending history once review has started, to preserve the review thread.

### Grounds for rejection

| Reason | Detail |
|---|---|
| Architecture violation | Leaking types across Clean Architecture layer boundaries |
| Failing checks | Detekt errors or failing tests |
| Missing tests | New behavior introduced without unit test coverage |
| Secrets committed | Credentials or environment-specific config in version control |
| Scope creep | Changes that go beyond the accepted issue |

---

## Permissions and Governance

### Who can do what

- **Anyone** with a GitHub account can report bugs, propose features, and open pull requests from forks.
- **Core team members only** can:
  - Label and manage issues (prioritization, scope classification)
  - Assign contributors to tasks
  - Merge code to `main` (after PR approval)
  - Close issues that are out of scope, duplicates, or not bugs
  - Push directly to `main` (generally avoided; PRs preferred)
  - Review and approve pull requests

- **Repository owner only** can:
  - Manage access levels and permissions
  - Configure branch protection rules
  - Create releases and tags
  - Delete branches or force-push (emergency use only)

### Branch protection rules

The `main` branch is protected and enforces:

- **Require pull request reviews before merging** — at least one approval from a core team member
- **Require status checks to pass before merging** — all CI/CD checks must pass:
  - Backend linting (Detekt)
  - Backend tests
  - Backend build
  - Frontend linting
  - Frontend tests
  - Frontend build
- **Require branches to be up to date before merging** — prevent stale PRs from bypassing new checks
- **No direct pushes to `main`** — all changes must go through a PR
- **No force-pushes** — preserve history and PR review threads

### Expectations for external contributors

- **Read the CONTRIBUTING guide first** — before opening an issue or PR, review this guide to understand the workflow.
- **Search existing issues** before creating a new one to avoid duplicates.
- **Open an issue before large PRs** — especially for features or refactors; discuss the approach first.
- **Be respectful and constructive** — follow the [Code of Conduct](CODE_OF_CONDUCT.md).
- **Respond to feedback** — if a PR is asked for changes, provide updates or explain your rationale within 2 weeks.
- **PRs without recent activity may be closed** — to keep the backlog manageable, stale PRs may be closed after 30 days without updates.

### How the core team triages issues

1. **New issue arrives** — labeled `triage` by GitHub Actions (or assigned manually).
2. **Initial assessment** — core team determines category:
   - `bug` — confirmed reproducible issue
   - `feature` — new capability request
   - `documentation` — doc improvements
   - `question` — support or clarification request
3. **Prioritization** — labeled with priority (`priority: critical`, `priority: high`, etc.) based on impact and scope.
4. **Scope decision** — labeled `accepted`, `needs discussion`, or `out of scope`.
5. **Good first issue** — if suitable for new contributors, labeled `good first issue` or `help wanted`.
6. **Assignment** — if work is approved, a core team member may assign themselves or ask for volunteers.

---

## Architecture Overview

Before contributing, familiarize yourself with the project structure and design:

| Resource | Description |
|---|---|
| `README.md` | Quick start, local setup, and available commands |
| `backend/README.md` | Backend architecture, Gradle commands, and Kotlin conventions |
| `frontend/README.md` | Frontend architecture, npm commands, and Angular conventions |
| `docs/roles/` | Per-role capabilities (admin, recruiter, applicant) |
| `docs/flows/` | Cross-role business flows (invitation, assessment, application, authentication, GDPR) |

### Key architectural constraints

**Backend:** The project uses a **single DynamoDB table** (`scorepion-data`) with composite PK/SK patterns. Any change that introduces new data access patterns must be evaluated against the existing GSI design before implementation.

**Frontend:** The SPA follows standard Angular conventions with smart/dumb component separation. State management uses reactive patterns with RxJS. All HTTP calls must go through typed services.

---

## Getting help

- **Documentation questions:** Open an issue with the `question` label or check the relevant README for your stack.
- **Setup issues:** See [Setting Up the Development Environment](#setting-up-the-development-environment) or the stack-specific guides in `backend/README.md` and `frontend/README.md`.
- **General questions:** Open an issue with the `question` label or start a discussion on GitHub.

---

*Last updated: 2026-05-05*