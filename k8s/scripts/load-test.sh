#!/usr/bin/env bash
set -euo pipefail

WORKERS="${WORKERS:-8}"
DURATION="${DURATION:-180}"

kubectl -n crm delete pod load-generator --ignore-not-found --wait=true
kubectl -n crm run load-generator --image=busybox:1.37 --restart=Never -- sh -c "
  end=\$((\$(date +%s) + $DURATION))
  for i in \$(seq 1 $WORKERS); do
    while [ \$(date +%s) -lt \$end ]; do
      wget -qO- http://api-gateway:8080/api/leads >/dev/null 2>&1
      wget -qO- http://api-gateway:8080/api/opportunities >/dev/null 2>&1
      wget -qO- http://api-gateway:8080/api/notifications >/dev/null 2>&1
    done &
  done
  wait"
echo "Carga iniciada por ${DURATION}s. Acompanhe com: kubectl -n crm get hpa -w"
