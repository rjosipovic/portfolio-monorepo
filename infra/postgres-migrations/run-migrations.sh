#!/bin/bash
set -e

# Wait for Postgres to be ready
# We assume the host is 'postgres' (from K8s service)
until pg_isready -h postgres -U postgres; do
  echo "Waiting for postgres..."
  sleep 2
done

echo "Postgres is up. Running migrations..."

for file in /postgres-migrations/*.sql; do
  echo "Executing $file..."
  # We use environment variables injected from the Secret
  PGPASSWORD=${POSTGRES_PASSWORD} psql -h postgres -U postgres -v ON_ERROR_STOP=1 \
  -c "SET my.user_manager_password='${POSTGRES_USER_MANAGER_PASSWORD}'" \
  -c "SET my.challenge_manager_password='${POSTGRES_CHALLENGE_MANAGER_PASSWORD}'" \
  -c "SET my.gamification_manager_password='${POSTGRES_GAMIFICATION_MANAGER_PASSWORD}'" \
  -f "$file"
done

echo "Migrations complete!"
