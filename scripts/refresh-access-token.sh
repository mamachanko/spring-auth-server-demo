#!/usr/bin/env bash

set -euo pipefail

usage() {
  echo "usage: $0 <refresh_token>"
  exit 64
}

REFRESH_TOKEN=${1:-""}

[ -n "$REFRESH_TOKEN" ] || usage

curl \
  --verbose \
  --user client: \
  -d grant_type=refresh_token \
  -d refresh_token="$REFRESH_TOKEN" \
  http://localhost:8080/oauth/token |
  jq .
