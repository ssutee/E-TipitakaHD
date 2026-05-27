# Bulk Database Download on First Launch — Design

**Date:** 2026-05-27
**Status:** Approved, pending implementation

## Problem

The app bundles two databases (`thai.db` + `pali.db`) via
`assets/databases/etipitaka_plus.zip`, extracted by `StartupActivity`
on first launch. The other ten languages (`THAIMM`, `THAIMC`, `THAIBT`,
`THAIWN`, `THAIPB`, `ROMANCT`, `THAIVN`, `THAIMS`, `PALINEW`,
`THAIMC2`) are downloadable on demand, one at a time, through the
existing per-language flow.

Users have no first-class way to grab everything in one step. They
have to discover each language through the picker, hit a "missing DB"
state, accept a download, and repeat. This makes the app feel less
complete than it is, especially for users on Wi-Fi who would prefer
to download all content up front and read offline later.

## Solution

After the bundle unzip + version-info fetch in `StartupActivity` and
before launching `MainActivity`, check which downloadable databases
are missing. Prompt the user once with a list and a single "download
all" confirmation. On Yes, sequentially download each missing
database. Persist a flag so the prompt never reappears even if the
user dismisses it with DBs still missing.

## Flow

`StartupActivity.startApp()` continuation chain extended:

1. `moveOldDataFiles()` — unchanged.
2. `unzipBundleDatabase()` — unchanged (extracts bundled `thai.db` +
   `pali.db` if `thai.db` not present).
3. `updateDatabasesInfo()` — unchanged (fetches version metadata
   JSON, stores per-language version codes in `SharedPreferences("update")`).
4. **NEW** `offerBulkDownload()` — see below.
5. Launch `MainActivity`, finish `StartupActivity` — unchanged.

## `offerBulkDownload()` behavior

### Missing-DB enumeration

Iterate `Language.values()`. Skip `THAI` and `PALI` (bundled).
For each remaining language, the DB is missing if
`!new File(Utils.getDatabasePath(this, lang)).exists()`.

Candidates (10): `THAIMM`, `THAIMC`, `THAIBT`, `THAIWN`, `THAIPB`,
`ROMANCT`, `THAIVN`, `THAIMS`, `PALINEW`, `THAIMC2`.

### Gating

The prompt is shown only if **all** of the following hold:

1. The missing-DB set is non-empty.
2. `SharedPreferences("startup").getBoolean("bulk_download_check_done", false)`
   is `false`.
3. `Utils.isNetworkConnected(this)` is `true`.

If any gate fails, `offerBulkDownload` resolves immediately and the
chain proceeds to `MainActivity`. In particular, an offline
first-launch silently skips and does **not** set the flag, so the
prompt re-evaluates on the next online launch.

### Dialog

`AlertDialog` with:

- Title: `R.string.bulk_download_title` — "ดาวน์โหลดฐานข้อมูล"
- Message: `R.string.bulk_download_message` — template:
  ```
  ฐานข้อมูลต่อไปนี้ยังไม่ได้ดาวน์โหลด:
  • <lang_full_name_1>
  • <lang_full_name_2>
  …

  ต้องการดาวน์โหลดทั้งหมดเลยหรือไม่?
  ```
  Built at runtime from each missing language's
  `language.getFullName(context)`.
- Positive button: `R.string.bulk_download_yes` — "ดาวน์โหลด"
- Negative button: `R.string.bulk_download_no` — "ไม่"
- `setCancelable(false)` — force explicit choice.

Both buttons set `SharedPreferences("startup").edit().putBoolean("bulk_download_check_done", true).apply()`
before resolving. Positive then runs the sequential download;
negative resolves immediately.

### Sequential download

Implemented as a new Kotlin file `BulkDownloadDatabases.kt` next to
the existing `DownloadDatabase.kt`. Exposes:

```kotlin
suspend fun bulkDownload(
    activity: Activity,
    languages: List<BookDatabaseHelper.Language>,
    progressBar: ProgressBar,
    progressLabel: TextView
): BulkDownloadResult
```

`BulkDownloadResult(total: Int, succeeded: Int, failed: List<Language>)`.

Internals:

- For each language in order (1..N):
  - Update label on UI thread: "กำลังดาวน์โหลด X จาก N: <full_name>"
    via `R.string.bulk_download_progress` (3-arg format).
  - Await existing `download(activity, language, progressBar, ::resume)`
    wrapped in `suspendCoroutine` so per-language completion is
    sequential.
  - Track success/failure. Failures append to `failed`; loop continues.
- After the loop, hide progress UI, show summary toast:
  `R.string.bulk_download_summary` — "ดาวน์โหลดสำเร็จ %1$d จาก %2$d รายการ"
  (X / N).

