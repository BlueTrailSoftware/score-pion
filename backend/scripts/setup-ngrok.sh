#!/bin/bash

# =============================================
# Ngrok Initial Setup Script
# Configures ngrok authtoken and verifies installation
# =============================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}❌ ERROR:${NC} $1"
}

success() {
    echo -e "${GREEN}✅${NC} $1"
}

warning() {
    echo -e "${YELLOW}⚠️${NC} $1"
}

# =============================================
# MAIN SCRIPT
# =============================================

log "🚀 Starting ngrok setup..."

# Check if ngrok is installed
if ! command -v ngrok &> /dev/null; then
    error "ngrok is not installed"
    log "Please install ngrok first:"
    log "Option A - Using snap (Linux):"
    log "  sudo snap install ngrok"
    log ""
    log "Option B - Manual download:"
    log "  # Download ngrok"
    log "  wget https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-linux-amd64.tgz"
    log "  # Extract the file"
    log "  tar xvzf ngrok-v3-stable-linux-amd64.tgz"
    log "  # Move to /usr/local/bin"
    log "  sudo mv ngrok /usr/local/bin/"
    log "  # Verify installation"
    log "  ngrok version"
    exit 1
fi

success "ngrok is installed: $(ngrok version)"

# Check if authtoken is provided
if [ -z "$1" ]; then
    error "Ngrok authtoken is required"
    log ""
    log "Usage: $0 <ngrok-authtoken>"
    log ""
    log "To get your authtoken:"
    log "1. Create account at: https://dashboard.ngrok.com/signup"
    log "2. Get your authtoken from: https://dashboard.ngrok.com/get-started/your-authtoken"
    log "3. Run: $0 your_authtoken_here"
    log ""
    log "Example:"
    log "  $0 344KCz9lSplrqhruwroZeBWhoD9_4NUx3oYRx9456cBpmm96Z"
    exit 1
fi

NGROK_AUTHTOKEN=$1

log "🔑 Configuring ngrok authtoken..."

# Configure ngrok authtoken
if ngrok config add-authtoken "$NGROK_AUTHTOKEN"; then
    success "Ngrok authtoken configured successfully"
else
    error "Failed to configure ngrok authtoken"
    log "Please check your token and try again"
    exit 1
fi

# Verify configuration
log "🔍 Verifying ngrok configuration..."

if ngrok config check; then
    success "Ngrok configuration is valid"
else
    error "Ngrok configuration check failed"
    exit 1
fi

# Test ngrok functionality
log "🧪 Testing ngrok functionality..."

# Start ngrok in background for testing
ngrok http 8080 > /dev/null 2>&1 &
NGROK_PID=$!

# Wait for ngrok to start
sleep 3

# Check if ngrok is running
if curl -s http://localhost:4040/api/tunnels > /dev/null; then
    success "Ngrok is working correctly"

    # Get the public URL
    NGROK_URL=$(curl -s http://localhost:4040/api/tunnels | grep -o 'https://[^"]*\.ngrok[^"]*' | head -1)
    log "🌐 Your ngrok URL: $NGROK_URL"
else
    error "Ngrok is not responding"
fi

# Stop ngrok
kill $NGROK_PID 2>/dev/null || true

log ""
success "🎉 Ngrok setup completed successfully!"
log "📋 Next steps:"
log "   1. Start your Spring Boot application"
log "   2. Run: ./scripts/start-ngrok.sh"
log "   3. Run: ./scripts/setup-asana-webhook.sh"