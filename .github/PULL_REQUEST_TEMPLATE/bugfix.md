## App afectada
- [ ] Backend
- [ ] Frontend

## Bug description
<!-- What was failing and under what conditions? -->

## Root cause
<!-- Explain why the problem was occurring. -->

## Applied fix
<!-- Describe how it was resolved. -->

## Checklist

### Fix Quality
- [ ] The fix addresses the root cause, not just the symptom
- [ ] The fix does not break existing functionality
- [ ] A test that reproduces the bug was added/updated

### Security
- [ ] If the bug was security-related (IDOR, auth bypass, data leak), access controls were reviewed
- [ ] No sensitive data logged in the fix

### Code Conventions
- [ ] `LoggerFactory` was NOT used directly — `LoggerPort` was injected via constructor

### General
- [ ] Branch is up to date with `main`

## Related issue
<!-- Closes #number -->

## How to reproduce the bug (before the fix)
1.
2.
3.