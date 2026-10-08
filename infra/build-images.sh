#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONTEXT_DIR="${SCRIPT_DIR}/../e-commerce-platform"

IMAGE_PREFIX="${IMAGE_PREFIX:-}"
TAG="${TAG:-v1}"

SERVICES=(
  api-gateway
  product
  order
  stock
  projection
  payment
  notification
)

for service in "${SERVICES[@]}"; do
  image="${IMAGE_PREFIX}${service}:${TAG}"
  echo "==> Building ${image} from ${service}/Dockerfile"
  docker build \
    -f "${CONTEXT_DIR}/${service}/Dockerfile" \
    -t "${image}" \
    "${CONTEXT_DIR}"
done

echo "==> Built images:"
for service in "${SERVICES[@]}"; do
  echo "  ${IMAGE_PREFIX}${service}:${TAG}"
done
