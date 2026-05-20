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

1. Make changes on `master` branch
2. Changes auto-deploy to staging via CI pipeline (`dev-<sha>` tags)
3. Verify on staging
4. When ready to release:

```bash
git tag v1.2.0
git push --tags
```

5. GitHub Actions builds all services, pushes images with semver tag
6. `values-production.yaml` is updated automatically
7. Sync production in ArgoCD

## Image Tagging Strategy

| Environment | Tag Format | Example |
|---|---|---|
| Staging | `dev-<short-sha>` | `dev-a13c428` |
| Production | semver | `1.2.0` |

## Local Development

Local development uses `latest` tag (default in `values.yaml`). No version management needed for local work.
