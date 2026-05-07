## App afectada
- [ ] Backend
- [ ] Frontend

## Motivation
<!-- Why is this being refactored? Technical debt, readability, preparation for a new feature, etc. -->

## Changes made
<!-- List the main changes. -->
-

## What does NOT change
<!-- Confirm that the external behavior remains the same. -->
- Public API does not change
-

## Checklist

### Behavioral Safety
- [ ] No functional behavior was modified
- [ ] Public API (endpoints, contracts) does not change
- [ ] Existing tests still pass without modification (or were updated only due to renames)

### Architecture
- [ ] Code follows the project's hexagonal architecture
- [ ] `LoggerFactory` was NOT used directly — `LoggerPort` was injected via constructor
- [ ] Mappers are in `infrastructure/dto/mapper/` as extension functions

### Code Quality
- [ ] No new blocking calls introduced in coroutine/reactive contexts
- [ ] Dead code removed (if applicable)

### General
- [ ] Branch is up to date with `main`

## Related issue
<!-- Technical debt ticket reference, if any -->
