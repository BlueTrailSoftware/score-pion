# =============================================
# Asana Webhook Configuration Script for Windows
# Configures webhooks for both development and production
# =============================================

param(
    [switch]$Production,
    [string]$ProductionUrl,
    [Parameter(Mandatory=$true)]
    [string]$AsanaToken,
    [Parameter(Mandatory=$true)]
    [string]$AsanaProjectId,
    [Parameter(Mandatory=$true)]
    [string]$AsanaWorkspaceId
)

# Colors for output
$ErrorActionPreference = "Stop"

function Write-Log {
    param([string]$Message)
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:%S')] $Message" -ForegroundColor Cyan
}

function Write-Error {
    param([string]$Message)
    Write-Host "ERROR: $Message" -ForegroundColor Red
}

function Write-Success {
    param([string]$Message)
    Write-Host "$Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "$Message" -ForegroundColor Yellow
}

# Function to validate that a variable is not empty
function Validate-Variable {
    param([string]$VarName, [string]$VarValue)

    if ([string]::IsNullOrWhiteSpace($VarValue)) {
        Write-Error "Variable $VarName is not defined or empty"
        exit 1
    }
}

# Function to get detailed error information
function Get-DetailedError {
    param($Exception)

    try {
        if ($Exception.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($Exception.Exception.Response.GetResponseStream())
            $reader.BaseStream.Position = 0
            $reader.DiscardBufferedData()
            $responseBody = $reader.ReadToEnd()
            return $responseBody
        }
    } catch {
        return "Could not read response body: $($_.Exception.Message)"
    }

    return "No additional error details available"
}

# =============================================
# MAIN CONFIGURATION
# =============================================

Write-Log "Starting Asana webhook configuration..."

# Validate all input parameters
Validate-Variable "AsanaToken" $AsanaToken
Validate-Variable "AsanaProjectId" $AsanaProjectId
Validate-Variable "AsanaWorkspaceId" $AsanaWorkspaceId

# Show values for verification (hide full token for security)
$tokenPreview = if ($AsanaToken.Length -gt 10) { $AsanaToken.Substring(0, 10) + "..." } else { $AsanaToken }
Write-Log "Using Asana token: $tokenPreview"
Write-Log "Project ID: $AsanaProjectId"
Write-Log "Workspace ID: $AsanaWorkspaceId"

Write-Success "All parameters validated successfully"

# =============================================
# DETECT ENVIRONMENT
# =============================================
$Environment = "development"
$WebhookBaseUrl = ""

if ($Production) {
    $Environment = "production"
    Write-Log "Mode: PRODUCTION"

    # In production, we need the backend URL
    if ([string]::IsNullOrWhiteSpace($ProductionUrl)) {
        Write-Error "In production mode you must specify the backend URL using -ProductionUrl"
        Write-Log "Example: .\setup-asana-webhook.ps1 -Production -ProductionUrl https://your-app.aws.com -AsanaToken 'your-token' -AsanaProjectId 'project-id' -AsanaWorkspaceId 'workspace-id'"
        exit 1
    }
    $WebhookBaseUrl = $ProductionUrl
} else {
    Write-Log "Mode: DEVELOPMENT"

    # Check if ngrok is running - try JSON API first, then XML as fallback
    try {
        Write-Log "Checking ngrok status..."

        $publicUrl = $null

        # Try JSON API first
        try {
            Write-Log "Trying ngrok JSON API..."
            $ngrokJsonResponse = Invoke-RestMethod -Uri "http://localhost:4040/api/tunnels" -Headers @{"Accept" = "application/json"} -ErrorAction Stop
            $publicUrl = $ngrokJsonResponse.tunnels[0].public_url
            Write-Log "Successfully got URL from JSON API"
        } catch {
            # If JSON fails, try XML
            Write-Log "JSON API failed, trying XML..."
            $ngrokXmlResponse = Invoke-WebRequest -Uri "http://localhost:4040/api/tunnels" -UseBasicParsing -ErrorAction Stop
            $xmlContent = [xml]$ngrokXmlResponse.Content
            $publicUrl = $xmlContent.tunnelListResource.Tunnels.PublicURL
            Write-Log "Successfully got URL from XML API"
        }

        if ([string]::IsNullOrWhiteSpace($publicUrl)) {
            Write-Error "Could not get valid ngrok URL"
            Write-Log "Make sure ngrok is running with a valid tunnel: .\scripts\start-ngrok.ps1"
            exit 1
        }

        $WebhookBaseUrl = $publicUrl
        Write-Success "Ngrok URL: $WebhookBaseUrl"

    } catch {
        Write-Error "ngrok is not running or not accessible"
        Write-Log "Start ngrok with: .\scripts\start-ngrok.ps1"
        Write-Log "Error details: $($_.Exception.Message)"
        exit 1
    }
}

