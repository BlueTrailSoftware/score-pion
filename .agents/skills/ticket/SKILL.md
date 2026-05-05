---
name: ticket
description: Manages the Asana ticket lifecycle tied to the current git branch. Use when starting work on a branch (loads or creates a ticket and optionally renames the branch with the ticket GID) or when finishing work (`/ticket close` marks the ticket complete and proposes a PR title). Requires the Asana MCP.
allowed-tools: Bash mcp__asana__asana_get_task mcp__asana__asana_create_task mcp__asana__asana_update_task
compatibility: Requires an Asana MCP server configured in the host agent. See AGENTS.md "MCP Servers".
---

# ticket

Manages the Asana ticket lifecycle for the current branch.

> **Prerequisite:** Requires the Asana MCP configured locally with a valid Personal Access Token.
> See the "MCP Servers" section in AGENTS.md for setup instructions.

## Detect invocation mode

Run `git branch --show-current` to get the current branch name.

If the branch is `main`, `master`, `develop`, or `dev`: warn the user they should be on a feature branch and stop.

Select the mode:

- If `$ARGUMENTS` equals `close` (case-insensitive) → **Close Mode**
- Else if the branch name ends with a dash followed by digits (e.g. `feature/add-pagination-1208234567890123`) → **Load Mode**
- Otherwise → **Create Mode**

---

## Load Mode — Branch already has a ticket ID

Extract the full GID (trailing digits after the last dash).

Use the Asana MCP to fetch the task directly by GID:
1. Look up the task using the extracted GID
2. Fetch full task details: name, description, status, assignee, due date, and latest comments

Present a clear summary to the developer:
- What is this ticket about?
- What is its current status?
- Who is assigned?
- Are there any notes or blockers?

Make this context available for the rest of the session.

---

## Create Mode — Branch has no ticket ID

### Step 1 — Detect type from branch name

Check if the branch name starts with one of the known prefixes: `feature/`, `fix/`, `refactor/`, `chore/`, `docs/`.

- If a known prefix is found → **auto-set the type** from the prefix.
- If the branch name has no recognizable prefix → **ask the user to pick** by presenting a numbered list:

```
What type of work is this?
  1. feature
  2. fix
  3. refactor
  4. chore
  5. docs
```

Wait for the user to reply with a number or the word, then continue.

### Step 2 — Infer title and description from branch slug

Strip the type prefix and any trailing GID from the branch name to get the slug (e.g., `feature/integrate-asana-mcp-1208234567890123` → `integrate-asana-mcp`).

From the slug, generate:
- **Title**: Convert the slug to a natural English sentence in imperative mood, title-cased. Keep it under 60 chars.
- **Description**: Write 2–4 sentences describing what this work likely involves, what problem it solves, and a rough acceptance criteria. Base this on the branch name context and the project domain (score-pion is a recruitment platform with backend API and Angular frontend).

### Step 3 — Present suggestions and collect feedback

Show the inferred values **and** ask for the two remaining fields in a **single message**:

```
Here's what I inferred from the branch name — adjust anything or confirm as-is:

1. **Type:** {type}
2. **Title:** {inferred title}
3. **Description:**
{inferred description}

---
4. **Assignee:** Who will work on this? (default: yourself)
5. **Due date:** Any deadline? (optional, press Enter to skip)
```

The user can reply with corrections inline or confirm everything. Apply any corrections before creating the task.

Read `TICKET_MANAGER_PROJECT_ID` and `TICKET_MANAGER_WORKSPACE_ID` from the repo's `.env` file. If either is missing, prompt the user for the value.

Then create the task in Asana via MCP:
- Project: use `TICKET_MANAGER_PROJECT_ID`
- Workspace: use `TICKET_MANAGER_WORKSPACE_ID`
- For assignee: pass `"me"` if the user chose default (themselves), or the provided user GID/email if they named someone else
- If the creation response has no `assignee` set, immediately call update_task: use `followers[0].gid` when assigning to yourself, or the value the user provided for someone else

After successful creation:
- Show the task GID and Asana URL
- Generate a branch name following the convention: `{type}/{title-slug}-{full-GID}`
  - Slug: title lowercased, spaces → hyphens, special characters removed
  - Example: title "Add pagination to applicant list", GID `1208234567890123` → `feature/add-pagination-to-applicant-list-1208234567890123`
- Ask: "Do you want to rename the current branch to `{proposed-name}`?"
  - If yes: run `git branch -m {proposed-name}`
  - If no: continue without renaming

---

## Close Mode — Mark ticket as complete

Triggered by `/ticket close`.

### Step 1 — Detect ticket ID

Extract the full GID from the branch name (trailing digits after the last dash).

If no GID is found, inform the user that the branch has no linked ticket and suggest running `/ticket` first. Stop.

### Step 2 — Verify the ticket

Fetch the task from Asana by GID. Show the task name and current status to confirm this is the right ticket before closing it.

### Step 3 — Mark as complete

Update the task in Asana via MCP to mark it as completed.

### Step 4 — Suggest PR title

Propose a PR title following the convention:
`[TYPE] {ticket title} #{full-GID}`

Example: `[FEATURE] Add pagination to applicant list #1208234567890123`

Ask the developer if they want to proceed to create the PR with this title.
