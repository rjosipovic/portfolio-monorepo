# Helm Chart Improvements

## Critical
- [x] Move secrets.yaml out of repo (gitignore + document external supply method)

## High
- [x] Add resource requests/limits to all containers
- [x] Add liveness/readiness probes to infrastructure deployments (postgres, rabbitmq, redis, neo4j, pgadmin)
- [x] Fix hook-delete-policy to include hook-failed on migrations Job
- [x] Add backoffLimit to migrations Job

## Medium
- [x] Add PersistentVolumeClaims for postgres, rabbitmq, redis, neo4j (or add a comment acknowledging ephemeral storage is intentional)
- [ ] Fix imagePullPolicy: Always for latest-tagged images
- [x] Add missing SERVICE_PORT / MANAGEMENT_PORT to challenge-manager-config and gamification-manager-config
- [x] Remove rewrite-target annotation from Ingress

## Low
- [ ] Standardize labels across all resources (add missing labels on Service/Job metadata)
- [ ] Add named ports to infrastructure deployments (rabbitmq, neo4j) for consistency with app deployments
