#!/usr/bin/env bash
set -euo pipefail

HOST="${HOST:-crm.localhost}"
GRAFANA_HOST="${GRAFANA_HOST:-grafana.crm.localhost}"
TIMEOUT="${TIMEOUT:-300}"
RESOLVE=(--resolve "$HOST:80:127.0.0.1" --resolve "$GRAFANA_HOST:80:127.0.0.1")

deadline=$(( $(date +%s) + TIMEOUT ))
until health=$(curl -sf "${RESOLVE[@]}" "http://$HOST/api/platform/health") && grep -q '"status":"UP","checkedAt"' <<<"$health"; do
  if [ "$(date +%s)" -ge "$deadline" ]; then
    echo "Plataforma nao ficou saudavel em ${TIMEOUT}s"
    echo "$health"
    exit 1
  fi
  echo "Aguardando a plataforma ficar UP..."
  sleep 10
done
echo "Saude da plataforma: UP"
grep -o '"name":"[^"]*","url":"[^"]*","status":"[^"]*"' <<<"$health" | sed -E 's/"name":"([^"]*)".*"status":"([^"]*)"/  \1: \2/'

status=$(curl -s -o /dev/null -w "%{http_code}" "${RESOLVE[@]}" "http://$HOST/")
[ "$status" = "200" ] || { echo "Frontend respondeu $status"; exit 1; }
echo "Frontend: HTTP $status"

status=$(curl -s -o /dev/null -w "%{http_code}" "${RESOLVE[@]}" "http://$HOST/api/sales-reps")
[ "$status" = "200" ] || { echo "API via gateway respondeu $status"; exit 1; }
echo "API via gateway: HTTP $status"

status=$(curl -s -o /dev/null -w "%{http_code}" "${RESOLVE[@]}" "http://$GRAFANA_HOST/api/health")
[ "$status" = "200" ] || { echo "Grafana respondeu $status"; exit 1; }
echo "Grafana: HTTP $status"

echo "Smoke test concluido com sucesso"