# =============================================
# BUILD WEBHOOK URL
# =============================================

$WebhookEndpoint = "$WebhookBaseUrl/admin/ticket/webhook"
Write-Log "Webhook endpoint: $WebhookEndpoint"

# =============================================
# VERIFY ENDPOINT IS ACCESSIBLE TO ASANA
# =============================================

Write-Log "Verifying that the endpoint is accessible to Asana..."

# Asana will send a HEAD request to verify the endpoint
# Let's test if the endpoint is reachable from the outside
try {
    Write-Log "   Testing endpoint accessibility..."
    $response = Invoke-WebRequest -Uri $WebhookEndpoint -UseBasicParsing -Method Head -ErrorAction Stop
    Write-Success "Endpoint is accessible (HTTP $($response.StatusCode))"
} catch {
    Write-Warning "Endpoint may not be accessible to Asana: $($_.Exception.Message)"
    Write-Log "   Asana needs to send a HEAD request to verify the endpoint"
    Write-Log "   Make sure your endpoint accepts HEAD requests and returns 200 OK"
}

# =============================================
# TEST ASANA TOKEN BEFORE REGISTERING WEBHOOK
# =============================================

Write-Log "Testing Asana token validity..."

$Headers = @{
    "Authorization" = "Bearer $AsanaToken"
    "Content-Type" = "application/json"
}

Write-Log "Using Bearer token: $tokenPreview"

try {
    Write-Log "   Testing token with Asana API..."
    $UserInfo = Invoke-RestMethod -Uri "https://app.asana.com/api/1.0/users/me" -Headers $Headers -Method Get
    Write-Success "Asana token is valid - Connected as: $($UserInfo.data.name)"
} catch {
    Write-Error "Asana token is invalid or has insufficient permissions"
    Write-Error "Response: $($_.Exception.Message)"
    Write-Log "Please check your Asana token"
    exit 1
}

# =============================================
# REGISTER WEBHOOK IN ASANA
# =============================================

Write-Log "Registering webhook in Asana..."

# First, list existing webhooks to avoid duplicates
Write-Log "Checking existing webhooks for workspace: $AsanaWorkspaceId"

try {
    $WebhookListUrl = "https://app.asana.com/api/1.0/webhooks?workspace=$AsanaWorkspaceId"
    Write-Log "   Request URL: $WebhookListUrl"
    $ExistingWebhooksResponse = Invoke-RestMethod -Uri $WebhookListUrl -Headers $Headers -Method Get
    Write-Log "   Found $($ExistingWebhooksResponse.data.Count) webhooks in workspace"

    # Log all found webhooks for debugging
    if ($ExistingWebhooksResponse.data.Count -gt 0) {
        Write-Log "   Existing webhooks:"
        $ExistingWebhooksResponse.data | ForEach-Object {
            Write-Log "     - Webhook ID: $($_.gid), Project ID: $($_.resource.gid), Target: $($_.target)"
        }
    }
} catch {
    Write-Error "Failed to get existing webhooks: $($_.Exception.Message)"
    exit 1
}

# Check if webhook already exists for this project (regardless of URL)
$ExistingWebhookForProject = $null
foreach ($webhook in $ExistingWebhooksResponse.data) {
    if ($webhook.resource.gid -eq $AsanaProjectId) {
        $ExistingWebhookForProject = $webhook
        break
    }
}

