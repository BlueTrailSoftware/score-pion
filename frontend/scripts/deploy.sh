#!/bin/bash
set -e

VERSION=awk -F'"' '/"version"/ {print $4}' package.json
IMAGE_NAME="score-pion-web"
DOCKER_TAG="${IMAGE_NAME}:v${VERSION}"
TAR_NAME="${IMAGE_NAME}-v${VERSION}.tar"

PEM_PATH="${SCORE_PION_SERVER_PEM}"
SERVER_USER="ubuntu"
SERVER_HOST="34.217.125.171"
REMOTE_DIR="/home/ubuntu/tars"
CONTAINER_NAME="score-pion-web-container"

echo "Using version: $VERSION"
echo "Docker tag: $DOCKER_TAG"

docker build --platform linux/arm64 -t "$DOCKER_TAG" .
docker save -o "$TAR_NAME" "$DOCKER_TAG"

echo "Copying image to server..."
scp -i "$PEM_PATH" "$TAR_NAME" "$SERVER_USER@$SERVER_HOST:$REMOTE_DIR"

echo "Deploying on remote server..."
ssh -i "$PEM_PATH" "$SERVER_USER@$SERVER_HOST" << EOF
  sudo docker load -i $TAR_NAME
  sudo docker stop $CONTAINER_NAME || true
  sudo docker rm $CONTAINER_NAME || true
  sudo docker load -i "$REMOTE_DIR/$TAR_NAME"
  sudo docker run -d -p 4200:4200 --network webnet --name "$CONTAINER_NAME" "$DOCKER_TAG"
  sudo docker container prune -f
  sudo docker image prune -a -f
  rm $remoteDir/*
EOF

echo "Deployment of $DOCKER_TAG complete!"
