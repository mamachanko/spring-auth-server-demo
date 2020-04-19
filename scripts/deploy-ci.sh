#!/usr/bin/env bash

set -euxo pipefail

cd "$(dirname "$0")"

cd ../ci

if [ ! -d concourse-docker ]; then
    git clone https://github.com/concourse/concourse-docker
fi

cd concourse-docker

./keys/generate

docker-compose up --detach
