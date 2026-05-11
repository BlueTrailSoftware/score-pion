#!/bin/bash

# =============================================
# Asana Webhook Configuration Script for Linux/macOS
# Configures webhooks for both development and production
# =============================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Initialize variables
PRODUCTION=false
PRODUCTION_URL=""
ASANA_TOKEN=""
ASANA_PROJECT_ID=""
ASANA_WORKSPACE_ID=""

# Function to print log messages
write_log() {
    echo -e "${CYAN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

write_error() {
    echo -e "${RED}ERROR: $1${NC}"
}

write_success() {
    echo -e "${GREEN}$1${NC}"
}

write_warning() {
    echo -e "${YELLOW}$1${NC}"
}

# Function to validate that a variable is not empty
validate_variable() {
    local var_name="$1"
    local var_value="$2"
    if [ -z "$var_value" ]; then
        write_error "Variable $var_name is not defined or empty"
        exit 1
    fi
}

# Function to get detailed error information
get_detailed_error() {
    local response_body="$1"
    if [ -n "$response_body" ]; then
        echo "$response_body"
    else
        echo "No additional error details available"
    fi
}

# Function to check if required commands are available
check_requirements() {
    local missing_commands=()

    if ! command -v curl &> /dev/null; then
        missing_commands+=("curl")
    fi

    if ! command -v jq &> /dev/null; then
        missing_commands+=("jq")
    fi

    if [ ${#missing_commands[@]} -ne 0 ]; then
        write_error "Missing required commands: ${missing_commands[*]}"
        write_log "Please install missing commands:"
        write_log "  Ubuntu/Debian: sudo apt-get install curl jq"
        write_log "  macOS: brew install curl jq"
        write_log "  CentOS/RHEL: sudo yum install curl jq"
        exit 1
    fi
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -p|--production)
            PRODUCTION=true
            shift
            ;;
        --production-url)
            PRODUCTION_URL="$2"
            shift
            shift
            ;;
        --asana-token)
            ASANA_TOKEN="$2"
            shift
            shift
            ;;
        --project-id)
            ASANA_PROJECT_ID="$2"
            shift
            shift
            ;;
        --workspace-id)
            ASANA_WORKSPACE_ID="$2"
            shift
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  -p, --production           Enable production mode"
            echo "  --production-url URL       Production backend URL (required in production mode)"
            echo "  --asana-token TOKEN        Asana API token (required)"
            echo "  --project-id ID            Asana project ID (required)"
            echo "  --workspace-id ID          Asana workspace ID (required)"
            echo "  -h, --help                 Show this help message"
            exit 0
            ;;
        *)
            write_error "Unknown option: $1"
            echo "Use -h or --help for usage information"
            exit 1
            ;;
    esac
done

# Check requirements
check_requirements

# =============================================
# MAIN CONFIGURATION
# =============================================

write_log "Starting Asana webhook configuration..."

# Validate all input parameters
validate_variable "AsanaToken" "$ASANA_TOKEN"
validate_variable "AsanaProjectId" "$ASANA_PROJECT_ID"
validate_variable "AsanaWorkspaceId" "$ASANA_WORKSPACE_ID"

# Show values for verification (hide full token for security)
token_preview="${ASANA_TOKEN:0:10}..."
write_log "Using Asana token: $token_preview"
write_log "Project ID: $ASANA_PROJECT_ID"
write_log "Workspace ID: $ASANA_WORKSPACE_ID"

write_success "All parameters validated successfully"

# =============================================
# DETECT ENVIRONMENT
# =============================================

ENVIRONMENT="development"
WEBHOOK_BASE_URL=""

if [ "$PRODUCTION" = true ]; then
    ENVIRONMENT="production"
    write_log "Mode: PRODUCTION"

    # In production, we need the backend URL
    if [ -z "$PRODUCTION_URL" ]; then
        write_error "In production mode you must specify the backend URL using --production-url"
        write_log "Example: $0 --production --production-url https://your-app.aws.com --asana-token 'your-token' --project-id 'project-id' --workspace-id 'workspace-id'"
        exit 1
    fi
    WEBHOOK_BASE_URL="$PRODUCTION_URL"
