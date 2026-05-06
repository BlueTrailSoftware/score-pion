#!/usr/bin/env sh
cat <<EOF > public/git-hash.json
{
    "hash": "$(git rev-parse --short HEAD)"
}
EOF