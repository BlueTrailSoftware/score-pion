## Description
<!-- Briefly describe what functionality is added and why. -->

## Changes made
<!-- List the main changes. -->
-

## Checklist

### Architecture
- [ ] Code follows the project's hexagonal architecture
- [ ] Business logic is in `application/useCases/`, not leaked into services or controllers
- [ ] `LoggerFactory` was NOT used directly — `LoggerPort` was injected via constructor
- [ ] Mappers are in `infrastructure/dto/mapper/` as extension functions

### Security & Authorization
- [ ] Endpoints have role-based authorization checks
- [ ] Resource ownership validated before access (no IDOR)
- [ ] No sensitive data logged

### API & Contract
- [ ] HTTP status codes match project conventions
- [ ] Swagger/OpenAPI annotations updated (if endpoint added/modified)
- [ ] Error responses follow existing format

### Data
- [ ] DynamoDB access patterns reviewed — no unnecessary scans, GSIs used where needed (if applicable)
- [ ] No hardcoded IDs, secrets, or environment values in code

### External Integrations *(if applicable)*
- [ ] External service calls are behind a port/adapter
- [ ] Failures in external services are handled gracefully

### Tests & Docs
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated (if applicable)
- [ ] Business rules validated against `docs/roles/` and `docs/flows/`
- [ ] Postman collection updated (if any endpoint was modified)

### General
- [ ] Branch is up to date with `main`
- [ ] Documentation added/updated

## Related issue
<!-- Closes #number or ticket reference -->

## Screenshots / evidence (if applicable)
