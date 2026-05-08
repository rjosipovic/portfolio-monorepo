#!/bin/bash

# =================================================================================================
# Script: run-portfolio.sh
# Description: Starts the entire Portfolio Monorepo environment using Docker Compose.
#              This includes all infrastructure (databases, brokers) and application services.
#
# Usage:
#   ./run-portfolio.sh [COMMAND]
#
# Arguments:
#   COMMAND (Optional) Any valid 'docker compose' command, e.g., 'down', 'logs -f'.
#           Defaults to 'up -d' if no command is provided.
#
# Examples:
#   1. Start the environment in detached mode:
#      ./run-portfolio.sh
#
#   2. Stop the environment:
#      ./run-portfolio.sh down
#
#   3. Follow the logs of all services:
#      ./run-portfolio.sh logs -f
#
#   4. Follow the logs of a specific service:
#      ./run-portfolio.sh logs -f user-manager
#
# Notes:
#   - Uses '-p portfolio' to ensure a consistent project name, preventing duplicate containers.
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
PROJECT_NAME="portfolio"

# Use 'up -d' as the default command if no arguments are provided
COMMAND=${@:-up -d}

echo "=========================================="
echo "Executing Docker Compose Command"
echo "Project Root: $PROJECT_ROOT"
echo "Compose File: $COMPOSE_FILE"
echo "Env File:     $ENV_FILE"
echo "Project Name: $PROJECT_NAME"
echo "Command:      $COMMAND"
echo "=========================================="

# Check if .env exists
if [ ! -f "$ENV_FILE" ]; then
    echo "WARNING: .env file not found at $ENV_FILE"
fi

# Run the docker compose command
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" -p "$PROJECT_NAME" $COMMAND

echo "=========================================="
echo "Command finished!"
echo "=========================================="
