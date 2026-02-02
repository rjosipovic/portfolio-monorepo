#!/bin/bash

# =================================================================================================
# Script: build-portfolio.sh
# Description: Builds the Docker images for the Portfolio Monorepo services locally.
#              This is useful for local development to ensure all services compile and package correctly.
#
# Usage:
#   ./build-portfolio.sh
#
# Examples:
#   1. Build all services:
#      ./build-portfolio.sh
#
# Notes:
#   - This script uses 'docker compose build', which leverages Docker layer caching.
#   - It does NOT push images to a registry. Use 'publish-portfolio.sh' for that.
#   - It builds the services defined in the SERVICES variable within the script.
# =================================================================================================

# Exit immediately if a command exits with a non-zero status
set -e

# Get the absolute path of the directory where this script is located (infra/)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Get the project root directory (parent of infra/)
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Define absolute paths for configuration files
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"
ENV_FILE="$PROJECT_ROOT/.env"

# Define the list of services to build
SERVICES="user-manager challenge-manager gamification-manager notification-manager analytics-manager api-gateway frontend"

echo "=========================================="
echo "Building Portfolio Monorepo Services"
echo "Project Root: $PROJECT_ROOT"
echo "Compose File: $COMPOSE_FILE"
echo "Env File:     $ENV_FILE"
echo "Services:     $SERVICES"
echo "=========================================="

# Check if .env exists to avoid confusing docker errors
if [ ! -f "$ENV_FILE" ]; then
    echo "WARNING: .env file not found at $ENV_FILE"
fi

# Build the services using 'docker compose' (V2)
# We explicitly pass the env file and compose file using absolute paths
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" build $SERVICES

echo "=========================================="
echo "Build Complete!"
echo "=========================================="
