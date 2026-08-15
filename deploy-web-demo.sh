#!/bin/bash
set -e

cd "$(dirname "$0")"

if [ ! -d gh-pages ]; then
    echo "gh-pages worktree missing, setting it up..."
    git fetch origin gh-pages
    if git show-ref --verify --quiet refs/heads/gh-pages; then
        git worktree add gh-pages gh-pages
    else
        git worktree add -b gh-pages gh-pages origin/gh-pages
    fi
elif ! git -C gh-pages rev-parse --git-dir >/dev/null 2>&1; then
    echo "gh-pages exists but isn't a git worktree, refusing to touch it." >&2
    exit 1
fi

./gradlew demo:web:dist

rm -rf gh-pages/assets gh-pages/html gh-pages/icon32.png gh-pages/index.html
cp -r demo/web/build/dist/assets demo/web/build/dist/html demo/web/build/dist/icon32.png demo/web/build/dist/index.html gh-pages/

cd gh-pages
git add -A
git commit -m "Web demo update"
git push
