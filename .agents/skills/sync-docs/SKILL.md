---
name: sync-docs
description: Generates or updates docs/roles/ and docs/flows/ by analyzing the codebase. Use to bootstrap documentation, or after adding/changing a feature, flow, or business rule.
allowed-tools: Read Write Edit Glob Grep Bash(git diff:*)
context: fork
---

# sync-docs

Analyze the backend and frontend codebases and generate or update documentation files in `docs/roles/` and `docs/flows/`.

## Mode selection

1. Check if `docs/roles/` is empty or missing. If so, run **full generation**.
2. Otherwise, run **incremental update**: use `git diff main...HEAD -- src/` in each repo. If there is a diff, update only affected docs. If there is no diff, skip.

## Repository Structure

This is a **monorepo** with backend and frontend in the same repository:

- **Backend**: `backend/` (this directory)
- **Frontend**: `../frontend` (sibling directory in monorepo root)

If the frontend code is not accessible, skip frontend analysis and document only the backend.

## Steps

### Full generation mode

1. Scan the backend: controllers, security config, use cases, domain entities
2. Scan the frontend (if available): routes, guards, page components, services
3. Identify all roles and their capabilities from both repos
4. Generate `docs/roles/<role>.md` for each role
5. Identify cross-role business flows and generate `docs/flows/<flow>.md` for each
6. Set `Last updated:` date on every file created

### Incremental update mode

1. Run `git diff main...HEAD -- src/` in each repo
2. Identify which role(s) and flow(s) are affected using the mapping convention below
3. Read the current content of the affected doc files
4. Update only the sections that changed — do not rewrite unaffected content
5. Update the `Last updated:` date on each modified file

## How to determine which docs are affected

Do not rely on file path conventions — derive the mapping from the code itself.

### For each changed file, determine the affected role(s):

1. **Backend**: Read the security annotations (`@PreAuthorize`) or security config to find which roles can access the changed code. If the endpoint is public (no auth required), it affects `docs/roles/applicant.md`.
2. **Frontend**: Read the route guards or route definitions to find which roles can reach the changed page/component. If no guard is present, it affects `docs/roles/applicant.md`.
3. If a change affects multiple roles (e.g., a shared endpoint with different behavior per role), update all affected role docs.

### For each changed file, determine if a flow doc is affected:

1. If the change involves a status transition, a multi-step process, or coordination between roles, find the matching flow doc in `docs/flows/` and update it.
2. If no matching flow doc exists and the change introduces a new cross-role process, create one.

### General rule

Read `docs/roles/` and `docs/flows/` first to know what docs already exist. Then match each changed capability to the doc that owns it. When in doubt, read the surrounding code to understand who uses it and why.

## What belongs in the docs

**Include:**
- Actions a role can perform and their outcome
- Business conditions (when something is available or blocked)
- Status states that are part of the domain (`PENDING`, `ACCEPTED`, `REJECTED`, etc.)
- Access control rules (what a role can and cannot see)

**Do not include:**
- Form field names or lists
- File format lists, size limits, or count limits — these are config values
- Database table names, PK/SK patterns, or column names
- Component names, hook names, service class names, or state variables
- API endpoint paths or HTTP methods
- Scale values or specific criteria names

**The test:** would this need updating if a developer refactors the code without changing the business intent? If yes, leave it out.

## Style rules

- Simple English, user-story style
- Describe the action and outcome, not the UI mechanics or API shape
- `Constraints` section is for business rules only, not technical limitations
- If a capability was removed, remove it from the doc — don't leave stale content
- Keep the access matrix table at the bottom of each role doc up to date
