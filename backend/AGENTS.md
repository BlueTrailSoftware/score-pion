# Score Pion Webhook

## Build & Development Commands

```bash
# Auto-generate/update docs/roles/ and docs/flows/
# (If using an Agent Skills client)
/sync-docs
```

## Agent Skills

This project follows the [Agent Skills](https://agentskills.io) standard. Skills are organized by scope:

**Root-level skills** (applicable to entire monorepo):
- `sync-docs` — Generates/updates `docs/roles/` and `docs/flows/` by analyzing both backend and frontend
- `ticket` — Asana ticket lifecycle (load/create/close). Requires the Asana MCP (see below).
- `find-skills` — Discover and install skills from the open Agent Skills ecosystem

**Backend-specific skills** (Spring Boot + Kotlin context):
- `security-review` — OWASP / GDPR / JWT / DynamoDB injection audit for Spring Boot + Kotlin

### Setup Instructions

**For Claude Code users:**

```bash
# Setup ROOT-level skills (one-time after cloning)
bash scripts/setup-agent-skills.sh

# Setup BACKEND-specific skills (if working in backend/)
bash backend/scripts/setup-agent-skills.sh
```

This creates symlinks in `.claude/skills/` for Claude Code. Other Agent Skills-compliant tools (Cursor, Copilot, Windsurf, etc.) read directly from `.agents/skills/` — no additional setup needed.

## MCP Servers

This project uses [Model Context Protocol](https://modelcontextprotocol.io) servers for external integrations. MCP is an open standard supported by Claude Code, Cursor, Cline, Continue, Windsurf, Zed, and other agents.

### Asana MCP (required for the `ticket` skill)

**Prerequisites:** Node.js 22.12+ (the MCP server uses `--experimental-vm-modules`; Node 20 fails at startup).

**1. Get an Asana Personal Access Token**
Asana → Profile → My Settings → Apps → Manage developer apps → Personal access tokens → Create new token.

**2. Configure the MCP server in your agent**
Copy the root `.mcp.json.example` to the config path your agent reads, then replace `YOUR_ASANA_PAT_HERE` with your token. The config file is gitignored.

| Agent | Config path |
|-------|-------------|
| Claude Code | `.mcp.json` (project root) |
| Cursor | `.cursor/mcp.json` |
| Cline (VS Code) | settings UI or `.vscode/mcp.json` |
| Others | see your agent's MCP docs |

**macOS / Linux** — works as-is with `npx`:
```json
{ "mcpServers": { "asana": { "command": "npx", "args": ["-y", "@roychri/mcp-server-asana"], "env": { "ASANA_ACCESS_TOKEN": "your-token" } } } }
```

**Windows** — `npx` must be invoked via `cmd /c`:
```json
{ "mcpServers": { "asana": { "command": "cmd", "args": ["/c", "npx", "-y", "@roychri/mcp-server-asana"], "env": { "ASANA_ACCESS_TOKEN": "your-token" } } } }
```

**3. Restart your agent** so it picks up the new MCP server.

### Branch naming convention (used by the `ticket` skill)

`{type}/{title-slug}-{full-asana-gid}`

Examples: `feature/add-pagination-to-applicant-list-1208234567890123`, `fix/auth0-token-refresh-1208234567341209`.
