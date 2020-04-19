#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")"

main() {
  build
  push
  celebrate
}

build() {
  echo ""
  echo "👷🏻‍♀️ let's build"
  echo ""

  ./build.sh
}

push() {
  echo ""
  echo "🆙 let's push"
  echo ""

  git push
}

celebrate() {
  echo ""
  echo "🚢 successfully shipped:"
  echo "🍾"
  echo "🍾     $(git show -s --format=oneline @)"
  echo "🍾"
  echo ""
}

main
