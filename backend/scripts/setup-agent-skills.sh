#!/bin/bash
# Backend-specific Agent Skills setup for Claude Code and other AI agents.
#
# This script handles BACKEND-ONLY skills that use Spring Boot/Kotlin context:
# - security-review: OWASP/JWT/DynamoDB audit (Kotlin + Spring Boot)
#
# Root-level skills (sync-docs, ticket, find-skills) are configured at:
# bash scripts/setup-agent-skills.sh
#
set -e

BACKEND_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$BACKEND_ROOT/.agents/skills"
DST="$BACKEND_ROOT/.claude/skills"

# Only setup security-review (backend-specific)
BACKEND_SKILLS=("security-review")

mkdir -p "$DST"

echo "Setting up backend-specific Agent Skills..."
echo "  Source: $SRC"
echo "  Claude Code destination: $DST"
echo ""

for skill_name in "${BACKEND_SKILLS[@]}"; do
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
echo "✓ Backend skills ready for Claude Code"
echo "  - security-review: OWASP/JWT/DynamoDB audit (Spring Boot + Kotlin)"
echo ""
echo "ℹ Other Agent Skills-compliant tools (Cursor, Copilot, etc.) read"
echo "  directly from .agents/skills/ — no additional setup needed."
echo ""
echo "Note: Root-level skills (sync-docs, ticket, find-skills) are configured"
echo "      separately. Run: bash scripts/setup-agent-skills.sh"