if ($ExistingWebhookForProject) {
    Write-Warning "Webhook already exists for this project (Webhook ID: $($ExistingWebhookForProject.gid))"
    Write-Log "   Project ID: $($ExistingWebhookForProject.resource.gid)"
    Write-Log "   Current target: $($ExistingWebhookForProject.target)"
    Write-Log "Deleting existing webhook before creating new one..."

    # Delete existing webhook
    try {
        $DeleteUrl = "https://app.asana.com/api/1.0/webhooks/$($ExistingWebhookForProject.gid)"
        Write-Log "   Delete URL: $DeleteUrl"
        $DeleteResponse = Invoke-RestMethod -Uri $DeleteUrl -Headers $Headers -Method Delete
        Write-Success "Previous webhook deleted successfully"
    } catch {
        Write-Error "Could not delete existing webhook: $($_.Exception.Message)"
        Write-Log "Will try to create new webhook anyway..."
    }
} else {
    Write-Log "No existing webhook found for this project"
}

# Register new webhook
Write-Log "Registering new webhook..."

$Body = @{
    data = @{
        resource = $AsanaProjectId
        target = $WebhookEndpoint
    }
} | ConvertTo-Json

try {
    Write-Log "   Sending webhook registration request..."
    Write-Log "   Request Body: $Body"
    $WebhookResponse = Invoke-RestMethod -Uri "https://app.asana.com/api/1.0/webhooks" -Headers $Headers -Method Post -Body $Body
    Write-Success "🎉 Webhook registered successfully!"

    Write-Log ""
    Write-Log "Webhook details:"
    Write-Log "   ID: $($WebhookResponse.data.gid)"
    Write-Log "   Target: $($WebhookResponse.data.target)"
    Write-Log "   Project: $AsanaProjectId"
    Write-Log "   Workspace: $AsanaWorkspaceId"
    Write-Log "   Environment: $Environment"
    Write-Log ""

    if ($Environment -eq "development") {
        Write-Warning "REMEMBER: In development, if you restart ngrok the URL will change"
        Write-Warning "   You must run this script again with the same parameters"
    }

} catch {
    Write-Error "Error registering webhook: $($_.Exception.Message)"

    # Get detailed error information
    $errorDetails = Get-DetailedError $_
    Write-Log "Error details: $errorDetails"

    # Provide specific guidance for 403 errors
    if ($errorDetails -like "*403*" -or $_.Exception.Message -like "*403*") {
        Write-Log ""
        Write-Log "🔧 SOLUCIÓN PARA ERROR 403:"
        Write-Log "   Asana no puede acceder a tu endpoint. Posibles soluciones:"
        Write-Log "   1. Verifica que tu aplicación esté ejecutándose"
        Write-Log "   2. Asegúrate de que el endpoint /admin/ticket/webhook acepte POST requests"
        Write-Log "   3. Si usas Spring Security, verifica que la ruta no esté bloqueada por CSRF"
        Write-Log "   4. Prueba acceder manualmente a: $WebhookEndpoint"
        Write-Log "   5. Verifica que ngrok esté funcionando correctamente"
    }

    exit 1
}

# =============================================
# FINAL VERIFICATION
# =============================================

Write-Log "Performing final verification..."

# List active webhooks to confirm
Write-Log "Active webhooks in the project:"

try {
    $ActiveWebhooksUrl = "https://app.asana.com/api/1.0/webhooks?workspace=$AsanaWorkspaceId"
    $ActiveWebhooks = Invoke-RestMethod -Uri $ActiveWebhooksUrl -Headers $Headers -Method Get
    if ($ActiveWebhooks.data.Count -gt 0) {
        $ActiveWebhooks.data | ForEach-Object {
            Write-Log "   $($_.gid) -> $($_.target)"
        }
    } else {
        Write-Warning "No active webhooks found"
    }
} catch {
    Write-Warning "Could not retrieve active webhooks list: $($_.Exception.Message)"
}

Write-Success "Configuration completed!"