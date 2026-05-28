## PR Type
- [ ] Feature
- [ ] Bugfix
- [ ] Hotfix
- [ ] Refactor

## App affected
- [ ] Backend
- [ ] Frontend

---

<!-- ──────────────────────────────────────────────────────────────
     Fill in the section that matches your PR type and delete the others.
     ────────────────────────────────────────────────────────────── -->

### Feature
<!-- Briefly describe what functionality is added and why. -->

**Changes made**
-

---

### Bugfix / Hotfix
**Bug / Production issue**
<!-- What was failing and under what conditions? For hotfix: describe the incident and its impact. -->

**Root cause**
<!-- Why did it happen? -->

**Applied fix**
<!-- How was it resolved? For hotfix: keep this minimal and focused on the incident. -->

**Change risk** *(hotfix only)*
<!-- Is there any regression risk? What was verified? -->

**Rollback plan** *(hotfix only)*
<!-- How to revert this change if it makes things worse? -->

---

### Refactor
**Motivation**
<!-- Technical debt, readability, preparation for a new feature, etc. -->

**Changes made**
-

**What does NOT change**
- Public API does not change
-

---

## Checklist

### Backend *(delete section if not applicable)*

#### Architecture
- [ ] Code follows the project's hexagonal architecture
- [ ] Business logic is in `application/useCases/`, not leaked into services or controllers
- [ ] `LoggerFactory` was NOT used directly — `LoggerPort` was injected via constructor
- [ ] Mappers are in `infrastructure/dto/mapper/` as extension functions

#### Security & Authorization
- [ ] Endpoints have role-based authorization checks
- [ ] Resource ownership validated before access (no IDOR)
- [ ] No sensitive data logged

#### API & Contract
- [ ] HTTP status codes match project conventions
- [ ] Swagger/OpenAPI annotations updated (if endpoint added/modified)
- [ ] Error responses follow existing format

#### Data
- [ ] DynamoDB access patterns reviewed — no unnecessary scans, GSIs used where needed (if applicable)
- [ ] No hardcoded IDs, secrets, or environment values in code

#### External Integrations *(if applicable)*
- [ ] External service calls are behind a port/adapter
- [ ] Failures in external services are handled gracefully

---

### Frontend *(delete section if not applicable)*

#### Structure & Conventions
- [ ] Components follow the project's folder structure and naming conventions
- [ ] No direct API calls outside of services/hooks
- [ ] No business logic in components — moved to services, stores, or composables

#### UI & UX
- [ ] UI matches the design spec or the expected behavior
- [ ] Responsive behavior verified (if applicable)
- [ ] Loading, empty, and error states handled

#### Security
- [ ] No sensitive data exposed in the UI or browser console
- [ ] User input is validated/sanitized before use

---

### Tests & Docs

- [ ] Unit tests added/updated
- [ ] Integration tests added/updated *(if applicable)*
- [ ] A test that reproduces the bug was added/updated *(bugfix/hotfix only)*
- [ ] Business rules validated against `docs/roles/` and `docs/flows/`
- [ ] Postman collection updated *(if any endpoint was modified)*
- [ ] A follow-up issue will be created for a permanent solution *(hotfix only, if applicable)*

### General
- [ ] Branch is up to date with `main`
- [ ] No hardcoded secrets, credentials, or environment values introduced

---

## Related issue
<!-- Closes #number -->

## Screenshots / evidence *(if applicable)*