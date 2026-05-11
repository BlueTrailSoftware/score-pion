# User Invitation Flow

Last updated: 2026-03-26

## Inviting an Admin

1. An existing admin sends an invitation to an email address.
2. The system creates a pending invitation with role ADMIN and a 3-day expiration.
3. An invitation email is sent to the new user.
4. The invited user logs in via Google SSO or Auth0.
5. The system detects the pending invitation, creates the user account with ADMIN role, and marks the invitation as ACCEPTED.
6. There is no separate "accept" step — the first successful login is the acceptance.
7. If no pending invitation exists for the email, the login is rejected.

## Inviting a Recruiter

1. An admin sends an invitation to an email address, optionally selecting positions to assign.
2. The system creates a pending invitation with role RECRUITER and a 3-day expiration.
3. An invitation email is sent to the new user.
4. The invited user logs in via Google SSO or Auth0.
5. The system creates the user account with RECRUITER role, marks the invitation as ACCEPTED, and grants access to the positions listed in the invitation.
6. The recruiter can immediately see their assigned positions.

## Invitation Statuses

| Status | Meaning |
|--------|---------|
| PENDING | Invitation sent, waiting for the user to log in |
| ACCEPTED | User logged in and account was created |
| EXPIRED | 3-day window passed without acceptance |
| REVOKED | Admin manually cancelled the invitation |

## Constraints

- Only admins can send invitations.
- A user who is not invited cannot create an account — SSO login alone is not sufficient.
- Deactivated users are blocked at login even if SSO succeeds.
