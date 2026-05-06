// Template for environment configuration
// Copy this file to environment.ts and environment.prod.ts
// and replace placeholder values with actual credentials
//

export const environment = {
  production: false,

  apiUrl: 'YOUR_API_URL',
  googleSSOClientID: 'YOUR_GOOGLE_SSO_CLIENT_ID',

  googleSSOCallbackPath: '/google-sso',
  version: '1.0.0',

  // reCAPTCHA v3
  recaptchaSiteKey: 'YOUR_RECAPTCHA_SITE_KEY',

  // Branding configuration
  branding: {
    companyName: 'Your Company Name', // Will be shown in welcome message
    companyUrl: 'https://www.yourcompany.com/', // Company website URL for footer links
    legalName: 'Your Company LLC', // Legal entity name for Privacy Policy
    showCompanyLogo: false, // Set to true to display company logo
    companyLogoPath: 'assets/images/logos/company-logo.svg', // Path to company logo
    careers: {
      title: 'Join Our Team', // Careers page title
      subtitle: 'Discover your next career opportunity with us', // Careers page subtitle
    },
    privacy: {
      contactEmail: 'privacy@yourcompany.com', // Email for GDPR/privacy inquiries
      dataRetentionMonths: 9, // How many months to retain applicant data before auto-delete
    },
  },
};
