# =============================================
# Ngrok Starter Script for Windows
# Starts ngrok and monitors the connection
# =============================================

# Colors for output
$ErrorActionPreference = "Stop"

function Write-Log {
    param([string]$Message)
    Write-Host "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] $Message" -ForegroundColor Cyan
}

function Write-Error {
    param([string]$Message)
    Write-Host "❌ ERROR: $Message" -ForegroundColor Red
}

function Write-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "⚠️ $Message" -ForegroundColor Yellow
}

# Global variable for ngrok process
$global:NgrokProcess = $null

# Function to cleanup on exit
function Cleanup {
    Write-Log "🛑 Stopping ngrok..."
    if ($global:NgrokProcess -and !$global:NgrokProcess.HasExited) {
        $global:NgrokProcess | Stop-Process -Force
    }
    exit 0
}

# Register cleanup on Ctrl+C
$null = Register-EngineEvent -SourceIdentifier PowerShell.Exiting -Action {
    Cleanup
}

# =============================================
# MAIN SCRIPT
# =============================================

Write-Log "🚀 Starting ngrok tunnel..."

# Check if ngrok is configured
try {
    ngrok config check | Out-Null
} catch {
    Write-Error "Ngrok is not configured"
    Write-Log "Please run first: .\scripts\setup-ngrok.ps1 -NgrokAuthtoken <your-authtoken>"
    Write-Log "Get your authtoken from: https://dashboard.ngrok.com/get-started/your-authtoken"
    exit 1
}

# Check if application is running
Write-Log "🔍 Checking if Spring Boot application is running..."

try {
    $response = Invoke-WebRequest -Uri "http://localhost:7070/actuator/health" -UseBasicParsing -ErrorAction Stop
    Write-Success "Application is running on port 7070 (actuator endpoint)"
} catch {
    Write-Error "Application is not running on port 7070"
    Write-Log "Please start your Spring Boot application first:"
    Write-Log "  .\gradlew.bat bootRun"
    Write-Log "Or if using a different port:"
    Write-Log "  .\gradlew.bat bootRun --args='--server.port=8081'"
    Write-Log "Then update this script to use the correct port"
    exit 1
}

# Start ngrok
Write-Log "🌐 Starting ngrok tunnel to port 7070..."

# Create log directory if it doesn't exist
$logDir = "logs"
if (!(Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir -Force
}

# Start ngrok and log to file
$global:NgrokProcess = Start-Process -FilePath "ngrok" -ArgumentList "http 7070" -PassThru -NoNewWindow

# Wait for ngrok to start
Write-Log "⏳ Waiting for ngrok to initialize..."
Start-Sleep -Seconds 5

# Check if ngrok started successfully
if ($global:NgrokProcess.HasExited) {
    Write-Error "Ngrok failed to start"
    Write-Log "Check ngrok output for details"
    exit 1
}

# Get ngrok URL
Write-Log "🔗 Getting ngrok public URL..."
$MaxRetries = 10
$RetryCount = 0
$NgrokUrl = $null

while ($RetryCount -lt $MaxRetries -and !$NgrokUrl) {
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:4040/api/tunnels"
        $NgrokUrl = $response.tunnels[0].public_url
        Write-Success "Ngrok tunnel established: $NgrokUrl"
    } catch {
        $RetryCount++
        Write-Log "   Retrying... ($RetryCount/$MaxRetries)"
        Start-Sleep -Seconds 2
    }
}

if (!$NgrokUrl) {
    Write-Error "Failed to get ngrok URL after $MaxRetries attempts"
    Write-Log "Check ngrok status: http://localhost:4040"
    Cleanup
}

# Display webhook information
Write-Log ""
Write-Success "🎉 Ngrok is running successfully!"
Write-Log "📋 Connection details:"
Write-Log "   Local URL: http://localhost:7070"
Write-Log "   Public URL: $NgrokUrl"
Write-Log "   Webhook endpoint: $NgrokUrl/admin/ticket/webhook"
Write-Log ""
Write-Log "📊 Monitoring:"
Write-Log "   Ngrok web interface: http://localhost:4040"
Write-Log "   Application logs: Get-Content logs\application.log -Wait"
Write-Log "   Ngrok logs: Check the console window"
Write-Log ""
Write-Log "🛑 To stop: Press Ctrl+C"
Write-Log ""

# Monitor ngrok status
Write-Log "🔍 Starting ngrok monitor..."
try {
    while ($global:NgrokProcess -and !$global:NgrokProcess.HasExited) {
        Start-Sleep -Seconds 10
        # Check if ngrok is still responding
        try {
            $response = Invoke-RestMethod -Uri "http://localhost:4040/api/tunnels" -ErrorAction Stop
        } catch {
            Write-Error "Ngrok stopped responding"
            break
        }
    }
} finally {
    Cleanup
}