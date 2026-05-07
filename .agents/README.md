# Agent Skills — Score Pion

This directory contains [Agent Skills](https://agentskills.io) — reusable capabilities for AI developers and agents.

## 📁 Structure

```
.agents/
├── README.md                          # This file
├── skills/                            # Root-level skills (entire monorepo)
│   ├── sync-docs/                     # Generate architectural docs
│   ├── ticket/                        # Asana ticket lifecycle
│   └── find-skills/                   # Discover and install skills
│
└── ../backend/.agents/
    └── skills/
        └── security-review/           # Backend-specific security audit
```

## 🎯 Root-Level Skills

These skills are **applicable to the entire monorepo** — they understand both backend and frontend contexts.

### sync-docs
Generates or updates `docs/roles/` and `docs/flows/` by analyzing the codebase.

- **When to use:** After adding features, changing access control, or introducing new business flows
- **Command:** `/sync-docs` (Claude Code) or invoke directly

### ticket
Manages Asana ticket lifecycle tied to your git branch.

- **When to use:** Starting work on a branch, finishing work (`/ticket close`)
- **Requires:** Asana MCP configured with a Personal Access Token
- **Command:** `/ticket` or `/ticket close` (Claude Code)
- **Setup:** See `backend/AGENTS.md` "MCP Servers" section

### find-skills
Discovers and installs skills from the open Agent Skills ecosystem.

- **When to use:** Looking for reusable capabilities (testing, deployment, design, etc.)
- **Command:** `/find-skills [query]` (Claude Code)
- **Learn more:** https://skills.sh/

## 🔐 Backend-Specific Skills

Skills that require Spring Boot + Kotlin context live in `backend/.agents/skills/`.

### security-review
OWASP/JWT/DynamoDB vulnerability audit for Spring Boot applications.

- **When to use:** Before merging security-related changes, adding auth, or accessing sensitive data
- **Command:** `/security-review` (Claude Code, when in `backend/` directory)

## 🛠️ Setup Instructions

### Claude Code
One-time setup after cloning:

```bash
# Setup root-level skills
bash scripts/setup-agent-skills.sh

# Setup backend-specific skills (optional, but recommended)
bash backend/scripts/setup-agent-skills.sh
```

This creates symlinks in `.claude/skills/` so Claude Code can discover the skills.

### Other Tools (Cursor, Copilot, Windsurf, Cline, etc.)
Agent Skills-compliant tools read directly from `.agents/skills/` — **no setup needed**.

## 📖 Using Skills in Claude Code

Once setup is complete, skills are available via slash commands:

```
User: /sync-docs
       → Claude generates architectural documentation

User: /ticket
       → Claude loads your current branch's Asana task (or creates a new one)

User: /find-skills react testing
       → Claude searches the open skills ecosystem for react testing tools
```

## 🔄 Agent Skills Standard

Score Pion follows the [Agent Skills](https://agentskills.io) standard:

- **`.agents/skills/`** — Canonical location (all tools read from here)
- **`.claude/skills/`** — Claude Code symlinks (local, not committed)
- **Script-based setup** — `setup-agent-skills.sh` (agent-agnostic)

This means:
- ✅ Tools beyond Claude Code can use these skills (Cursor, Copilot, etc.)
- ✅ New skills can be added to `.agents/skills/` and auto-discovered
- ✅ Skills work offline and don't depend on network lookups
- ✅ Entire team sees the same capabilities

## 📝 Adding New Skills

To create a new skill:

1. **Root-level (monorepo-wide):**
   ```bash
   mkdir -p .agents/skills/my-skill
   cat > .agents/skills/my-skill/SKILL.md << 'EOF'
   ---
   name: my-skill
   description: Brief description
   ---
   
   # My Skill
   
   Full skill documentation...
   EOF
   ```

2. **Backend-specific:**
   ```bash
   mkdir -p backend/.agents/skills/my-skill
   # Create backend/.agents/skills/my-skill/SKILL.md
   ```

3. **Re-run setup scripts:**
   ```bash
   bash scripts/setup-agent-skills.sh
   bash backend/scripts/setup-agent-skills.sh
   ```

4. **Commit the skill:**
   ```bash
   git add .agents/skills/my-skill/
   git commit -m "feat: add my-skill for X capability"
   ```

## 🔗 More Resources

- [Agent Skills Spec](https://agentskills.io) — Official standard and documentation
- [Skills Directory](https://skills.sh/) — Browse and discover skills
- `backend/AGENTS.md` — Backend-specific agent tooling
- `AGENTS.md` — Root-level AI developer guide
