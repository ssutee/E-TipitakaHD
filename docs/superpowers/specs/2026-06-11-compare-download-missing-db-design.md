# Compare → download missing database → continue — Design

**Date:** 2026-06-11
**Status:** Approved (design)

## Problem

When the user starts a comparison against a target language whose database
file is not present on disk, both compare entry points currently just show a
`database_not_found` toast and abandon the action. The user has to leave the
reader, open the language menu, download that database manually, navigate
back, and restart the comparison from scratch.

## Goal

When the user picks a compare target whose database is missing:

1. Ask the user whether to download it (confirmation dialog).
2. On confirm, download it with a modal progress dialog.
3. On success, automatically resume the exact comparison that was interrupted.

Applies to **both** compare entry points.

## Scope

In scope:

- `MainActivity` reader-compare flow (compare from the reading screen).
- `ComparisonActivity` compare-again flow (picking another language while
  already in the split-compare view).
- A reusable confirm + download-with-progress helper in `DownloadDatabase.kt`.

Out of scope:

- Changing the existing `download()` used by `MenuFragment` (it ships in
  production; leave it untouched to avoid regressions).
- Any layout/XML changes (progress UI is built programmatically).
- New string resources (all required strings already exist).
- Parallel / background download, retry queues, or download cancellation
  beyond dismissing on activity teardown.

## Existing building blocks (reused)

`DownloadDatabase.kt` already factors the download into independent suspend
helpers, which the new code reuses verbatim:

- `isThaiClient(context)` — host selection (`Constants.THAI_HOST` vs
  `Constants.S3_HOST`).
- `downloadDatabaseZipFile(activity, url, path, progressBar)` — drives the
  ProgressBar 0–100% via `FileDownloader`.
- `unzipDatabase(context, path)` — extracts and deletes the zip.

Strings already present (used today by `MenuFragment` / `download()`):
`database_not_found`, `confirm_download_database`, `download`, `cancel`,
`downloading`, `download_complete`, `download_error`, `space_error`,
`no_internet_connection`.

## Design

### 1. `DownloadDatabase.kt` — two new functions

**`confirmAndDownloadDatabase(activity, language, onSuccess)`** — Java-callable
entry point.

- If `!Utils.isNetworkConnected(activity)` → toast `no_internet_connection`,
  return without invoking `onSuccess`.
- Show a confirmation `AlertDialog`: title `database_not_found`, message
  `confirm_download_database`, positive `download`, negative `cancel`.
- On **download** → call `downloadDatabaseWithDialog(...)`. On its success,
  show a `download_complete` toast and invoke `onSuccess()`. On failure, the
  failure toast is shown and `onSuccess` is NOT invoked.
- On **cancel** → dismiss; do nothing.

`onSuccess` is a `() -> Unit` (Kotlin functional type, called from Java as a
lambda returning `Unit.INSTANCE`).

**`downloadDatabaseWithDialog(activity, language, onFinish)`** — private.

- Build a horizontal `ProgressBar` programmatically
  (`android.R.attr.progressBarStyleHorizontal`) inside a non-cancelable
  `AlertDialog` (title `downloading`). No layout file needed.
- `GlobalScope.launch` (matches existing pattern), whole body wrapped in
  `try/catch`:
  - host = `if (isThaiClient(activity)) THAI_HOST else S3_HOST`
  - `filename = "${language.stringCode}.zip"`,
    `url = "$host/$filename"`,
    `path = Utils.getDatabaseDirectory(activity) + "/" + filename`
  - `runOnUiThread { showDialog() }` (guarded — see lifecycle guards)
  - `result = downloadDatabaseZipFile(activity, url, path, progressBar)`
  - set bar indeterminate, then `if (result) result = unzipDatabase(activity, path)`
  - `runOnUiThread { dismissDialog(); toast(message); onFinish(result) }`
  - `catch` → `runOnUiThread { dismissDialog(); onFinish(false) }`
