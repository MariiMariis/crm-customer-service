#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
OVERLAY="${1:-prod}"

kubectl -n crm delete job crm-seeder --ignore-not-found
kubectl apply -k "$ROOT/k8s/overlays/$OVERLAY"

kubectl -n crm rollout status statefulset --timeout=300s
for deployment in config-server team-service accounts-service catalog-service sales-service notification-service api-gateway frontend; do
  kubectl -n crm rollout status "deployment/$deployment" --timeout=600s
done
kubectl -n crm wait --for=condition=complete job/crm-seeder --timeout=600s
kubectl -n crm get pods -o wide
echo "Nexo disponivel em http://crm.localhost"
