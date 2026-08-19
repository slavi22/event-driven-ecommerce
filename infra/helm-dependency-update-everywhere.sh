#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
helm_dir="$script_dir/helm"

while IFS= read -r -d '' chart_yaml; do
  chart_dir="$(dirname "$chart_yaml")"
  echo "==> helm dependency update $chart_dir"
  helm dependency update "$chart_dir"
done < <(find "$helm_dir" -name "Chart.yaml" -print0)
