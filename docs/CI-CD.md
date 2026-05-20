# CI/CD Strategy

## Branch Model

Single branch: `master`. No `development` branch.

- Push to `master` → auto-deploys to staging
- Git tag `v*` → releases to production

## Environment Ladder

| Environment | Image Tag | Deploy Method | Trigger |
|---|---|---|---|
| Local Docker Compose | `latest` (local) | `docker-compose up` | Manual |
| Local Minikube | `latest` (local) | `helm upgrade` | Manual |
| Staging (forge-1) | `dev-<sha>` | ArgoCD auto-sync | Push to `master` |
| Production (Hetzner) | `v1.2.0` | ArgoCD manual sync | `git tag v1.2.0 && git push --tags` |

## CI Pipeline (GitHub Actions)

**Trigger:** Push to `master` that changes `apps/`, `services/`, or `infra/postgres-migrations/`.

**Steps:**
1. Build & test all services (Maven + Docker)
2. Push images to Docker Hub tagged `dev-<short-sha>`
3. Update `values-staging.yaml` with new image tag
4. Commit change to repo → ArgoCD detects and deploys

**Workflow:** `.github/workflows/ci.yml`

## Release Pipeline (GitHub Actions)

**Trigger:** Git tag matching `v*` (e.g. `v1.2.0`).

**Steps:**
1. Build & test all services
2. Push images tagged with semver (e.g. `1.2.0`)
3. Update `values-production.yaml` with version tag
4. Commit change to repo → ArgoCD shows OutOfSync on production

**Workflow:** `.github/workflows/release.yml`

## How to Release

```bash
git tag v1.2.0
git push --tags
```

Wait for the release workflow to complete, then sync production in ArgoCD.

## How to Rollback

**Staging:** Revert the `values-staging.yaml` commit in Git. ArgoCD syncs back.

**Production:** Either:
- Revert the `values-production.yaml` commit (sets previous image tag)
- Or in ArgoCD: History → select previous sync → Rollback

## Secrets Management

Secrets are excluded from ArgoCD (`resource.exclusions` in `argocd-cm`). Managed manually via:

```bash
helm upgrade portfolio ./infra/k8s/charts/portfolio \
  -f values.yaml -f secrets-staging.yaml -f values-staging.yaml \
  --namespace portfolio
```

Only needed when secrets change (rare).

## Versioning

- Single global version across all services (semver)
- Staging uses `dev-<sha>` tags (every push)
- Production uses semver tags (explicit releases)
- Version displayed in UI comes from `global.imageTag` in Helm values
