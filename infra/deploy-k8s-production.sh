#!/bin/bash

# =================================================================================================
# Script: deploy-k8s-production.sh
# Description: Deploys or upgrades the Portfolio Monorepo to the production Kubernetes cluster.
#              Uses the production values overlay for real domains, TLS, and Traefik ingress.
#
# Usage:
#   ./deploy-k8s-production.sh [RELEASE_NAME] [NAMESPACE]
#
# Prerequisites:
#   - kubectl is configured to point to the production k3s cluster.
#   - secrets-production.yaml exists in the chart directory.
#   - cert-manager and a ClusterIssuer named 'letsencrypt-prod' are installed on the cluster.
# =================================================================================================

set -e

DEFAULT_RELEASE_NAME="portfolio"
DEFAULT_NAMESPACE="portfolio"

RELEASE_NAME=${1:-$DEFAULT_RELEASE_NAME}
NAMESPACE=${2:-$DEFAULT_NAMESPACE}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHART_DIR="$SCRIPT_DIR/k8s/charts/portfolio"
VALUES_FILE="$CHART_DIR/values.yaml"
SECRETS_FILE="$CHART_DIR/secrets-production.yaml"
PRODUCTION_FILE="$CHART_DIR/values-production.yaml"

echo "=========================================="
echo "Deploying Portfolio to PRODUCTION"
echo "  Release Name: $RELEASE_NAME"
echo "  Namespace:    $NAMESPACE"
echo "  Chart Path:   $CHART_DIR"
echo "  Values:       $VALUES_FILE"
echo "  Production:   $PRODUCTION_FILE"
echo "  Secrets:      $SECRETS_FILE"
echo "=========================================="

if [ ! -f "$SECRETS_FILE" ]; then
    echo "ERROR: Production secrets file not found at $SECRETS_FILE"
    echo "Please copy secrets-production.yaml.template to secrets-production.yaml and fill in the values."
    exit 1
fi

if [ ! -f "$PRODUCTION_FILE" ]; then
    echo "ERROR: Production values file not found at $PRODUCTION_FILE"
    exit 1
fi

# Create namespaces
echo "Creating namespaces..."
for NS in portfolio portfolio-web math-challenges shared-services infrastructure observability; do
  kubectl create namespace "$NS" --dry-run=client -o yaml | kubectl apply -f -
done

# Deploy with production overrides
helm upgrade --install "$RELEASE_NAME" "$CHART_DIR" \
  -f "$VALUES_FILE" \
  -f "$SECRETS_FILE" \
  -f "$PRODUCTION_FILE" \
  --namespace "$NAMESPACE"

echo "=========================================="
echo "Production Deployment Complete!"
echo "=========================================="