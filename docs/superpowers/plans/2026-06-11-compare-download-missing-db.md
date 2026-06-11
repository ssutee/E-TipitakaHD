# Compare → download missing DB → continue — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** When the user starts a comparison against a language whose database file is missing, prompt to download it (confirm → modal progress), then automatically resume the interrupted comparison — at both compare entry points.

**Architecture:** Add a reusable `confirmAndDownloadDatabase(activity, language, onSuccess)` to `DownloadDatabase.kt` that shows a confirm dialog, downloads via a programmatic modal progress dialog (reusing the file's existing `isThaiClient` / `downloadDatabaseZipFile` / `unzipDatabase` suspend helpers), and invokes a continuation on success. Wire the two existing "database not found" call sites (`MainActivity` reader-compare, `ComparisonActivity` compare-again) to call it instead of toasting and bailing. The existing `download()` used by `MenuFragment` is left untouched.

**Tech Stack:** Kotlin (coroutines, `GlobalScope`), Java callers, Android `AlertDialog` + programmatic `ProgressBar`. Spec: `docs/superpowers/specs/2026-06-11-compare-download-missing-db-design.md`.

---

## File Structure

- **Modify** `app/src/main/java/com/watnapp/etipitaka/plus/helper/DownloadDatabase.kt` — add `confirmAndDownloadDatabase` (public, Java-callable) + private `downloadDatabaseWithDialog`. No change to existing functions.
- **Modify** `app/src/main/java/com/watnapp/etipitaka/plus/activity/MainActivity.java` — reader-compare missing-DB branch (~line 582) calls the helper; add `import kotlin.Unit;`.
- **Modify** `app/src/main/java/com/watnapp/etipitaka/plus/activity/ComparisonActivity.java` — extract `proceedCompare(...)`; compare-again missing-DB branch (~line 163) calls the helper; add `import kotlin.Unit;`.

No new string resources (all reused). No layout/XML changes.

**Testing note:** The download path hits the live network + S3/Thai host and is manual-QA'd (same posture as Account-sync and bulk-download per project history). The one deterministic slice — offline → no-op + toast — is an instrumented test in Task 1. Everything past the network gate is covered by the manual-QA checklist at the end.

---

## Task 1: Reusable confirm + download-with-dialog helper

**Files:**
- Modify: `app/src/main/java/com/watnapp/etipitaka/plus/helper/DownloadDatabase.kt`
- Test: `app/src/androidTest/java/com/watnapp/etipitaka/CompareDownloadInstrumentedTest.kt` (create)

- [ ] **Step 1: Write the failing test**

The only deterministic behaviour without a live download: when the device reports no network, `confirmAndDownloadDatabase` must NOT invoke the success continuation (it shows the offline toast and returns). We assert the continuation is not called within a short window. Use a language whose DB is absent so the missing-DB path is what's under test.

Create `app/src/androidTest/java/com/watnapp/etipitaka/CompareDownloadInstrumentedTest.kt`:

```kotlin
package com.watnapp.etipitaka

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.watnapp.etipitaka.plus.Utils
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper
import com.watnapp.etipitaka.plus.helper.confirmAndDownloadDatabase
import org.junit.Assume.assumeFalse
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * The only deterministic slice of the compare→download→continue flow: when
 * offline, confirmAndDownloadDatabase must never reach the success
 * continuation. The full download path hits the live network and is
 * manual-QA'd (see the plan's manual-QA checklist).
 */
@RunWith(AndroidJUnit4::class)
class CompareDownloadInstrumentedTest {

  private val context: Context
    get() = InstrumentationRegistry.getInstrumentation().targetContext

  @Test
  fun offline_doesNotInvokeContinuation() {
    // Only meaningful when actually offline AND the target DB is absent.
    assumeFalse("device is online — skipping offline test",
        Utils.isNetworkConnected(context))
    val dbPath = Utils.getDatabasePath(context, BookDatabaseHelper.Language.ROMANCT)
    assumeFalse("romanct.db present — skipping", File(dbPath).exists())

    val continued = AtomicBoolean(false)
    val latch = CountDownLatch(1)
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
      confirmAndDownloadDatabase(
          InstrumentationRegistry.getInstrumentation().context as android.app.Activity,
          BookDatabaseHelper.Language.ROMANCT
      ) {
        continued.set(true)
      }
      latch.countDown()
    }
    latch.await(2, TimeUnit.SECONDS)
    assertFalse("continuation must not run while offline", continued.get())
  }
}
```

Note: this test is `@Assume`-gated to offline + missing-DB, so it skips on a normal online CI device. It documents and locks the offline contract; it does not exercise the live download.

- [ ] **Step 2: Run test to verify it fails (compile error — function doesn't exist yet)**

Run:
```bash
./gradlew compileDebugAndroidTestSources
```
Expected: FAIL — `unresolved reference: confirmAndDownloadDatabase`.

- [ ] **Step 3: Implement the helper in `DownloadDatabase.kt`**

Add these imports to the existing import block (top of the file), keeping the others:

```kotlin
import android.app.AlertDialog
import android.widget.LinearLayout
```

Append the two functions at the end of the file (after `download(...)`):

```kotlin
/**
 * Show a confirmation dialog offering to download the (missing) database for
 * [language]; on confirm, download it with a modal progress dialog and, on
 * success, invoke [onSuccess]. Used by the compare flows when the target
 * language's DB is not yet on disk. [onSuccess] runs on the UI thread.
 *
 * No-ops (with an offline toast) when there is no network. Does NOT invoke
 * [onSuccess] on cancel or on download failure.
 */
@OptIn(DelicateCoroutinesApi::class)
fun confirmAndDownloadDatabase(
    activity: Activity,
    language: BookDatabaseHelper.Language,
    onSuccess: () -> Unit
) {
    if (!Utils.isNetworkConnected(activity)) {
        Toast.makeText(activity, R.string.no_internet_connection, Toast.LENGTH_LONG).show()
        return
    }
    AlertDialog.Builder(activity)
        .setTitle(R.string.database_not_found)
        .setMessage(R.string.confirm_download_database)
        .setPositiveButton(R.string.download) { dialog, _ ->
            dialog.dismiss()
            downloadDatabaseWithDialog(activity, language) { success ->
                if (success) onSuccess()
            }
        }
        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        .create()
        .show()
}

/**
 * Download [language]'s database zip with a non-cancelable modal progress
 * dialog, unzip it, and report the result via [onFinish] on the UI thread.
 * Reuses the same suspend helpers as [download]. Builds its progress UI
 * programmatically so callers need no layout of their own.
 */
@OptIn(DelicateCoroutinesApi::class)
private fun downloadDatabaseWithDialog(
    activity: Activity,
    language: BookDatabaseHelper.Language,
    onFinish: (success: Boolean) -> Unit
) {
    val progressBar = ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal)
    val pad = (16 * activity.resources.displayMetrics.density).toInt()
    val container = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(pad, pad, pad, pad)
        addView(progressBar)
    }
    val dialog = AlertDialog.Builder(activity)
        .setTitle(R.string.downloading)
        .setView(container)
        .setCancelable(false)
        .create()

    fun alive() = !activity.isFinishing && !activity.isDestroyed

    GlobalScope.launch {
        try {
            val host = if (isThaiClient(activity)) Constants.THAI_HOST else Constants.S3_HOST
            activity.runOnUiThread {
                progressBar.isIndeterminate = false
                if (alive()) {
                    try { dialog.show() } catch (e: Exception) { Log.w("UPDATE", "dialog.show failed", e) }
                }
            }

            val filename = "${language.stringCode}.zip"
            val url = "$host/$filename"
            val path = Utils.getDatabaseDirectory(activity) + "/" + filename

            var result = downloadDatabaseZipFile(activity, url, path, progressBar)
            activity.runOnUiThread { progressBar.isIndeterminate = true }

            var messageId = if (result) R.string.download_complete else R.string.download_error
            if (result) {
                result = unzipDatabase(activity, path)
                messageId = if (result) R.string.download_complete else R.string.space_error
            }

            val finalResult = result
            val finalMessageId = messageId
            activity.runOnUiThread {
                if (alive()) {
                    try { dialog.dismiss() } catch (e: Exception) { Log.w("UPDATE", "dialog.dismiss failed", e) }
                    Toast.makeText(activity, finalMessageId, Toast.LENGTH_SHORT).show()
                }
                onFinish(finalResult)
            }
        } catch (e: Exception) {
            Log.e("UPDATE", "downloadDatabaseWithDialog failed for ${language.stringCode}", e)
            activity.runOnUiThread {
                if (alive()) {
                    try { dialog.dismiss() } catch (ignored: Exception) {}
                }
                onFinish(false)
            }
        }
    }
}
```

- [ ] **Step 4: Run the test (and compile) to verify it passes**

Boot the emulator first if not running:
```bash
$HOME/Library/Android/sdk/emulator/emulator -avd Pixel_Tablet -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect &
$HOME/Library/Android/sdk/platform-tools/adb wait-for-device
```
Run:
```bash
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.watnapp.etipitaka.CompareDownloadInstrumentedTest
```
Expected: BUILD SUCCESSFUL. The test SKIPS on an online device (assume-gated) — a skip here is success; it proves the symbol compiles and links. (The compile is the real gate on CI; the offline assertion only runs on an offline device.)

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/watnapp/etipitaka/plus/helper/DownloadDatabase.kt \
        app/src/androidTest/java/com/watnapp/etipitaka/CompareDownloadInstrumentedTest.kt
git commit -m "feat(compare): add confirmAndDownloadDatabase helper

Reusable confirm-dialog + modal-progress download that reuses the
existing isThaiClient/downloadDatabaseZipFile/unzipDatabase helpers and
invokes a continuation on success. Lifecycle-guarded dialog show/dismiss.

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 2: Wire MainActivity reader-compare

**Files:**
- Modify: `app/src/main/java/com/watnapp/etipitaka/plus/activity/MainActivity.java` (import + ~line 582 branch)

- [ ] **Step 1: Add the `kotlin.Unit` import**

After the existing import block (the file has no `import kotlin.Unit;` yet), add it. Insert near the other top-level imports, e.g. after line 44 (`import com.watnapp.etipitaka.plus.account.UserDataExporter;`):

```java
import kotlin.Unit;
```

- [ ] **Step 2: Replace the missing-DB toast with the download prompt**

Find this block (currently ~lines 580–585):

```java
              if (new File(dbPath).exists()) {
                compare(references, page, targetLanguage);
              } else {
                Toast.makeText(MainActivity.this,
                        R.string.database_not_found, Toast.LENGTH_LONG).show();
              }
```

Replace the `else` body with:

```java
              if (new File(dbPath).exists()) {
                compare(references, page, targetLanguage);
              } else {
                // DB for the chosen compare target is missing — offer to
                // download it, then resume the comparison automatically.
                DownloadDatabaseKt.confirmAndDownloadDatabase(
                    MainActivity.this, targetLanguage, () -> {
                      if (!isFinishing() && !isDestroyed()) {
                        compare(references, page, targetLanguage);
                      }
                      return Unit.INSTANCE;
                    });
              }
```

Note: `references` and `page` are already effectively-final locals captured by the enclosing `setItems` lambda, so they are available here. `DownloadDatabaseKt` is the generated facade class for `DownloadDatabase.kt` (same pattern already used: `MenuFragment` imports `DownloadDatabaseKt.download`). It is in package `com.watnapp.etipitaka.plus.helper`, which `MainActivity` already imports members of (`BookDatabaseHelper`), so reference it fully-qualified as `DownloadDatabaseKt` after adding the import below.

Add this import alongside the others (after line 41, `import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper.Language;`):

```java
import com.watnapp.etipitaka.plus.helper.DownloadDatabaseKt;
```

- [ ] **Step 3: Compile to verify it builds**

Run:
```bash
./gradlew compileDebugJavaWithJavac
```
Expected: BUILD SUCCESSFUL (note: pre-existing deprecation warnings are fine).

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/watnapp/etipitaka/plus/activity/MainActivity.java
git commit -m "feat(compare): offer DB download from reader compare when missing

MainActivity reader-compare now prompts to download a missing target
database and resumes the comparison on success instead of just toasting.

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 3: Wire ComparisonActivity compare-again

**Files:**
- Modify: `app/src/main/java/com/watnapp/etipitaka/plus/activity/ComparisonActivity.java` (imports + `onCompareButtonClick` ~line 149, new `proceedCompare`)

- [ ] **Step 1: Add imports**

After line 21 (`import com.watnapp.etipitaka.plus.model.ETDataModelCreator;`) add:

```java
import com.watnapp.etipitaka.plus.helper.DownloadDatabaseKt;
```

After line 26 (`import dart.DartModel;`) add a blank line then:

```java
import kotlin.Unit;
```

(Placement is cosmetic; any valid import position works. Keep `kotlin.Unit` grouped with non-project imports if you prefer.)

- [ ] **Step 2: Extract `proceedCompare` and route both branches through it**

Replace the current `onCompareButtonClick` body (lines 149–172) — specifically the `setItems` lambda's missing-DB toast and the items-fetch call — so the method becomes:

```java
  public void onCompareButtonClick(final Language language, final int volume, final int page) {
    final ETDataModel sourceModel = language == mDataModel1.getLanguage() ? mDataModel1 : mDataModel2;

    if (sourceModel.getLanguage() == Language.THAIBT || sourceModel.getLanguage() == Language.THAIPB) {
      return;
    }

    new AlertDialog.Builder(this).setTitle(R.string.select_langauge)
        .setItems(Constants.COMPARE_TITLES, (dialog, which) -> {
          final Language targetLanguage = Constants.COMPARE_LANGUAGES[which];
          if (targetLanguage == sourceModel.getLanguage()) {
            return;
          }
          String dbPath = Utils.getDatabasePath(ComparisonActivity.this, targetLanguage);
          if (new File(dbPath).exists()) {
            proceedCompare(sourceModel, language, targetLanguage, volume, page);
          } else {
            // Target DB missing — offer download, then resume on success.
            DownloadDatabaseKt.confirmAndDownloadDatabase(
                ComparisonActivity.this, targetLanguage, () -> {
                  if (!isFinishing() && !isDestroyed()) {
                    proceedCompare(sourceModel, language, targetLanguage, volume, page);
                  }
                  return Unit.INSTANCE;
                });
          }
        }).create().show();
  }

  /**
   * Fetch the comparable items at (volume, page) from sourceModel and present
   * the item picker / launch. Shared by the normal path and the
   * post-download continuation so the resume behaves identically.
   */
  private void proceedCompare(final ETDataModel sourceModel, final Language sourceLanguage,
                              final Language targetLanguage, final int volume, final int page) {
    sourceModel.getComparingItemsAtPage(volume, page, (items, sections) ->
        mHandler.post(() -> pickItemAndLaunch(sourceLanguage, targetLanguage, volume, page, items, sections)));
  }
```

This preserves the existing behaviour exactly for the DB-present case (same `getComparingItemsAtPage` → `pickItemAndLaunch` chain), now reached through `proceedCompare`. `pickItemAndLaunch` and `launchComparison` below are unchanged.

- [ ] **Step 3: Compile to verify it builds**

Run:
```bash
./gradlew compileDebugJavaWithJavac
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/watnapp/etipitaka/plus/activity/ComparisonActivity.java
git commit -m "feat(compare): offer DB download from compare-again when missing

ComparisonActivity compare-again now prompts to download a missing
target database and resumes via the extracted proceedCompare() on
success instead of toasting and bailing.

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 4: Build verification + regression test run

**Files:** none (verification only)

- [ ] **Step 1: Full debug build + lint compile**

Run:
```bash
./gradlew compileDebugJavaWithJavac compileDebugAndroidTestSources
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Run existing instrumented regression suites (no regressions)**

Boot the emulator if needed (see Task 1 Step 4), then:
```bash
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.watnapp.etipitaka.CompareNearestItemInstrumentedTest,com.watnapp.etipitaka.DatabaseFailureInstrumentedTest,com.watnapp.etipitaka.CompareDownloadInstrumentedTest
```
Expected: BUILD SUCCESSFUL; `CompareNearestItemInstrumentedTest` 10/10 pass, `DatabaseFailureInstrumentedTest` 4 pass (or skips if a real thaiwn.db is present), `CompareDownloadInstrumentedTest` skips on an online device.

- [ ] **Step 3: Run JVM unit tests**

Run:
```bash
./gradlew testDebugUnitTest
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: No commit (verification task).** If any step fails, fix in the owning task before proceeding to manual QA.

---

## Manual QA (post-implementation, on device)

Cannot be automated (live download). Run before release:

1. **Reader compare, missing DB:** device without e.g. `romanct.db` → from the reader, Compare → pick Roman Script → confirm dialog (`ไม่พบฐานข้อมูล` / `คุณต้องการดาวน์โหลด…`) → Download → progress 0–100% → comparison opens automatically at the right item.
2. **Compare-again, missing DB:** while in split-compare, tap Compare again → pick a missing-DB language → same confirm → download → split view continues.
3. **Cancel:** confirm dialog → ยกเลิก → nothing downloads, user stays put.
4. **Offline:** disable network → trigger a missing-DB compare → `ไม่สามารถเชื่อมต่ออินเทอร์เน็ต` toast, no dialog, no resume.
5. **Lifecycle:** start a download, then immediately background / rotate / press Back → no crash; if the download completed, the DB is present next attempt.
6. **Regression — DB present:** compare to an already-downloaded language → no prompt, behaves exactly as before.

---

## Self-review notes

- **Spec coverage:** confirm dialog (Task 1) ✓; modal progress download (Task 1) ✓; offline no-op + toast (Task 1, tested) ✓; auto-resume both entry points (Tasks 2, 3) ✓; reuse existing `download()` untouched (Task 1 appends, no edits to `download`) ✓; lifecycle guards (Task 1 dialog guards + Tasks 2/3 `isFinishing/isDestroyed`) ✓; no new strings / no layout (verified) ✓.
- **Type consistency:** `confirmAndDownloadDatabase(Activity, Language, () -> Unit)` defined in Task 1 and called identically in Tasks 2 & 3 (`() -> { …; return Unit.INSTANCE; }`). `proceedCompare(ETDataModel, Language, Language, int, int)` defined and called consistently within Task 3. `DownloadDatabaseKt` facade name matches the existing `MenuFragment` usage.
- **Placeholders:** none — all steps carry full code and exact commands.
