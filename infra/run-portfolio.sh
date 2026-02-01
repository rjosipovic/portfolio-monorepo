#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Get the absolute path of the directory where this script is located (infra/)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Get the project root directory (parent of infra/)
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Define absolute paths for configuration files
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"
ENV_FILE="$PROJECT_ROOT/.env"
PROJECT_NAME="portfolio"

echo "=========================================="
echo "Starting Portfolio Monorepo Environment"
echo "Project Root: $PROJECT_ROOT"
echo "Compose File: $COMPOSE_FILE"
echo "Env File:     $ENV_FILE"
echo "Project Name: $PROJECT_NAME"
echo "=========================================="

# Check if .env exists
if [ ! -f "$ENV_FILE" ]; then
    echo "WARNING: .env file not found at $ENV_FILE"
fi

# Start the environment
# -p: Sets the project name (so it doesn't default to 'infra' or 'portfolio-monorepo' randomly)
# -d: Detached mode
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" -p "$PROJECT_NAME" up -d

echo "=========================================="
echo "Environment Started!"
echo "Use 'docker compose -p $PROJECT_NAME logs -f' to follow logs."
echo "=========================================="
