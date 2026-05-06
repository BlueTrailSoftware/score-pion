#!/bin/bash

# =============================================
# Ngrok Starter Script
# Starts ngrok and monitors the connection
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

# Function to cleanup on exit
cleanup() {
    log "🛑 Stopping ngrok..."
    kill $NGROK_PID 2>/dev/null || true
    exit 0
}

trap cleanup SIGINT SIGTERM

# =============================================
# MAIN SCRIPT
# =============================================

log "🚀 Starting ngrok tunnel..."

# Check if ngrok is configured
if ! ngrok config check &>/dev/null; then
    error "Ngrok is not configured"
    log "Please run first: ./scripts/setup-ngrok.sh <your-authtoken>"
    log "Get your authtoken from: https://dashboard.ngrok.com/get-started/your-authtoken"
    exit 1
fi

# Check if application is running
log "🔍 Checking if Spring Boot application is running..."

if curl -s http://localhost:7070/actuator/health > /dev/null 2>&1; then
    success "Application is running on port 7070 (actuator endpoint)"
else
    error "Application is not running on port 7070"
    log "Please start your Spring Boot application first:"
    log "  ./gradlew bootRun"
    log "Or if using a different port:"
    log "  ./gradlew bootRun --args='--server.port=8081'"
    log "Then update this script to use the correct port"
    exit 1
fi

# Start ngrok
log "🌐 Starting ngrok tunnel to port 7070..."

# Create log directory if it doesn't exist
mkdir -p logs

# Start ngrok and log to file
ngrok http 7070 > logs/ngrok.log 2>&1 &
NGROK_PID=$!

# Wait for ngrok to start
log "⏳ Waiting for ngrok to initialize..."
sleep 5

# Check if ngrok started successfully
if ! ps -p $NGROK_PID > /dev/null; then
    error "Ngrok failed to start"
    log "Check logs/ngrok.log for details"
    exit 1
fi

# Get ngrok URL
log "🔗 Getting ngrok public URL..."
MAX_RETRIES=10
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    NGROK_URL=$(curl -s http://localhost:4040/api/tunnels 2>/dev/null | grep -o 'https://[^"]*\.ngrok[^"]*' | head -1)

    if [ -n "$NGROK_URL" ]; then
        success "Ngrok tunnel established: $NGROK_URL"
        break
    fi

    RETRY_COUNT=$((RETRY_COUNT + 1))
    log "   Retrying... ($RETRY_COUNT/$MAX_RETRIES)"
    sleep 2
done

if [ -z "$NGROK_URL" ]; then
    error "Failed to get ngrok URL after $MAX_RETRIES attempts"
    log "Check ngrok status: curl http://localhost:4040/api/tunnels"
    cleanup
fi

# Display webhook information
log ""
success "🎉 Ngrok is running successfully!"
log "📋 Connection details:"
log "   Local URL: http://localhost:7070"
log "   Public URL: $NGROK_URL"
log "   Webhook endpoint: $NGROK_URL/admin/ticket/webhook"
log ""
log "📊 Monitoring:"
log "   Ngrok web interface: http://localhost:4040"
log "   Application logs: tail -f logs/application.log"
log "   Ngrok logs: tail -f logs/ngrok.log"
log ""
log "🛑 To stop: Press Ctrl+C"
log ""

# Monitor ngrok status
log "🔍 Starting ngrok monitor..."
while ps -p $NGROK_PID > /dev/null; do
    sleep 10
    # Check if ngrok is still responding
    if ! curl -s http://localhost:4040/api/tunnels > /dev/null 2>&1; then
        error "Ngrok stopped responding"
        break
    fi
done

error "Ngrok process ended"
cleanup