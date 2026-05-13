---
description: Write a session log to the Obsidian vault and commit + push the repo
---

Save the current work session to my Obsidian vault, then commit and push.

## Steps

1. **Open the vault.** Use the Obsidian MCP tools (`mcp__obsidian__*`). Confirm the vault is reachable (e.g. `mcp__obsidian__obsidian_list_files_in_dir` on `e-tipitaka-hd/`). If `e-tipitaka-hd/logs/` does not exist yet, that's fine — creating a file in it will create the folder.

2. **Build the session log.** Path: `e-tipitaka-hd/logs/YYYY-MM-DD-<short-kebab-description>.md` where the date is today and the description is a 2–5 word slug of the session's main work. If a log with that exact name already exists, append a `## Update HH:MM` section to it instead of overwriting (use `mcp__obsidian__obsidian_append_content` or `obsidian_patch_content`).

   Content template:
   ```markdown
   ---
   date: YYYY-MM-DD
   tags: [session-log, e-tipitaka-hd]
   ---

   # YYYY-MM-DD — <description>

   ## What was done
   - <bullet per concrete change / task completed>

   ## Decisions made
   - <bullet per decision + brief why>

   ## Pending / next
   - <bullet per open item, TODO, or follow-up>

   ## Touched notes
   - [[note-name]] — <what changed>
   ```

3. **Add wikilinks.** In the *Touched notes* section, link every vault note created or modified this session with `[[note-name]]`. If graphify notes were regenerated, link the relevant community overview notes (e.g. `[[_COMMUNITY_...]]`) or key node notes. Link liberally — a `[[name]]` that doesn't resolve yet is acceptable.

4. **Commit + push.** If the working directory is a git repository:
   - `git status` to see what changed.
   - If on the default branch and the change is non-trivial, branch first; otherwise stage and commit on the current branch.
   - `git add -A` (or a targeted add), then `git commit` with a concise message summarizing the session. End the commit message with:
     ```
     Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
     ```
   - `git push` (set upstream if needed).
   - If not a git repo, skip this step and say so.

5. **Report** the log path written, the wikilinks added, and the commit hash + push result (or why each was skipped).
