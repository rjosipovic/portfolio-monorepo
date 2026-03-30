#!/bin/bash

# =================================================================================================
# Script: deploy-k8s.sh
# Description: Deploys or upgrades the Portfolio Monorepo to Kubernetes using Helm.
#              This script is idempotent, using 'helm upgrade --install'.
#
# Usage:
#   ./deploy-k8s.sh [RELEASE_NAME] [NAMESPACE]
#
# Arguments:
#   RELEASE_NAME (Optional) The Helm release name. Defaults to 'portfolio'.
#   NAMESPACE    (Optional) The Kubernetes namespace. Defaults to 'portfolio'.
#
# Examples:
#   1. Deploy/upgrade the 'portfolio' release to the 'portfolio' namespace:
#      ./deploy-k8s.sh
#
#   2. Deploy/upgrade a different release name or to a different namespace:
#      ./deploy-k8s.sh my-test-release my-test-ns
#
# Prerequisites:
#   - Helm CLI is installed.
#   - 'kubectl' is configured to point to the target Kubernetes cluster.
#   - A 'secrets.yaml' file exists in the chart directory (copy secrets.yaml.template and fill in values).
# =================================================================================================

# Exit immediately if a command exits with a non-zero status
set -e

# --- Configuration ---
DEFAULT_RELEASE_NAME="portfolio"
DEFAULT_NAMESPACE="portfolio"
# --- End Configuration ---

# Parse arguments
RELEASE_NAME=${1:-$DEFAULT_RELEASE_NAME}
NAMESPACE=${2:-$DEFAULT_NAMESPACE}

# Get the absolute path of the directory where this script is located (infra/)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHART_DIR="$SCRIPT_DIR/k8s/charts/portfolio"
VALUES_FILE="$CHART_DIR/values.yaml"
SECRETS_FILE="$CHART_DIR/secrets.yaml"

echo "=========================================="
echo "Deploying Portfolio to Kubernetes"
echo "  Release Name: $RELEASE_NAME"
echo "  Namespace:    $NAMESPACE"
echo "  Chart Path:   $CHART_DIR"
echo "  Values File:  $VALUES_FILE"
echo "  Secrets File: $SECRETS_FILE"
echo "=========================================="

# Check if secrets.yaml exists
if [ ! -f "$SECRETS_FILE" ]; then
    echo "ERROR: Secrets file not found at $SECRETS_FILE"
    echo "Please copy secrets.yaml.template to secrets.yaml and fill in the values."
    exit 1
fi

# Run the Helm command
helm upgrade --install "$RELEASE_NAME" "$CHART_DIR" \
  -f "$VALUES_FILE" \
  -f "$SECRETS_FILE" \
  --create-namespace \
  --namespace "$NAMESPACE"

echo "=========================================="
echo "Deployment Complete!"
echo "Run 'kubectl get all -n $NAMESPACE' to see the status."
echo "=========================================="
