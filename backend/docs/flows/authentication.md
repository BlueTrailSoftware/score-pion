# Authentication Flow

Last updated: 2026-03-26

## Supported Providers

The platform supports two login methods:
- **Google SSO** — Google Identity Services (implicit flow, ID token returned directly)
- **Auth0** — Authorization Code flow (server exchanges code for tokens)

Both providers converge on the same logic after identity is verified.

## Login Flow

1. User selects a login method on the login page.
2. The identity provider authenticates the user and returns identity information.
3. The system checks whether the user already exists:
   - **Existing user**: Profile is updated with latest SSO info (name, picture). If the account is deactivated, login is rejected.
   - **New user**: The system looks for a pending invitation matching the email. If found, a new account is created with the role from the invitation (ADMIN or RECRUITER) and the invitation is marked ACCEPTED. If no invitation exists, login is rejected.
4. The system generates its own JWT token and redirects the user to the frontend with the token, user ID, and role.

## Post-Login Routing

- **Admin** is redirected to Review Candidates.
- **Recruiter** is redirected to the positions page.
- Already-authenticated users who visit the login page are redirected to their home page.

## Session Management

- The JWT token is stored client-side and sent with every request.
- Invalid or expired tokens result in automatic logout and redirect to the login page.
- Logout clears the client-side token.

## Constraints

- Users cannot create accounts without an invitation — SSO alone is not sufficient.
- Deactivated accounts are blocked at login regardless of valid SSO credentials.
- The platform issues its own JWT regardless of which identity provider was used.
