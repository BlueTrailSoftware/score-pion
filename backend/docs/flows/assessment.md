# Assessment Flow

Last updated: 2026-03-26

## How Assessments Are Linked to Positions

- When an admin creates or edits a position, they select one or more assessments from the assessment platform (Coderbyte).
- Each assessment is stored as a link between the position and the assessment.
- Candidates invited to a position receive one invitation per linked assessment.

## Candidate Assessment Lifecycle

1. A recruiter or admin invites a candidate to a position.
2. The system creates one invitation per assessment linked to that position, each with status PENDING.
3. The candidate receives email invitations with links to take each assessment.
4. The candidate opens the assessment link and begins the test on the assessment platform.
5. The platform sends a webhook event indicating the assessment was started.
6. The candidate completes (or times out on) the assessment.
7. The platform sends a webhook event with the results: final score, qualification status, code score, multiple-choice score, and plagiarism indicators.
8. The system updates the invitation with the results.
9. Notifications are sent to global recipients via email and chat.

## Notifications

- When an assessment is completed, the system notifies:
  - Global recipient email addresses (configured by admin in settings)
  - Google Chat (if configured)
- The notification includes a summary of the candidate's results.

## Constraints

- Candidates interact only with the assessment platform — they do not log into ScorePion.
- Assessment results are received exclusively via webhooks; the system does not poll for results.
- A position must have at least one assessment before candidates can be invited.
