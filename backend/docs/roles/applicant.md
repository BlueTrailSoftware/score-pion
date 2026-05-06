# Applicant

Last updated: 2026-03-26

## Overview

Applicants are unauthenticated users who browse open positions, apply to jobs, and exercise their GDPR data rights. No login is required.

## Browse Positions

- View all active external positions on the public careers page.
- Internal positions and inactive positions are hidden.
- View position details including title, description, and associated assessments.

## Apply to a Position

- Submit an application with personal details, a CV or LinkedIn profile URL, and GDPR consent.
- At least one of CV or LinkedIn URL is required.
- Application requires reCAPTCHA verification.
- Application status starts as PENDING and is reviewed by an admin.

## GDPR Data Rights

### Data Export (Right to Access)

- Request a personal data export by providing an email address (with reCAPTCHA).
- The system sends a verification email with a one-time download link.
- Clicking the link downloads all personal data associated with that email.
- Cannot export data that has already been anonymized.

### Data Erasure (Right to be Forgotten)

- Request data deletion by providing an email address (with reCAPTCHA).
- The system sends a verification email with a one-time confirmation link.
- Confirming the link anonymizes all personal data: name, email, and phone are replaced with generic values.
- Applicant status changes to ANONYMIZED.
- A confirmation email is sent to the original address.
- Anonymized data can no longer be exported or identified.

## Privacy Policy

- A dedicated privacy policy page is available without authentication.

## Constraints

- Cannot access any authenticated pages or features.
- Cannot see non-external or inactive positions.
- Cannot view other applicants or any internal platform data.
- All public form submissions require reCAPTCHA verification.

## Access Matrix

| Resource | Access |
|----------|--------|
| Positions | Active external only (read-only) |
| Applications | Can submit own application |
| Personal data | Can request export or deletion of own data |
| Platform features | No access |
