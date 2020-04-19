#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")"

cd ../api

source /docker-lib.sh
start_docker "3" "3" "" ""

docker run alpine echo ok!

./gradlew clean build --info