else
    write_log "Mode: DEVELOPMENT"

    # Check if ngrok is running
    try_ngrok_json() {
        curl -s http://localhost:4040/api/tunnels | jq -r '.tunnels[0].public_url'
    }

    try_ngrok_xml() {
        curl -s http://localhost:4040/api/tunnels | grep -oP '<PublicURL>\K[^<]+'
    }

    write_log "Checking ngrok status..."

    public_url=""

    # Try JSON API first
    write_log "Trying ngrok JSON API..."
    if public_url=$(try_ngrok_json 2>/dev/null) && [ -n "$public_url" ] && [ "$public_url" != "null" ]; then
        write_log "Successfully got URL from JSON API"
    else
        # If JSON fails, try XML
        write_log "JSON API failed, trying XML..."
        if public_url=$(try_ngrok_xml 2>/dev/null) && [ -n "$public_url" ]; then
            write_log "Successfully got URL from XML API"
        fi
    fi

    if [ -z "$public_url" ]; then
        write_error "Could not get valid ngrok URL"
        write_log "Make sure ngrok is running with a valid tunnel"
        write_log "Start ngrok with: ngrok http 8080 (or your app's port)"
        exit 1
    fi

    WEBHOOK_BASE_URL="$public_url"
    write_success "Ngrok URL: $WEBHOOK_BASE_URL"
fi

# =============================================
# BUILD WEBHOOK URL
# =============================================

WEBHOOK_ENDPOINT="$WEBHOOK_BASE_URL/admin/ticket/webhook"
write_log "Webhook endpoint: $WEBHOOK_ENDPOINT"

# =============================================
# VERIFY ENDPOINT IS ACCESSIBLE TO ASANA
# =============================================

write_log "Verifying that the endpoint is accessible to Asana..."

# Test if endpoint is reachable
write_log "   Testing basic connectivity to: $WEBHOOK_ENDPOINT"
if curl -s -I "$WEBHOOK_ENDPOINT" > /dev/null; then
    write_success "Endpoint is accessible"
else
    write_warning "Endpoint may not be accessible to Asana"
    write_log "   Asana needs to send a HEAD request to verify the endpoint"
    write_log "   Make sure your endpoint accepts HEAD requests and returns 200 OK"
fi

# =============================================
# TEST ASANA TOKEN BEFORE REGISTERING WEBHOOK
# =============================================

write_log "Testing Asana token validity..."

write_log "Using Bearer token: $token_preview"

response=$(curl -s -w "%{http_code}" -H "Authorization: Bearer $ASANA_TOKEN" -H "Content-Type: application/json" "https://app.asana.com/api/1.0/users/me")
http_code=${response: -3}
response_body=${response%???}

if [ "$http_code" -eq 200 ]; then
    user_name=$(echo "$response_body" | jq -r '.data.name')
    write_success "Asana token is valid - Connected as: $user_name"
else
    write_error "Asana token is invalid or has insufficient permissions"
    write_error "Response: $response_body"
    write_log "Please check your Asana token"
    exit 1
fi

# =============================================
# REGISTER WEBHOOK IN ASANA
# =============================================

write_log "Registering webhook in Asana..."

# First, list existing webhooks to avoid duplicates
write_log "Checking existing webhooks for workspace: $ASANA_WORKSPACE_ID"

WEBHOOK_LIST_URL="https://app.asana.com/api/1.0/webhooks?workspace=$ASANA_WORKSPACE_ID"
write_log "   Request URL: $WEBHOOK_LIST_URL"

response=$(curl -s -w "%{http_code}" -H "Authorization: Bearer $ASANA_TOKEN" -H "Content-Type: application/json" "$WEBHOOK_LIST_URL")
http_code=${response: -3}
response_body=${response%???}

if [ "$http_code" -ne 200 ]; then
    write_error "Failed to get existing webhooks: $response_body"
    exit 1
fi

existing_webhooks_count=$(echo "$response_body" | jq '.data | length')
write_log "   Found $existing_webhooks_count webhooks in workspace"

# Log all found webhooks for debugging
if [ "$existing_webhooks_count" -gt 0 ]; then
    write_log "   Existing webhooks:"
    echo "$response_body" | jq -c '.data[]' | while read webhook; do
        webhook_id=$(echo "$webhook" | jq -r '.gid')
        project_id=$(echo "$webhook" | jq -r '.resource.gid')
        target=$(echo "$webhook" | jq -r '.target')
        write_log "     - Webhook ID: $webhook_id, Project ID: $project_id, Target: $target"
    done
fi

# Check if webhook already exists for this project (regardless of URL)
existing_webhook_for_project=$(echo "$response_body" | jq -c ".data[] | select(.resource.gid == \"$ASANA_PROJECT_ID\")" | head -n1)

