# Stop on errors
$ErrorActionPreference = "Stop"

# Extract version from build.gradle.kts
$packageJsonPath = "package.json"
$packageJson = Get-Content $packageJsonPath | ConvertFrom-Json
$version = $packageJson.version

# Define variables
$imageName = "score-pion-web"
$dockerTag = "${imageName}:v${version}"
$tarName = "$imageName-v$version.tar"
$serverUser = "ubuntu"
$serverHost = "34.217.125.171"
$containerName = "score-pion-web-container"
$remoteDir = '/home/ubuntu/tars'
$pemPath = $env:SCORE_PION_SERVER_PEM

# Optional: Ensure console encoding supports Unicode
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "Using version: $version"
Write-Host "Docker tag: $dockerTag"

docker build --platform linux/arm64 -t $dockerTag .
docker save -o $tarName $dockerTag

Write-Host "Copying image to server..."
scp -i $pemPath $tarName "${serverUser}@${serverHost}:${remoteDir}"

Write-Host "Deploying on remote server..."
# Sending multi-line script to SSH via here-string
$remoteScript = @"
sudo docker load -i $tarName
sudo docker stop $containerName || echo "Container not running"
sudo docker rm $containerName || echo "No container to remove"
sudo docker load -i $remoteDir/$tarName
sudo docker run -d -p 4200:4200 --network webnet --name $containerName $dockerTag
sudo docker container prune -f
sudo docker image prune -a -f
rm $remoteDir/*
"@

ssh -i $pemPath ${serverUser}@$serverHost $remoteScript

Write-Host "Deployment of '$dockerTag' complete!"

