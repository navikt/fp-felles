#!/bin/bash

tag="$1-jakarta"
message="$2"

echo "Listing tags:"
git tag &&
  git tag -a "${tag}" HEAD -m "${message}" &&
  echo "Deploy tag: $tag" &&
  git push origin "${tag}"