if [ -n "$existing_webhook_for_project" ]; then
    webhook_id=$(echo "$existing_webhook_for_project" | jq -r '.gid')
    current_target=$(echo "$existing_webhook_for_project" | jq -r '.target')
    write_warning "Webhook already exists for this project (Webhook ID: $webhook_id)"
    write_log "   Project ID: $ASANA_PROJECT_ID"
    write_log "   Current target: $current_target"
    write_log "Deleting existing webhook before creating new one..."

    # Delete existing webhook
    DELETE_URL="https://app.asana.com/api/1.0/webhooks/$webhook_id"
    write_log "   Delete URL: $DELETE_URL"

    response=$(curl -s -w "%{http_code}" -X DELETE -H "Authorization: Bearer $ASANA_TOKEN" -H "Content-Type: application/json" "$DELETE_URL")
    http_code=${response: -3}
    response_body=${response%???}

    if [ "$http_code" -eq 200 ]; then
        write_success "Previous webhook deleted successfully"
    else
        write_error "Could not delete existing webhook: $response_body"
        write_log "Will try to create new webhook anyway..."
    fi
else
    write_log "No existing webhook found for this project"
fi

# Register new webhook
write_log "Registering new webhook..."

BODY=$(jq -n --arg resource "$ASANA_PROJECT_ID" --arg target "$WEBHOOK_ENDPOINT" '{data: {resource: $resource, target: $target}}')

write_log "   Request Body: $BODY"

response=$(curl -s -w "%{http_code}" -X POST -H "Authorization: Bearer $ASANA_TOKEN" -H "Content-Type: application/json" -d "$BODY" "https://app.asana.com/api/1.0/webhooks")
http_code=${response: -3}
response_body=${response%???}

if [ "$http_code" -eq 201 ] || [ "$http_code" -eq 200 ]; then
    webhook_id=$(echo "$response_body" | jq -r '.data.gid')
    webhook_target=$(echo "$response_body" | jq -r '.data.target')
    write_success "Webhook registered successfully!"

    echo ""
    write_log "Webhook details:"
    write_log "   ID: $webhook_id"
    write_log "   Target: $webhook_target"
    write_log "   Project: $ASANA_PROJECT_ID"
    write_log "   Workspace: $ASANA_WORKSPACE_ID"
    write_log "   Environment: $ENVIRONMENT"
    echo ""

    if [ "$ENVIRONMENT" = "development" ]; then
        write_warning "REMEMBER: In development, if you restart ngrok the URL will change"
        write_warning "   You must run this script again with the same parameters"
    fi

else
    write_error "Error registering webhook: $response_body"

    # Get detailed error information
    error_details=$(get_detailed_error "$response_body")
    write_log "Error details: $error_details"

    # Provide specific guidance for 403 errors
    if echo "$response_body" | grep -q "403" || echo "$error_details" | grep -q "403"; then
        echo ""
        write_log "SOLUCIÓN PARA ERROR 403:"
        write_log "   Asana no puede acceder a tu endpoint. Posibles soluciones:"
        write_log "   1. Verifica que tu aplicación esté ejecutándose"
        write_log "   2. Asegúrate de que el endpoint /admin/ticket/webhook acepte POST requests"
        write_log "   3. Si usas Spring Security, verifica que la ruta no esté bloqueada por CSRF"
        write_log "   4. Prueba acceder manualmente a: $WEBHOOK_ENDPOINT"
        write_log "   5. Verifica que ngrok esté funcionando correctamente"
    fi

    exit 1
fi

# =============================================
# FINAL VERIFICATION
# =============================================

write_log "Performing final verification..."

# List active webhooks to confirm
write_log "Active webhooks in the project:"

response=$(curl -s -w "%{http_code}" -H "Authorization: Bearer $ASANA_TOKEN" -H "Content-Type: application/json" "$WEBHOOK_LIST_URL")
http_code=${response: -3}
response_body=${response%???}

if [ "$http_code" -eq 200 ]; then
    active_webhooks_count=$(echo "$response_body" | jq '.data | length')
    if [ "$active_webhooks_count" -gt 0 ]; then
        echo "$response_body" | jq -c '.data[]' | while read webhook; do
            webhook_id=$(echo "$webhook" | jq -r '.gid')
            target=$(echo "$webhook" | jq -r '.target')
            write_log "   $webhook_id -> $target"
        done
    else
        write_warning "No active webhooks found"
    fi
else
    write_warning "Could not retrieve active webhooks list: $response_body"
fi

write_success "Configuration completed!"
write_log "Your application will now receive notifications when tasks are moved in Asana"
write_log "Test the integration by:"
write_log "  1. Creating a task in your Asana board"
write_log "  2. Moving the task to a different column"
write_log "  3. Checking your application logs for webhook events"