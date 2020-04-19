#!/usr/bin/env bash

set -euxo pipefail

cd "$(dirname "$0")"

cd ../ci

fly login \
  --target ci \
  --concourse-url http://localhost:8080 \
  --username test \
  --password test

fly set-pipeline \
  --target ci \
  --non-interactive \
  --pipeline cypress-demo \
  --config pipeline.yml \
  --load-vars-from secrets.yml

fly unpause-pipeline \
  --target ci \
  --pipeline cypress-demo
