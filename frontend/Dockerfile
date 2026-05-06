# Stage 1: Build Angular app
FROM node:20-alpine AS builder

# Install build dependencies required by Angular
RUN apk add --no-cache \
  python3 \
  make \
  g++ \
  libc6-compat \
  git

WORKDIR /app

# Install dependencies
COPY package*.json ./
RUN npm ci --ignore-scripts

# Copy source files
COPY . .

RUN apk add --no-cache dos2unix \
  && dos2unix ./scripts/git-info.sh \
  && chmod +x ./scripts/git-info.sh \
  && ./scripts/git-info.sh

# Validate that environment configuration files exist
RUN test -f src/environments/environment.ts || \
  (echo "ERROR: src/environments/environment.ts not found." && \
   echo "SETUP: Copy the environment template:" && \
   echo "  cp src/environments/environment.example.ts src/environments/environment.ts" && \
   echo "  cp src/environments/environment.example.ts src/environments/environment.prod.ts" && \
   echo "Then fill in your Google OAuth Client ID and reCAPTCHA site key." && \
   exit 1)

# Run prebuild script explicitly (ignore-scripts blocks automatic execution)
RUN node update-env-version.js

# Build the Angular app using production configuration
RUN echo "Installing Angular CLI..." \
  && npm install -g @angular/cli \
  && echo "Starting Angular build..." \
  && ng build --configuration=production --base-href / --verbose

# Stage 2: Serve the app with `serve`
FROM node:20-alpine AS runner

WORKDIR /app

# Install serve and curl
RUN npm install -g serve \
  && apk add --no-cache curl

# Copy built files from builder stage
COPY --from=builder /app/dist/score-pion-web/browser/ /app/dist

EXPOSE 4200

# Serve the app on port 4200 with SPA fallback
CMD ["serve", "-s", "dist", "-l", "4200"]

# healthcheck will be managed by traefik
# HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
#  CMD curl -f http://localhost:4200/ || exit 1