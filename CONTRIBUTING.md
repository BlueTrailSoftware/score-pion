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

- JDK 17
- Docker and Docker Compose
- A GitHub account

### Local setup

```bash
# 1. Clone the repository
git clone <repository-url>
cd score-pion-webhook

# 2. Start DynamoDB Local
docker-compose -f docker-compose.dynamodb.yml up -d

# 3. Initialize the table and indexes
./scripts/init-dynamodb-local.sh

# 4. Run the application (port 7070)
./gradlew bootRun
```

### Environment variables

See [README.md](README.md#environment-configuration) for environment setup and credential requirements. Never commit `.env` files or real credentials — all secrets must be managed outside version control.

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

This project uses **Kotlin** with **Spring Boot WebFlux** and **Coroutines**. All code must follow these conventions.

### Architecture

Follow the [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) layer boundaries strictly:

| Layer | Package | Rule |
|---|---|---|
| Domain | `domain/` | Pure Kotlin. No framework dependencies. |
| Application | `application/` | Use cases and services. No HTTP or persistence types. |
| Infrastructure | `infrastructure/` | Controllers, persistence, external integrations. |

Do not leak infrastructure types (HTTP DTOs, DynamoDB items) into the application or domain layers.

### Mappers

Use explicit **Mapper classes** — never map inline inside controllers or use cases. Mappers are organized by their translation boundary:

| Location | Purpose |
|---|---|
| `infrastructure/dto/mapper/` | Use case result → HTTP response DTO |
| `infrastructure/persistence/mapper/` | Domain entity → DynamoDB item (and back) |
| `infrastructure/client/mapper/` | External API response → domain types |
| `infrastructure/adapter/*/mapper/` | Adapter-specific translations (e.g., Coderbyte) |

### Reactive model

All service and repository functions must be `suspend`. Blocking calls are not acceptable in the reactive pipeline.

### Logging

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

### Style limits

| Rule | Limit |
|---|---|
| Line length | 120 characters |
| Function length | 60 lines |
| Cyclomatic complexity | 15 per function |
| Nested block depth | 4 levels |

Add comments **only** on complex or non-obvious logic. Routine code should be self-explanatory.

### Static analysis

All code must pass Detekt with zero issues before a PR will be reviewed:

```bash
./gradlew detekt
```

The CI pipeline will reject PRs that fail linting. Fix all issues before requesting review.

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
How the change was tested (unit tests, manual verification steps).

## Checklist
- [ ] Detekt passes (`./gradlew detekt`)
- [ ] Tests pass (`./gradlew test`)
- [ ] No credentials or environment-specific values committed
- [ ] Docs updated if behavior changed
```

### Size guidance

| Lines changed | Expectation |
|---|---|
| < 200 | Reviewed quickly |
| 200–500 | Provide clear context in the description |
| > 500 | Split if possible; expect a longer review cycle |

---

## Testing Requirements

- All new use cases and services must have corresponding unit tests.
- Tests live in `src/test/kotlin/` mirroring the main source structure.
- Use **JUnit 5** and **Mockito-Kotlin** for mocking.
- Do not write tests that only verify mocks were called — test behavior and outcomes.
- Tests must be deterministic and must not depend on external services or network calls.

```bash
# Run all tests
./gradlew test

# Run a specific test
./gradlew test --tests "org.example.notifier.application.useCases.MyUseCaseTest.shouldReturnErrorWhenNotFound"
```

The CI pipeline runs all tests on every PR. PRs with failing tests will not be merged.

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

## Architecture Overview

Before contributing, familiarize yourself with the project:

| Resource | Description |
|---|---|
| `README.md` | Quick start, local setup, and available Gradle commands |
| `docs/roles/` | Per-role capabilities (admin, recruiter, applicant) |
| `docs/flows/` | Cross-role business flows (invitation, assessment, application, authentication, GDPR) |

The project uses a **single DynamoDB table** (`scorepion-data`) with composite PK/SK patterns. Any change that introduces new data access patterns must be evaluated against the existing GSI design before implementation.

---

*For questions not covered here, open an issue with the `question` label.*