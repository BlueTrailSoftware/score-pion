#!/bin/bash
# Frontend-specific Agent Skills setup for Claude Code and other AI agents.
#
# This script handles FRONTEND-ONLY skills that use Angular/TypeScript context:
# - ui-standards: UI/UX and component standards for the Angular frontend
#
# Root-level skills (sync-docs, ticket, find-skills) are configured at:
# bash scripts/setup-agent-skills.sh
#
set -e

FRONTEND_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$FRONTEND_ROOT/.agents/skills"
DST="$FRONTEND_ROOT/.claude/skills"

# Only setup ui-standards (frontend-specific)
FRONTEND_SKILLS=("ui-standards")

mkdir -p "$DST"

echo "Setting up frontend-specific Agent Skills..."
echo "  Source: $SRC"
echo "  Claude Code destination: $DST"
echo ""

for skill_name in "${FRONTEND_SKILLS[@]}"; do
  skill_dir="$SRC/$skill_name"
  [ -d "$skill_dir" ] || continue

  target="$DST/$skill_name"

  # Skip if already linked
  if [ -e "$target" ] || [ -L "$target" ]; then
    echo "  ⊘ $skill_name (already exists, skipping)"
    continue
  fi

  # Create symlink for Claude Code
  ln -s "$(realpath "$skill_dir")" "$target"
  echo "  ✅ $skill_name"
done

echo ""
echo "✓ Frontend skills ready for Claude Code"
echo "  - ui-standards: UI/UX and component standards (Angular)"
echo ""
echo "ℹ Other Agent Skills-compliant tools (Cursor, Copilot, etc.) read"
echo "  directly from .agents/skills/ — no additional setup needed."
echo ""
echo "Note: Root-level skills (sync-docs, ticket, find-skills) are configured"
echo "      separately. Run: bash scripts/setup-agent-skills.sh"
