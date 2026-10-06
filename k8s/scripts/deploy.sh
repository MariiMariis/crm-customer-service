#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
OVERLAY="${1:-prod}"

OBSERVABILITY="$ROOT/observability"
if kubectl -n observability get secret splunk-observability >/dev/null 2>&1; then
  SPLUNK_REALM="$(kubectl -n observability get secret splunk-observability -o jsonpath='{.data.realm}' | base64 -d)"
  helm repo add splunk-otel-collector-chart https://signalfx.github.io/splunk-otel-collector-chart --force-update
  helm upgrade --install splunk-otel-collector splunk-otel-collector-chart/splunk-otel-collector \
    --version 0.161.0 \
    --namespace observability \
    --values "$ROOT/observability-splunk/values.yaml" \
    --set splunkObservability.realm="$SPLUNK_REALM" \
    --wait --timeout 10m
  OBSERVABILITY="$ROOT/observability-splunk"
fi

kubectl apply -k "$OBSERVABILITY"
kubectl -n observability rollout status deployment/otel-collector --timeout=300s

kubectl -n crm delete job crm-seeder --ignore-not-found
kubectl apply -k "$ROOT/k8s/overlays/$OVERLAY"

kubectl -n crm rollout status statefulset --timeout=300s
for deployment in config-server team-service accounts-service catalog-service sales-service notification-service api-gateway frontend; do
  kubectl -n crm rollout status "deployment/$deployment" --timeout=600s
done
kubectl -n crm wait --for=condition=complete job/crm-seeder --timeout=600s
kubectl -n crm get pods -o wide
kubectl -n observability rollout status statefulset --timeout=300s
kubectl -n observability rollout status deployment/grafana --timeout=300s
echo "Nexo disponivel em http://crm.localhost"
echo "Grafana disponivel em http://grafana.crm.localhost"
