#!/bin/bash

set -ex
VERSION_NUMBER=$(cd message_parser && mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
export VERSION_NUMBER
export VERSION_TAG="v$VERSION_NUMBER"

LATEST_TAG=$(curl -H \
  "Authorization: token $GITHUB_TOKEN" \
  https://api.github.com/repos/nasa/cumulus-message-adapter-java/tags | jq --raw-output '[.[].name][0]')
export LATEST_TAG

if [ "$VERSION_TAG" != "$LATEST_TAG" ]; then
  echo "tag does not exist for version $VERSION_TAG, creating tag"

  # create git tag
  git tag -a "$VERSION_TAG" -m "$VERSION_TAG" || echo "$VERSION_TAG already exists"
  git push origin "$VERSION_TAG"
fi

RELEASE_URL=$(curl -H \
  "Authorization: token $GITHUB_TOKEN" \
  https://api.github.com/repos/nasa/cumulus-message-adapter-java/releases/tags/$VERSION_TAG | jq --raw-output '.url // ""')
export RELEASE_URL

if [ -z "$RELEASE_URL" ]; then
  echo "release does not exist"

  curl -H \
    "Authorization: token $GITHUB_TOKEN" \
    -d "{\"tag_name\": \"$VERSION_TAG\", \"name\": \"$VERSION_TAG\", \"body\": \"Release $VERSION_TAG\" }"\
    -H "Content-Type: application/json"\
    -X POST \
    https://api.github.com/repos/nasa/cumulus-message-adapter-java/releases
fi
