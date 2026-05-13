---
description: Reload context from the latest Obsidian session logs and summarize state
---

Reload working context from my Obsidian vault's recent session logs.

## Steps

1. **Open the vault.** Use the Obsidian MCP tools (`mcp__obsidian__*`). List `e-tipitaka-hd/logs/` with `mcp__obsidian__obsidian_list_files_in_dir`. If the folder is missing or empty, say so and stop.

2. **Pick the 3 most recent logs.** Log filenames are `YYYY-MM-DD-<description>.md`, so sort by name descending and take the top 3 (if fewer exist, take what's there). Read them with `mcp__obsidian__obsidian_batch_get_file_contents` (or `obsidian_get_file_contents` each).

3. **Follow key wikilinks.** If a log's *Touched notes* / *Pending* sections reference notes that matter for understanding current state, optionally read those too (don't go more than one hop deep).

4. **Summarize** in chat, concise:
   - **Current state** — what the project / branch is at now, per the logs.
   - **Recent work** — what got done across those sessions (most recent first).
   - **Open items** — consolidated list of pending tasks / TODOs / follow-ups still outstanding (dedupe items resolved in a later log).
   - **Suggested next step** — the single most logical thing to pick up.

Do not modify the vault or the repo — this command is read-only.
