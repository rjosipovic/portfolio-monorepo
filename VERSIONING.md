# Versioning

This project uses a single global version for all services. All images are tagged with the same version on each release.

## Semantic Versioning

Format: `MAJOR.MINOR.PATCH`

| Bump | When |
|---|---|
| **Major** | Breaking API changes, major architecture changes, non-backward-compatible data migrations |
| **Minor** | New features, new endpoints, new services, non-breaking changes |
| **Patch** | Bug fixes, config changes, dependency updates, dashboard fixes |

## Release Workflow

1. Make changes on `development` branch
2. Determine version bump based on changes
3. Build and publish images:
   bash
   ./infra/publish-portfolio.sh <version>

4. Update imageTag in infra/k8s/charts/portfolio/values-production.yaml
5. Deploy to production:
   bash
   kubectl config use-context production
   ./infra/deploy-k8s-production.sh


## Local Development

Local development uses latest tag (default in values.yaml). No version management needed for local work.
