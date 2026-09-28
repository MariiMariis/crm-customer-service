#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
CLUSTER="${CLUSTER:-nexo-crm}"
TAG="${TAG:-latest}"
IMAGES=(config-server team-service accounts-service catalog-service sales-service notification-service api-gateway frontend seeder)

TAG="$TAG" docker compose -f "$ROOT/docker-compose.full.yml" build
for image in "${IMAGES[@]}"; do
  kind load docker-image "mariimariis/crm-$image:$TAG" --name "$CLUSTER"
done
