# =============================================
# Ngrok Setup Script for Windows
# Configures ngrok authtoken and verifies installation
# =============================================

param(
    [Parameter(Mandatory=$true)]
    [string]$NgrokAuthtoken
)

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

# =============================================
# MAIN SCRIPT
# =============================================

Write-Log "🚀 Starting ngrok setup..."

# Check if ngrok is installed
try {
    $ngrokVersion = ngrok version
    Write-Success "ngrok is installed: $ngrokVersion"
} catch {
    Write-Error "ngrok is not installed or not in PATH"
    Write-Log "Please install ngrok first:"
    Write-Log "1. Download from: https://ngrok.com/download"
    Write-Log "2. Extract ngrok.exe to a folder in your PATH"
    Write-Log "3. Or run from the download location"
    exit 1
}

Write-Log "🔑 Configuring ngrok authtoken..."

# Configure ngrok authtoken
try {
    $process = Start-Process -FilePath "ngrok" -ArgumentList "config add-authtoken $NgrokAuthtoken" -Wait -PassThru -NoNewWindow
    if ($process.ExitCode -eq 0) {
        Write-Success "Ngrok authtoken configured successfully"
    } else {
        throw "Process exited with code $($process.ExitCode)"
    }
} catch {
    Write-Error "Failed to configure ngrok authtoken: $($_.Exception.Message)"
    Write-Log "Please check your token and try again"
    exit 1
}

# Verify configuration
Write-Log "🔍 Verifying ngrok configuration..."

try {
    ngrok config check
    Write-Success "Ngrok configuration is valid"
} catch {
    Write-Error "Ngrok configuration check failed"
    exit 1
}

# Test ngrok functionality
Write-Log "🧪 Testing ngrok functionality..."

try {
    # Start ngrok in background for testing
    $ngrokProcess = Start-Process -FilePath "ngrok" -ArgumentList "http 8080" -PassThru
    
    # Wait for ngrok to start
    Start-Sleep -Seconds 3
    
    # Check if ngrok is running
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:4040/api/tunnels"
        Write-Success "Ngrok is working correctly"
        
        # Get the public URL
        $ngrokUrl = $response.tunnels[0].public_url
        Write-Log "🌐 Your ngrok URL: $ngrokUrl"
    } catch {
        Write-Error "Ngrok is not responding"
    }
    
    # Stop ngrok
    Stop-Process -Id $ngrokProcess.Id -Force -ErrorAction SilentlyContinue
    
} catch {
    Write-Error "Failed to test ngrok: $($_.Exception.Message)"
}

Write-Log ""
Write-Success "🎉 Ngrok setup completed successfully!"
Write-Log "📋 Next steps:"
Write-Log "   1. Start your Spring Boot application"
Write-Log "   2. Run: .\scripts\start-ngrok.ps1"
Write-Log "   3. Run: .\scripts\setup-asana-webhook.ps1"