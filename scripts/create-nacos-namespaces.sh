#!/usr/bin/env bash
# Create dev/test/prod namespaces for blog-base-services on Nacos (idempotent).
# Uses custom short namespace IDs so NACOS_NAMESPACE can be set to a readable value.
#
# Usage:
#   ./create-nacos-namespaces.sh [NACOS_ADDR] [USERNAME] [PASSWORD] [ENV1,ENV2,...]
# Examples:
#   ./create-nacos-namespaces.sh
#   ./create-nacos-namespaces.sh 10.0.0.1:8848 nacos secret dev,test
set -euo pipefail

NACOS_ADDR="${1:-127.0.0.1:8848}"
USERNAME="${2:-nacos}"
PASSWORD="${3:-nacos}"
IFS=',' read -ra ENVS <<< "${4:-dev,test,prod}"

BASE_URL="http://${NACOS_ADDR}/nacos"
AUTH_HEADER=()

# 1. login (skipped when Nacos auth disabled)
login_resp=$(curl -s -X POST "${BASE_URL}/v1/auth/users/login" \
    -d "username=${USERNAME}&password=${PASSWORD}" || true)
TOKEN=$(echo "${login_resp}" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
if [ -n "${TOKEN}" ]; then
    AUTH_HEADER=(-H "Authorization: Bearer ${TOKEN}")
    echo "==> auth: ok"
else
    echo "==> auth: skipped (disabled or login failed)"
fi

# 2. list existing namespaces
existing=$(curl -s -X GET "${BASE_URL}/v1/console/namespaces" "${AUTH_HEADER[@]}")
echo "${existing}" | grep -o '"namespace":"[^"]*"' | cut -d'"' -f4 > /tmp/.nacos_ns_$$.txt

for env in "${ENVS[@]}"; do
    ns_id="blog-base-${env}"
    ns_name="blog-base-${env}"
    ns_desc="blog-base-services ${env} environment"

    if grep -qx "${ns_id}" /tmp/.nacos_ns_$$.txt; then
        echo "[skip] ${ns_id} already exists"
    else
        resp=$(curl -s -X POST "${BASE_URL}/v1/console/namespaces" "${AUTH_HEADER[@]}" \
            -d "customNamespaceId=${ns_id}&namespaceName=${ns_name}&namespaceDesc=${ns_desc}")
        code=$(echo "${resp}" | sed -n 's/.*"code":\([0-9]*\).*/\1/p')
        if [ "${code}" = "0" ] || [ "${code}" = "200" ]; then
            echo "[ok]   created ${ns_id}"
        else
            echo "[fail] ${ns_id} : ${resp}"
        fi
    fi
done
rm -f /tmp/.nacos_ns_$$.txt

echo
echo "==> environment variables for application startup:"
for env in "${ENVS[@]}"; do
    printf '  %-8s NACOS_NAMESPACE=blog-base-%s\n' "${env}" "${env}"
done