- Completion message id: `download_complete` / `download_error` /
  `space_error`, matching `download()`.

### 2. `MainActivity` — reader compare

At the existing language-pick handler (around line 580), the `else` (file
missing) branch replaces the bare toast:

```java
DownloadDatabaseKt.confirmAndDownloadDatabase(this, targetLanguage, () -> {
    if (!isFinishing() && !isDestroyed()) {
        compare(references, page, targetLanguage);
    }
    return Unit.INSTANCE;
});
```

`references` and `page` are already captured in the dialog lambda; the
continuation simply re-runs the same `compare(...)` call the exists-branch
would have run.

### 3. `ComparisonActivity` — compare-again

Extract the current
`getComparingItemsAtPage(...) → mHandler.post(pickItemAndLaunch(...))` block
into a private method:

```java
private void proceedCompare(Language sourceLanguage, Language targetLanguage,
                            int volume, int page)
```

- Exists-branch (`new File(dbPath).exists()` true) → call `proceedCompare(...)`.
- Missing-branch → `confirmAndDownloadDatabase(this, targetLanguage, ...)`
  whose `onSuccess` re-checks `!isFinishing() && !isDestroyed()` then calls
  `proceedCompare(...)`.

This keeps the resume path identical to the normal path (single source of
truth) and avoids duplicating the items-fetch logic.

### Lifecycle guards (explicit)

The download runs on `GlobalScope` and completes well after the call site
returns; the activity may finish or recreate in the meantime. To avoid the
`WindowLeaked` / "View not attached to window manager" / FragmentManager
crashes the codebase has a history of:

- Guard `dialog.show()` and `dialog.dismiss()` behind
  `!activity.isFinishing && !activity.isDestroyed`, and wrap dialog
  show/dismiss in `try/catch`.
- Each continuation (`onSuccess`) re-checks `isFinishing()/isDestroyed()`
  before touching the activity.

### Error handling

| Situation | Behaviour |
|-----------|-----------|
| Offline at prompt time | Toast `no_internet_connection`; no download, no resume. |
| User taps Cancel | Dialog dismissed; user stays where they are. |
| Download/unzip fails | Toast `download_error` / `space_error`; no resume; user can retry. |
| Activity destroyed mid-download | Guards skip dialog ops + continuation; no crash. The downloaded DB (if completed) remains on disk for next time. |

## Testing

The download path hits the real network and S3/Thai host, so it follows the
same manual-QA posture as the Account-sync and bulk-download features (per
project history — those flows are manual-QA'd, not unit-tested).

Optional deterministic slice: an instrumented test asserting that
`confirmAndDownloadDatabase` is a no-op (and shows the offline toast) when the
device reports no network. Everything past the network gate requires a live
download and is covered by manual QA below.

### Manual QA

1. Reader compare (MainActivity): on a device missing e.g. `romanct.db`,
   compare → pick that language → confirm dialog appears → Download → progress
   0–100% → on finish the comparison opens automatically at the right item.
2. Compare-again (ComparisonActivity): while comparing, tap compare again →
   pick a missing-DB language → same confirm → download → split view continues.
3. Cancel: confirm dialog → Cancel → nothing downloads, user stays put.
4. Offline: turn off network → trigger a missing-DB compare → `no_internet`
   toast, no dialog.
5. Lifecycle: start a download, then immediately background / rotate / press
   back → no crash; if download completed, DB is present for next attempt.
6. Already-present DB: compare to a downloaded language → no prompt, behaves
   exactly as today (regression check).

## Files touched

- `app/.../helper/DownloadDatabase.kt` — add `confirmAndDownloadDatabase` +
  private `downloadDatabaseWithDialog`. (`download()` unchanged.)
- `app/.../activity/MainActivity.java` — missing-branch in the reader-compare
  language handler.
- `app/.../activity/ComparisonActivity.java` — extract `proceedCompare(...)`;
  wire missing-branch to the helper.
