#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")"

./refresh-access-token.sh "$(./get-access-token.sh | jq --raw-output .refresh_token)"

echo ""
echo "🔒 access token work"
echo "♻️  refresh token work"
echo ""
