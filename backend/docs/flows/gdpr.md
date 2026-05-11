# GDPR Data Rights Flow

Last updated: 2026-03-26

## Data Export (Right to Access)

1. The data subject submits an export request with their email address and reCAPTCHA verification.
2. The system checks whether any applications exist for that email.
3. A one-time privacy token is generated and a verification email is sent with a download link.
4. The data subject clicks the download link.
5. The system verifies the token and generates a downloadable file containing all personal data associated with that email.
6. The token is invalidated after use or expiration.

## Data Erasure (Right to be Forgotten)

1. The data subject submits an erasure request with their email address and reCAPTCHA verification.
2. The system checks whether any applications exist for that email.
3. A one-time privacy token is generated and a verification email is sent with a confirmation link.
4. The data subject clicks the confirmation link.
5. For each applicant record matching that email:
   - Name, email, and phone are replaced with anonymized values.
   - Status is set to ANONYMIZED.
6. A confirmation email is sent to the original email address.
7. Anonymized data can no longer be exported or identified.

## Automatic Data Retention

- Applicant data is automatically anonymized after a configurable retention period (default: 9 months).
- A scheduled process runs periodically to identify and anonymize expired records.
- This happens independently of any data subject request.

## Constraints

- All public GDPR requests require reCAPTCHA verification.
- Privacy tokens are single-use and time-limited.
- Data that has already been anonymized cannot be exported.
- The erasure process is irreversible — once anonymized, the original data cannot be recovered.
