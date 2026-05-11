# Recruiter

Last updated: 2026-03-26

## Overview

Recruiters have scoped access to the platform. They can view assigned positions, invite candidates, and track assessment results — but only for their own work.

## Onboarding

- Recruiters are invited by an admin via email. The invitation may include initial position assignments.
- On first login via SSO (Google or Auth0), the invitation is accepted automatically and the recruiter account is created.
- Invitation expires after 3 days if not accepted.

## Position Access

- View only positions explicitly assigned by an admin.
- View position details including title, description, assessments, and attached files (read-only).
- Cannot create, edit, activate, or deactivate positions.

## Candidate Management

- Invite candidates to any assigned position by email and name.
- The system creates one assessment invitation per assessment linked to that position.
- View only candidates they invited — cannot see other recruiters' candidates.
- View assessment status, scores, and completion details for their candidates.

## Applicant Access

- View applicants only from positions assigned to them.
- Cannot approve, reject, or edit applicants.

## Constraints

- Position access is controlled entirely by the admin. A recruiter cannot request or grant their own access.
- Candidate visibility is scoped to the recruiter's own invitations.
- No access to team management, settings, ticketing, or any admin-only features.

## Navigation

Recruiters see the following menu:
- Review Candidates
- Manage Positions

## Access Matrix

| Resource | Access |
|----------|--------|
| Positions | Only assigned positions (read-only) |
| Applicants | Only from assigned positions (read-only) |
| Candidates | Only self-invited candidates |
| Team members | No access |
| Settings | No access |
| Tickets | No access |
