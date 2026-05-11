#!/bin/bash
# Root-level Agent Skills setup.
#
# This script is AGENT-AGNOSTIC and works with any tool that supports Agent Skills:
# - Tools with .claude/skills/ support (e.g. Claude Code): symlinks skills there automatically
# - Cursor, Copilot, Windsurf, etc.: read directly from .agents/skills/, no setup needed
#
# Root-level skills: sync-docs, ticket, find-skills (applicable to entire monorepo)
# Backend-specific skills: see backend/scripts/setup-agent-skills.sh
#
set -e

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$REPO_ROOT/.agents/skills"
DST="$REPO_ROOT/.claude/skills"

mkdir -p "$DST"

echo "Setting up root-level Agent Skills..."
echo "  Source: $SRC"
echo "  Destination: $DST"
echo ""

for skill_dir in "$SRC"/*/; do
  [ -d "$skill_dir" ] || continue
  name=$(basename "$skill_dir")
  target="$DST/$name"

  if [ -e "$target" ] || [ -L "$target" ]; then
    echo "  ⊘ $name (already exists, skipping)"
    continue
  fi

  ln -s "$(realpath "$skill_dir")" "$target"
  echo "  ✅ $name"
done

echo ""
echo "✓ Root skills ready"
echo "  - sync-docs: Generate docs/roles/ and docs/flows/"
echo "  - ticket: Manage Asana lifecycle"
echo "  - find-skills: Discover and install skills"
echo ""
echo "ℹ Other Agent Skills-compliant tools (Cursor, Copilot, etc.) read"
echo "  directly from .agents/skills/ — no additional setup needed."
echo ""
echo "Note: Backend-specific skills (security-review) are configured separately."
echo "      Run: bash backend/scripts/setup-agent-skills.sh"
