#!/bin/bash
set -e

#VERSION=grep 'version\s*=' build.gradle.kts | cut -d '"' -f2
VERSION=$(grep 'version *= *"' build.gradle.kts | sed -E 's/.*version *= *"([0-9.]+)".*/\1/')
IMAGE_NAME="score-pion-backend"
DOCKER_TAG="${IMAGE_NAME}:v${VERSION}"
TAR_NAME="${IMAGE_NAME}-v${VERSION}.tar"

PEM_PATH="${SCORE_PION_SERVER_PEM}"
SERVER_USER="ubuntu"
SERVER_HOST="${SCORE_PION_SERVER_HOST}"
REMOTE_DIR="/home/ubuntu/tars"
CONTAINER_NAME="score-pion-backend-container"

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
  sudo docker run -d -p 7070:7070 --name "$CONTAINER_NAME" -e SPRING_PROFILES_ACTIVE=prod "$DOCKER_TAG"
  sudo docker container prune -f
  sudo docker image prune -a -f
  rm $REMOTE_DIR/*
EOF

echo "Deployment of $DOCKER_TAG complete!"