`StartupActivity.offerBulkDownload` wraps `bulkDownload` in a
`TaskCompletionSource<Void>` so it slots into the existing
`bolts.Task` continuation chain.

### Reuse of existing per-file download

The existing `DownloadDatabase.download(...)` already handles:

- Thai/S3 host selection via `isThaiClient`.
- Progress bar updates per-file.
- `FLAG_NOT_TOUCHABLE` to block input during the file download.
- Toast for per-file complete / error.
- Unzip + cleanup.

We deliberately reuse it unchanged to keep the new path narrow. The
duplication is the per-file complete toast (the user will see N
toasts plus the final summary). Accept this for now — fixing means a
parameter to suppress the per-file toast, which is a small API change
we can revisit if it feels noisy in QA.

## SharedPreferences

- File: `"startup"`. Created lazily by Android on first write.
- Key: `"bulk_download_check_done"` — boolean, default `false`.

Separate from the existing `"update"` prefs file so the bulk-download
state isn't entangled with the per-language version-tracking.

## Layout changes

`res/layout/activity_startup.xml` currently contains a single
indeterminate circular `ProgressBar` (id `@+id/progressBar`) centered
in a `RelativeLayout`. The existing per-file `download(...)` flips
that bar to determinate via `progressBar.isIndeterminate = false`,
which renders poorly on the default circular style.

Restructure:

- Wrap content in a centered vertical `LinearLayout`:
  - `ProgressBar` id `@+id/progressBar`, default visible, default
    indeterminate (preserves existing startup-spinner behavior).
  - `ProgressBar` id `@+id/downloadProgressBar` with
    `style="?android:attr/progressBarStyleHorizontal"`,
    `android:max="100"`, initial `visibility="gone"`. Used by the
    bulk-download helper for per-file progress.
  - `TextView` id `@+id/downloadProgressLabel`, initial
    `visibility="gone"`. Used for the "X / N: <name>" caption.

The bulk-download helper toggles `progressBar.visibility = GONE` and
the two download views to `VISIBLE` while a file is in flight, then
reverses on completion. Existing startup flow (steps 1-3) sees the
spinner untouched.

## String resources

Add to `res/values/strings.xml` (Thai default — matches existing
account_* / download_* keys):

- `bulk_download_title` — "ดาวน์โหลดฐานข้อมูล"
- `bulk_download_message` — "ฐานข้อมูลต่อไปนี้ยังไม่ได้ดาวน์โหลด:\n\n%1$s\n\nต้องการดาวน์โหลดทั้งหมดเลยหรือไม่?"
- `bulk_download_yes` — "ดาวน์โหลด"
- `bulk_download_no` — "ไม่"
- `bulk_download_progress` — "กำลังดาวน์โหลด %1$d / %2$d: %3$s"
- `bulk_download_summary` — "ดาวน์โหลดสำเร็จ %1$d / %2$d รายการ"

## Error handling

- Per-file download failure: `DownloadDatabase.download(...)` returns
  `false` via callback. Helper records the language in `failed`, moves
  on. Final summary reflects actual count.
- Activity destroyed mid-batch (user back-press, system kill): the
  in-flight coroutine's continuation may attempt to dereference a
  destroyed Activity. Mitigate by wrapping the per-language await in
  `try/catch` and bailing if `activity.isFinishing || activity.isDestroyed`.
  The dismiss flag is already set, so the user can re-trigger
  per-language via the picker on the next launch.
- Disk full during unzip: existing `unzipDatabase` returns false. The
  per-file `download(...)` then shows `R.string.space_error` toast.
  Our helper records the failure and continues. Subsequent languages
  will likely fail too; the summary surfaces the partial count.

## Out of scope

- Cancel button mid-batch. User can press back to exit; the
  in-flight `download(...)` is best-effort terminated, the flag is
  already set, future launches won't re-prompt. Per-language picker
  remains the recovery path.
- Re-prompt after N days. User chose "remember forever".
- Parallel downloads. Decision was sequential single-bar.
- Storage-full pre-flight check. Existing per-file unzip already
  surfaces space errors.
- Suppressing per-file complete toast. Accepted noise for now;
  revisit after QA.

## Files touched

- `app/src/main/java/com/watnapp/etipitaka/plus/helper/BulkDownloadDatabases.kt`
  (new)
- `app/src/main/java/com/watnapp/etipitaka/plus/activity/StartupActivity.java`
  — new `offerBulkDownload()` step + wiring.
- `app/src/main/res/layout/activity_startup.xml` — add progress label
  (and progress bar if absent).
- `app/src/main/res/values/strings.xml` — 6 new strings.

No changes to `DownloadDatabase.kt`, `Language` enum,
`BookDatabaseHelper`, or any data-model class.
