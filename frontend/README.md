# score-pion-web

## 📋 Initial Setup

### 1️⃣ Install Dependencies

```bash
npm run setup
```

This installs Node dependencies and configures Husky git hooks.

### 2️⃣ Create Environment Files (Required)

```bash
# Copy the template for development
cp src/environments/environment.example.ts src/environments/environment.ts

# Copy the template for production
cp src/environments/environment.example.ts src/environments/environment.prod.ts
```

### 3️⃣ Configure Google OAuth (Required for Login)

To enable user login via Google:

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create an OAuth 2.0 Client ID:
   - Application type: Web
   - Add authorized domains: `localhost:4200` (dev), `yourdomain.com` (prod)
   - Download or copy your **Client ID**
3. Add the Client ID to both environment files:
   ```typescript
   googleSsoClientId: 'YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com',
   ```

### 4️⃣ Configure reCAPTCHA v3 (Required for Forms)

reCAPTCHA protects forms from spam:

1. Go to [Google reCAPTCHA Admin](https://www.google.com/recaptcha/admin)
2. Create a new site with reCAPTCHA v3:
   - Add domains: `localhost` (dev), `yourdomain.com` (prod)
   - Copy your **Site Key** (public, safe to commit)
3. Add to both environment files:
   ```typescript
   recaptchaSiteKey: 'YOUR_RECAPTCHA_SITE_KEY',
   ```
4. Share the **Secret Key** with your backend team for validation configuration

### 5️⃣ Configure API URL

Set the backend API URL in both environment files:

```typescript
apiUrl: 'http://localhost:7070',  // For local dev
apiUrl: 'https://api.yourdomain.com',  // For production
```

### 6️⃣ Customize Privacy Policy (Optional)

1. Edit `public/privacy-policy.md` to match your organization
2. Update privacy settings in environment files:
   ```typescript
   branding: {
     privacy: {
       contactEmail: 'privacy@yourcompany.com',
       dataRetentionMonths: 9,
     }
   }
   ```
3. Have legal review before deploying to production

## 🚀 Development Scripts

### Code Quality & Linting

| Command                   | Description                                                        |
| ------------------------- | ------------------------------------------------------------------ |
| `npm run lint`            | Check TypeScript and HTML files for code issues                    |
| `npm run lint:fix`        | Automatically fix linting issues in TypeScript and HTML            |
| `npm run lint:styles`     | Check SCSS files for style issues                                  |
| `npm run lint:styles:fix` | Automatically fix style issues in SCSS files                       |
| `npm run format`          | Format all code files (TS, HTML, SCSS, JSON) using Prettier        |
| `npm run format:check`    | Check if files are formatted correctly without modifying them      |
| `npm run check`           | Run all checks (format + lint + styles) - useful before committing |
| `npm run fix`             | Run all auto-fixes (format + lint:fix + lint:styles:fix)           |

### Development & Build

| Command           | Description                                              |
| ----------------- | -------------------------------------------------------- |
| `npm run setup`   | Install dependencies and configure Husky git hooks       |
| `npm start`       | Start development server at http://localhost:4200        |
| `npm run build`   | Build the project for production (updates version first) |
| `npm run watch`   | Build in watch mode for development                      |

### Pre-commit Hook

The project uses Husky to run `lint-staged` before each commit. This is configured automatically by `npm run setup`.

---

## 🔒 GDPR & Privacy Compliance

This application implements GDPR-compliant data handling for job applicants:

### Privacy Features

- **Privacy Policy**: Available at `/privacy-policy` with configurable company details
- **Right to Access**: Users can download their data at `/privacy/download-my-data`
- **Right to Erasure**: Users can request data deletion at `/privacy/delete-my-data`
- **Data Retention**: Automatic anonymization after configurable retention period
- **Consent Management**: Explicit GDPR consent required during application

### Configuration

Privacy settings are configured in environment files:

```typescript
branding: {
  privacy: {
    contactEmail: 'privacy@yourcompany.com',  // GDPR contact email
    dataRetentionMonths: 9,                   // Data retention period
  }
}
```

### Privacy Policy Customization

The privacy policy uses placeholders that are automatically replaced:

- `{{privacyEmail}}` - Privacy contact email
- `{{companyLegalName}}` - Legal entity name
- `{{dataRetentionMonths}}` - Data retention period
- `{{baseUrl}}` - Application URL (auto-detected)

Edit `public/privacy-policy.md` to customize the policy for your organization.

---

## 🛡️ reCAPTCHA v3 Configuration

The application uses Google reCAPTCHA v3 to protect forms from spam and abuse.

### Setup

1. **Get reCAPTCHA Keys**:
   - Go to [Google reCAPTCHA Admin](https://www.google.com/recaptcha/admin)
   - Create a new site with reCAPTCHA v3
   - Add your domains (localhost for dev, production domain)

2. **Configure Environment**:

```typescript
// environment.ts / environment.prod.ts
recaptchaSiteKey: 'YOUR_RECAPTCHA_SITE_KEY',  // Public key (safe to commit)
```

3. **Backend Configuration**:
   - Configure the **Secret Key** in your backend (never commit this!)
   - Backend must validate tokens at: `https://www.google.com/recaptcha/api/siteverify`
   - Recommended score threshold: 0.5

### Protected Forms

reCAPTCHA v3 protects the following forms:

- Job application form (`/careers/:id/apply`) - Action: `apply`
- Delete my data form (`/privacy/delete-my-data`) - Action: `delete_data`
- Download my data form (`/privacy/download-my-data`) - Action: `download_data`

### Important Notes

- reCAPTCHA v3 is **invisible** to users (no checkbox or challenges)
- Tokens are generated automatically when forms are submitted
- Tokens expire after ~2 minutes and are single-use
- Monitor usage at [reCAPTCHA Admin Console](https://www.google.com/recaptcha/admin)

---

## 🔧 Deployment Script Overview

The scripts under /scripts automates the build and deployment process of the `score-pion-web` Docker image to a remote server. It extracts the version from `package.json`, builds and saves the Docker image locally, transfers it to the server, and deploys it using SSH.

To run the automated script to deploy this service you need to run the following command

- 🐧 **Linux / macOS**

```bash
./scripts/deploy.sh
```

- 🪟 **Windows**

```bash
.\scripts\deploy.ps1
```

---

## ⚙️ What You Need to Configure in your local machine

Make sure to update these variables in the script:

| Env variable            | Description                        |
| ----------------------- | ---------------------------------- |
| `SCORE_PION_SERVER_PEM` | Path to the server SSH private key |

Other requirements:

- Docker installed locally and on the remote server
- scp need to be installed or usable
- SSH access to the server with sudo privileges
