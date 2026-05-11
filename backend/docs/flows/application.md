# Self-Application Flow

Last updated: 2026-03-26

## Applicant Submits Application

1. An unauthenticated user visits the public careers page and selects an active external position.
2. They submit an application with their personal details, a CV or LinkedIn URL, and GDPR consent.
3. The system validates reCAPTCHA and stores the application with status PENDING.

## Admin Reviews Application

1. The admin views all applicants, with optional filters by status, position, or search term.
2. The admin chooses one of the following actions:

**Invite to assessments:**
- The applicant status changes from PENDING to INVITED.
- The system creates assessment invitations for every assessment linked to the position.
- The applicant receives assessment invitation emails.

**Reject:**
- The applicant status changes from PENDING to REJECTED.
- The admin can optionally provide a rejection reason.

## Applicant Statuses

| Status | Meaning |
|--------|---------|
| PENDING | Application submitted, awaiting admin review |
| INVITED | Admin approved and assessment invitations were sent |
| REJECTED | Admin rejected the application |
| ANONYMIZED | Applicant requested data erasure via GDPR |

## Data Retention

- Applicant data has a configurable retention period (default: 9 months).
- After the retention period, data is automatically anonymized by a scheduled process.
- Applicants can also request immediate erasure via GDPR data rights.

## Constraints

- Only active external positions are visible to public applicants.
- GDPR consent is required to submit an application.
- reCAPTCHA is required on all public submissions.
