#!/bin/bash

# =================================================================================================
# Script: publish-portfolio.sh
# Description: Builds and pushes Docker images for the Portfolio Monorepo services to a registry.
#
# Usage:
#   ./publish-portfolio.sh [TAG] [DOCKER_USER]
#
# Arguments:
#   TAG          (Optional) The image tag/version. Defaults to 'latest'.
#   DOCKER_USER  (Optional) The Docker Hub username or registry prefix. Defaults to 'rjosipovic'.
#
# Examples:
#   1. Build and push 'latest' to default user 'rjosipovic':
#      ./publish-portfolio.sh
#
#   2. Build and push version 'v1.0.0' to default user:
#      ./publish-portfolio.sh v1.0.0
#
#   3. Build and push version 'v1.0.0' to a custom user/org 'myorg':
#      ./publish-portfolio.sh v1.0.0 myorg
#
# Prerequisites:
#   - You must be logged in to Docker Hub (run `docker login`).
#   - A .env file should exist in the project root (to silence variable warnings).
# =================================================================================================

# Exit immediately if a command exits with a non-zero status
set -e

# --- Configuration ---
# The Docker Hub username or registry prefix.
# Change this default or pass it as the second argument.
DEFAULT_USER="rjosipovic"

# The default tag to use if none is provided.
DEFAULT_TAG="latest"

# List of services from docker-compose.yml to build and publish.
SERVICES="user-manager challenge-manager gamification-manager notification-manager analytics-manager api-gateway frontend"
# --- End Configuration ---

# Parse arguments:
# $1 is the tag (e.g., v1.0.0), defaults to 'latest'.
# $2 is the Docker user/organization, defaults to 'rjosipovic'.
TAG=${1:-$DEFAULT_TAG}
DOCKER_USER=${2:-$DEFAULT_USER}

# Get the absolute path of the directory where this script is located (infra/)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"
ENV_FILE="$PROJECT_ROOT/.env"

echo "=========================================="
echo "Publishing Portfolio Monorepo Services"
echo "  Docker Registry/User: $DOCKER_USER"
echo "  Tag:                  $TAG"
echo "  Services:             $SERVICES"
echo "  Env File:             $ENV_FILE"
echo "=========================================="

# Check if .env exists to avoid confusing docker errors
if [ ! -f "$ENV_FILE" ]; then
    echo "WARNING: .env file not found at $ENV_FILE. You may see variable substitution warnings."
fi

# Export variables so docker-compose can use them to name the images.
# e.g., image: ${DOCKER_USER}/user-manager:${TAG}
export DOCKER_USER
export TAG

# 1. Build the images with the specified user and tag.
echo "Building images..."
# We include --env-file to silence warnings about missing variables,
# even though build/push don't strictly need the secrets.
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" build $SERVICES

# 2. Push the images to the registry.
# You must be logged in first (e.g., `docker login`).
echo "Pushing images..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" push $SERVICES

echo "=========================================="
echo "Publish Complete!"
echo "Images pushed to: ${DOCKER_USER}/<service-name>:${TAG}"
echo "=========================================="
